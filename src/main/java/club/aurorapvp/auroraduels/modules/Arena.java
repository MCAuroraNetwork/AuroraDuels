package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.data.ArenaDataHandler;
import club.aurorapvp.auroraduels.data.SchematicDataHandler;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Arena {
  private static final Set<Arena> arenas = new HashSet<>();
  private BlockVector3 spawn1 = null;
  private BlockVector3 spawn2 = null;
  private BlockVector3 pos1 = null;
  private BlockVector3 pos2 = null;
  private static Arena unfinishedArena;
  private ArenaState state;
  private boolean occupied = false;
  private final SchematicDataHandler schematic;
  private final ArenaDataHandler data;
  private final String name;
  private World world;

  public Arena(String arenaName) {
    this.name = arenaName;
    this.state = ArenaState.IDLE;

    this.schematic = new SchematicDataHandler(this);
    this.data = new ArenaDataHandler(this);

    if (this.exists()) {
      state = ArenaState.COMPLETE;
      this.reload();
    }
  }

  public static void init() {
    File dir = new File(AuroraDuels.INSTANCE.getDataFolder(), "/arenas/");

    if (dir.exists()) {
      for (File file : Objects.requireNonNull(dir.listFiles())) {
        Arena arena = new Arena(file.getName().replace(".yml", ""));
        if (arena.exists()) {
          arenas.add(arena);
        }
      }
    }

    AuroraDuels.INSTANCE.getLogger().info("All arenas loaded!");
  }

  public static Set<Arena> getArenas() {
    return arenas;
  }

  public ArenaState getState() {
    return state;
  }

  public String getName() {
    return name;
  }

  public World getWorld() {
    return world;
  }

  public boolean isOccupied() {
    return occupied;
  }

  public void setOccupied(boolean occupied) {
    this.occupied = occupied;
  }

  public BlockVector3 getFirstPosition() {
    return pos1;
  }

  public BlockVector3 getSecondPosition() {
    return pos2;
  }

  public BlockVector3 getFirstSpawn() {
    return spawn1;
  }

  public BlockVector3 getSecondSpawn() {
    return spawn2;
  }

  public boolean exists() {
    return this.data.getFile().exists() && this.schematic.getFile().exists();
  }

  public void reload() {
    spawn1 = this.data.getFirstSpawn();
    spawn2 = this.data.getSecondSpawn();
    world = this.data.getWorld();
  }

  public static Arena getArena(String arenaName) {
    for (Arena arena : arenas) {
      if (Objects.equals(arenaName, arena.name)) {
        return arena;
      }
    }
    return null;
  }

  public static void onWandUse(PlayerInteractEvent event) {
    Player p = event.getPlayer();
    p.setCooldown(Material.IRON_HOE, 20);
    Action action = event.getAction();
    BlockVector3 vector = BukkitAdapter.asBlockVector(
        Objects.requireNonNull(event.getClickedBlock()).getLocation());

    switch (unfinishedArena.getState()) {
      case SPAWN_1, SPAWN_2 -> unfinishedArena.setSpawns(p, action, vector);
      case CORNER_1, CORNER_2 -> unfinishedArena.setCorners(p, action, vector);
      default -> {
        AuroraDuels.INSTANCE.getLogger().warning(
            "Invalid ArenaState value " + unfinishedArena.getState().toString() +
                "! Resetting to idle");
        unfinishedArena.state = ArenaState.IDLE;
      }
    }
  }

  public void startCreation(Player p) {
    if (state == ArenaState.IDLE && !this.exists()) {
      state = ArenaState.SPAWN_1;
      Wand.setActive(Wand.WandActivity.LEFT);
      unfinishedArena = this;

      p.getInventory().getItemInMainHand().setAmount(1);
      p.getInventory().getItemInMainHand().setType(Material.IRON_HOE);
      p.sendMessage(Lang.getComponent("set-spawn1"));
    } else if (state != ArenaState.IDLE) {
      p.sendMessage(Lang.getComponent("arena-already-being-created"));
    } else {
      p.sendMessage(Lang.getComponent("arena-already-exists"));
    }
  }

  public void setSpawns(Player p, Action action, BlockVector3 vector) {
    if (action.isLeftClick() && state == ArenaState.SPAWN_1) {
      spawn1 = vector;

      p.sendMessage(
          MiniMessage.miniMessage().deserialize(Lang.getString("spawn1-set") +
              " (" + spawn1.getBlockX() + ", " + spawn1.getBlockY() + ", " +
              spawn1.getBlockZ() +
              ")"));

      p.sendMessage(Lang.getComponent("set-spawn2"));
      state = ArenaState.SPAWN_2;
      Wand.setActive(Wand.WandActivity.RIGHT);
    }

    if (action.isRightClick() && state == ArenaState.SPAWN_2) {
      spawn2 = vector;

      p.sendMessage(
          MiniMessage.miniMessage().deserialize(Lang.getString("spawn2-set") +
              " (" + spawn2.getBlockX() + ", " + spawn2.getBlockY() + ", " +
              spawn2.getBlockZ() + ")"));

      p.sendMessage(Lang.getComponent("spawns-set"));
      p.sendMessage(Lang.getComponent("set-pos1"));
      world = p.getWorld();
      state = ArenaState.CORNER_1;
      Wand.setActive(Wand.WandActivity.LEFT);
    }
  }

  public void setCorners(Player p, Action action, BlockVector3 vector) {
    if (action.isLeftClick() && state == ArenaState.CORNER_1) {
      pos1 = vector;
      p.sendMessage(
          MiniMessage.miniMessage().deserialize(Lang.getString("pos1-set") +
              " (" + pos1.getBlockX() + ", " + pos1.getBlockY() + ", " + pos1.getBlockZ() +
              ")"));

      p.sendMessage(Lang.getComponent("set-pos2"));
      state = ArenaState.CORNER_2;
      Wand.setActive(Wand.WandActivity.RIGHT);
    }

    if (action.isRightClick() && state == ArenaState.CORNER_2) {
      pos2 = vector;
      p.sendMessage(
          MiniMessage.miniMessage().deserialize(Lang.getString("pos2-set") +
              " (" + pos2.getBlockX() + ", " + pos2.getBlockY() + ", " + pos2.getBlockZ() +
              ")"));

      p.sendMessage(Lang.getComponent("arena-created"));
      Wand.setActive(Wand.WandActivity.INACTIVE);
      schematic.create();
      data.create();
      state = ArenaState.COMPLETE;
      arenas.add(this);
    }
  }

  public void reset(Player p) {
    if (this.exists()) {
      new SchematicDataHandler(this).paste();
      p.sendMessage(Lang.getComponent("arena-reset"));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  public void reset() {
    if (this.exists()) {
      new SchematicDataHandler(this).paste();
    }
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void delete(Player p) {
    if (this.exists()) {
      this.data.getFile().delete();
      this.schematic.getFile().delete();
      arenas.remove(this);

      p.sendMessage(Lang.getComponent("arena-deleted"));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  public static void reloadAll() {
    for (Arena arena : Arena.arenas) {
      arena.reload();
    }

    AuroraDuels.INSTANCE.getLogger().info("ALl arenas reloaded!");
  }

  private enum ArenaState {
    IDLE,
    SPAWN_1,
    SPAWN_2,
    CORNER_1,
    CORNER_2,
    COMPLETE
  }
}