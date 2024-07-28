package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.data.ArenaDataHandler;
import club.aurorapvp.auroraduels.data.SchematicDataHandler;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Arena {

  private static final Set<Arena> arenas = new HashSet<>();
  private static Arena unfinishedArena;
  private final SchematicDataHandler schematic;
  private final ArenaDataHandler data;
  private final String name;
  private BlockVector3 spawn1 = null;
  private BlockVector3 spawn2 = null;
  private Location corner1;
  private Location corner2;
  private BlockVector3 pos1 = null;
  private BlockVector3 pos2 = null;
  private ArenaState state;
  private boolean occupied = false;
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
    File dir = new File(AuroraDuels.getInstance().getDataFolder(), "/arenas/");

    if (dir.exists()) {
      for (File file : Objects.requireNonNull(dir.listFiles())) {
        Arena arena = new Arena(file.getName().replace(".yml", ""));
        if (arena.exists()) {
          arenas.add(arena);
        }
      }
    }

    AuroraDuels.getInstance().getLogger().info("All arenas loaded!");
  }

  public static Set<Arena> getArenas() {
    return arenas;
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
    event.setCancelled(true);
    Player player = event.getPlayer();
    player.setCooldown(Material.IRON_HOE, 20);
    Action action = event.getAction();
    BlockVector3 vector =
        BukkitAdapter.asBlockVector(Objects.requireNonNull(event.getClickedBlock()).getLocation());

    switch (unfinishedArena.getState()) {
      case SPAWN_1, SPAWN_2 -> unfinishedArena.setSpawns(player, action, vector);
      case CORNER_1, CORNER_2 -> unfinishedArena.setCorners(player, action, vector);
      default -> {
        AuroraDuels.getInstance()
            .getLogger()
            .warning(
                "Invalid ArenaState value "
                    + unfinishedArena.getState().toString()
                    + "! Resetting to idle");
        unfinishedArena.state = ArenaState.IDLE;
      }
    }
  }

  public static void reloadAll() {
    arenas.clear();

    File dir = new File(AuroraDuels.getInstance().getDataFolder(), "/arenas/");

    if (dir.exists()) {
      for (File file : Objects.requireNonNull(dir.listFiles())) {
        Arena arena = new Arena(file.getName().replace(".yml", ""));
        if (arena.exists()) {
          arenas.add(arena);
        }
      }
    }

    AuroraDuels.getInstance().getLogger().info("ALl arenas reloaded!");
  }

  private ArenaState getState() {
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

  public Location getCornerOne() {
    return corner1;
  }

  public Location getCornerTwo() {
    return corner2;
  }

  public boolean exists() {
    return this.data.exists() && this.schematic.exists();
  }

  public void reload() {
    spawn1 = this.data.getFirstSpawn();
    spawn2 = this.data.getSecondSpawn();
    corner1 = this.data.getCornerOne();
    corner2 = this.data.getCornerTwo();
    world = this.data.getWorld();
  }

  public void startCreation(Player player) {
    if (state == ArenaState.IDLE && !this.exists()) {
      state = ArenaState.SPAWN_1;
      Wand.setActive(Wand.WandActivity.LEFT);
      unfinishedArena = this;

      player.getInventory()
          .setItem(player.getInventory().getHeldItemSlot(), new ItemStack(Material.IRON_HOE));

      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("set-spawn1"));
    } else if (state != ArenaState.IDLE) {
      player.sendMessage(
          AuroraDuels.getInstance().getLang().getComponent("arena-already-being-created"));
    } else {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("arena-already-exists"));
    }
  }

  public void setSpawns(Player player, Action action, BlockVector3 vector) {
    if (action.isLeftClick() && state == ArenaState.SPAWN_1) {
      spawn1 = vector.add(0, 1, 0);

      player.sendMessage(
          MiniMessage.miniMessage()
              .deserialize(
                  AuroraDuels.getInstance().getLang().getString("spawn1-set")
                      + " ("
                      + spawn1.x()
                      + ", "
                      + spawn1.y()
                      + ", "
                      + spawn1.z()
                      + ")"));

      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("set-spawn2"));
      state = ArenaState.SPAWN_2;
      Wand.setActive(Wand.WandActivity.RIGHT);
    }

    if (action.isRightClick() && state == ArenaState.SPAWN_2) {
      spawn2 = vector.add(0, 1, 0);

      player.sendMessage(
          MiniMessage.miniMessage()
              .deserialize(
                  AuroraDuels.getInstance().getLang().getString("spawn2-set")
                      + " ("
                      + spawn2.x()
                      + ", "
                      + spawn2.y()
                      + ", "
                      + spawn2.z()
                      + ")"));

      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("spawns-set"));
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("set-pos1"));
      world = player.getWorld();
      state = ArenaState.CORNER_1;
      Wand.setActive(Wand.WandActivity.LEFT);
    }
  }

  public void setCorners(Player player, Action action, BlockVector3 vector) {
    if (action.isLeftClick() && state == ArenaState.CORNER_1) {
      pos1 = vector;

      corner1 = new Location(this.getWorld(), vector.x(), vector.y(), vector.z());

      player.sendMessage(
          MiniMessage.miniMessage()
              .deserialize(
                  AuroraDuels.getInstance().getLang().getString("pos1-set")
                      + " ("
                      + pos1.x()
                      + ", "
                      + pos1.y()
                      + ", "
                      + pos1.z()
                      + ")"));

      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("set-pos2"));
      state = ArenaState.CORNER_2;
      Wand.setActive(Wand.WandActivity.RIGHT);
    }

    if (action.isRightClick() && state == ArenaState.CORNER_2) {
      pos2 = vector;

      corner2 = new Location(this.getWorld(), vector.x(), vector.y(), vector.z());

      player.sendMessage(
          MiniMessage.miniMessage()
              .deserialize(
                  AuroraDuels.getInstance().getLang().getString("pos2-set")
                      + " ("
                      + pos2.x()
                      + ", "
                      + pos2.y()
                      + ", "
                      + pos2.z()
                      + ")"));

      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("arena-created"));
      Wand.setActive(Wand.WandActivity.INACTIVE);
      schematic.create();
      data.create();
      state = ArenaState.COMPLETE;
      arenas.add(this);
    }
  }

  public void reset(Player player) {
    if (this.exists()) {
      this.reset();
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("arena-reset"));
    } else {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("arena-does-not-exist"));
    }
  }

  public void reset() {
    Arena thisArena = this;

    Bukkit.getScheduler()
        .runTask(
            AuroraDuels.getInstance(),
            () -> {
              for (Entity entity : thisArena.getWorld().getEntities()) {
                if (thisArena.isInBounds(entity.getLocation()) && !(entity instanceof Player)) {
                  entity.remove();
                }
              }
            });

    if (this.exists()) {
      new SchematicDataHandler(this).paste();
    }
  }

  public void delete(Player player) {
    if (this.exists()) {
      this.data.delete();
      this.schematic.delete();
      arenas.remove(this);

      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("arena-deleted"));
    } else {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("arena-does-not-exist"));
    }
  }

  public boolean isInBounds(Location loc) {
    Location corner1 = this.getCornerOne();
    Location corner2 = this.getCornerTwo();

    double minX = Math.min(corner1.x(), corner2.x());
    double minY = Math.min(corner1.y(), corner2.y());
    double minZ = Math.min(corner1.z(), corner2.z());
    double maxX = Math.max(corner1.x(), corner2.x());
    double maxY = Math.max(corner1.y(), corner2.y());
    double maxZ = Math.max(corner1.z(), corner2.z());

    return loc.x() >= minX
        && loc.x() <= maxX
        && loc.y() >= minY
        && loc.y() <= maxY
        && loc.z() >= minZ
        && loc.z() <= maxZ;
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
