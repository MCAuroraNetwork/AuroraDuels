package club.aurorapvp.modules;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.datahandlers.ArenaData;
import club.aurorapvp.datahandlers.SchematicData;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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
  private static int creationPhase = 0;
  private static Arena unFinishedArena;
  private boolean occupied = false;
  private final SchematicData schematic;
  private final ArenaData data;
  private final String name;
  private World world;

  public Arena(String arenaName) {
    this.name = arenaName;

    this.schematic = new SchematicData(this);
    this.data = new ArenaData(this);

    if (this.exists()) {
      this.reload();
    }
  }

  public static void init() {
    File dir = new File(AuroraDuels.DATA_FOLDER, "/arenas/");

    if (dir.exists()) {
      for (File file : dir.listFiles()) {
        Arena arena = new Arena(file.getName().replace(".yml", ""));
        if (arena.exists()) {
          arenas.add(arena);
        }
      }
    }

    AuroraDuels.INSTANCE.getLogger().info("All arenas loaded!");
  }

  public World getWorld() {
    return world;
  }

  public String getName() {
    return name;
  }

  public ArenaData getData() {
    return data;
  }

  public SchematicData getSchematic() {
    return schematic;
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
    return this.getData().getFile().exists() && this.getSchematic().getFile().exists();
  }

  public static Arena[] getArenas() {
    Arena[] arenaArray = new Arena[arenas.size()];

    arenaArray = arenas.toArray(arenaArray);

    return arenaArray;
  }

  public void reload() {
    spawn1 = this.getData().getFirstSpawn();
    spawn2 = this.getData().getSecondSpawn();
    world = this.getData().getWorld();
  }

  public static Arena getArena(String arenaName) {
    for (Arena arena : arenas) {
      if (Objects.equals(arenaName, arena.getName())) {
        return arena;
      }
    }
    return null;
  }

  public static void onWandEvent(PlayerInteractEvent event) {
    Player p = event.getPlayer();
    p.setCooldown(Material.IRON_HOE, 20);
    Action action = event.getAction();
    BlockVector3 vector = BukkitAdapter.asBlockVector(event.getClickedBlock().getLocation());

    if (creationPhase != 0) {
      switch (creationPhase) {
        case 1 -> unFinishedArena.setSpawns(p, action, vector);
        case 2 -> unFinishedArena.setCorners(p, action, vector);
        default -> {
          AuroraDuels.INSTANCE.getLogger()
              .warning("Invalid arena creation phase " + creationPhase + "! Resetting to phase 0.");
          creationPhase = 0;
        }
      }
    }
  }

  public void create(Player p) {
    if (creationPhase == 0 && !this.exists()) {
      Wand.setActive("right", true);
      Wand.setActive("left", true);
      spawn1 = null;
      spawn2 = null;
      pos1 = null;
      pos2 = null;
      creationPhase = 1;
      unFinishedArena = this;

      p.getInventory().getItemInMainHand().add();
      p.getInventory().getItemInMainHand().setType(Material.IRON_HOE);
      p.sendMessage(Lang.getComponent("set-spawn1-and-spawn2"));
    } else if (creationPhase != 0) {
      p.sendMessage(Lang.getComponent("arena-already-being-created"));
    } else if (this.exists()) {
      p.sendMessage(Lang.getComponent("arena-already-exists"));
    }
  }

  public void setSpawns(Player p, Action action, BlockVector3 vector) {
    if (action.isLeftClick()) {
      spawn1 = vector;
      p.sendMessage(
          AuroraDuels.COMPONENT_DESERIALIZER.deserialize(Lang.getString("spawn1-set") +
              " (" + spawn1.getBlockX() + ", " + spawn1.getBlockY() + ", " +
              spawn1.getBlockZ() +
              ")"));
      if (spawn2 == null) {
        p.sendMessage(Lang.getComponent("set-spawn2"));
      }
    } else if (action.isRightClick()) {
      spawn2 = vector;
      p.sendMessage(
          AuroraDuels.COMPONENT_DESERIALIZER.deserialize(Lang.getString("spawn2-set") +
              " (" + spawn2.getBlockX() + ", " + spawn2.getBlockY() + ", " +
              spawn2.getBlockZ() + ")"));
      if (spawn1 == null) {
        p.sendMessage(Lang.getComponent("set-spawn1"));
      }
    }
    if (spawn1 != null && spawn2 != null) {
      p.sendMessage(Lang.getComponent("spawns-set"));
      p.sendMessage(Lang.getComponent("set-pos1-and-pos2"));
      world = p.getWorld();
      creationPhase++;
    }
  }

  public void setCorners(Player p, Action action, BlockVector3 vector) {
    if (action.isLeftClick()) {
      pos1 = vector;
      p.sendMessage(
          AuroraDuels.COMPONENT_DESERIALIZER.deserialize(Lang.getString("pos1-set") +
              " (" + pos1.getBlockX() + ", " + pos1.getBlockY() + ", " + pos1.getBlockZ() +
              ")"));
      if (pos2 == null) {
        p.sendMessage(Lang.getComponent("set-pos2"));
      }
    } else if (action.isRightClick()) {
      pos2 = vector;
      p.sendMessage(
          AuroraDuels.COMPONENT_DESERIALIZER.deserialize(Lang.getString("pos2-set") +
              " (" + pos2.getBlockX() + ", " + pos2.getBlockY() + ", " + pos2.getBlockZ() +
              ")"));
      if (pos1 == null) {
        p.sendMessage(Lang.getComponent("set-pos1"));
      }
    }
    if (pos1 != null && pos2 != null) {
      p.sendMessage(Lang.getComponent("arena-created"));
      Wand.setActive("right", false);
      Wand.setActive("left", false);
      schematic.create();
      data.create();
      creationPhase = 0;
      arenas.add(this);
    }
  }

  public void reset(Player p) {
    if (this.exists()) {
      new SchematicData(this).paste();
      p.sendMessage(Lang.getComponent("arena-reset"));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  public void reset() {
    if (this.exists()) {
      new SchematicData(this).paste();
    }
  }

  public void delete(Player p) {
    if (this.exists()) {
      this.getData().getFile().delete();
      this.getSchematic().getFile().delete();
      arenas.remove(this);

      p.sendMessage(Lang.getComponent("arena-deleted"));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  public static void reloadAll() {
    for (Arena arena : getArenas()) {
      arena.reload();
    }

    AuroraDuels.INSTANCE.getLogger().info("ALl arenas reloaded!");
  }
}