package club.aurorapvp.modules;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.datahandlers.ArenaData;
import club.aurorapvp.datahandlers.Schematic;
import club.aurorapvp.events.WandEvent;
import com.sk89q.worldedit.math.BlockVector3;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

public class Arena implements Listener {
  private static final List<Arena> arenas = new ArrayList<>();
  @Getter private BlockVector3 spawn1 = null;
  @Getter private BlockVector3 spawn2 = null;
  @Getter private BlockVector3 pos1 = null;
  @Getter private BlockVector3 pos2 = null;
  private static int creationPhase = 0;
  @Setter
  @Getter private boolean occupied = false;
  @Getter private final Schematic schematic;
  @Getter private final ArenaData data;
  @Getter private final String name;
  @Getter private World world;

  public Arena(String arenaName) {
    this.name = arenaName;

    this.schematic = new Schematic(this);
    this.data = new ArenaData(this);

    if (this.exists()) {
      this.reload();
    }
  }

  public Arena() {
    this.name = null;
    this.schematic = null;
    this.data = null;
  }

  public boolean exists() {
    return this.getData().getFile().exists() && this.getSchematic().getFile().exists();
  }

  public static Arena[] getArenas() {
    Arena[] arenaArray = new Arena[arenas.size()];

    for (int i = 0; i < arenas.size(); i++) {
      arenaArray[i] = arenas.get(i);
    }

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

  public void create(Player p) {
    if (creationPhase == 0 && !this.exists()) {
      Wand.setActive("right", true);
      Wand.setActive("left", true);
      spawn1 = null;
      spawn2 = null;
      pos1 = null;
      pos2 = null;
      creationPhase = 1;

      p.getInventory().getItemInMainHand().setType(Material.IRON_HOE);
      p.sendMessage(Lang.getComponent("set-spawn1-and-spawn2"));
    } else if (creationPhase != 0) {
      p.sendMessage(Lang.getComponent("arena-already-being-created"));
    } else if (this.exists()) {
      p.sendMessage(Lang.getComponent("arena-already-exists"));
    }
  }

  @EventHandler
  public void onWandEvent(WandEvent event) {
    Player p = event.getPlayer();
    Action action = event.getAction();
    BlockVector3 vector = event.getVector();

    if (creationPhase != 0) {
      switch (creationPhase) {
        case 1 -> setSpawns(p, action, vector);
        case 2 -> setCorners(p, action, vector);
        default -> {
          AuroraDuels.INSTANCE.getLogger()
              .warning("Invalid arena creation phase " + creationPhase + "! Resetting to phase 0.");
          creationPhase = 0;
        }
      }
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
      new Schematic(this).paste();
      p.sendMessage(Lang.getComponent("arena-reset"));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  public void reset() {
    if (this.exists()) {
      new Schematic(this).paste();
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

  public static void setup() {
    File dir = new File(AuroraDuels.DATA_FOLDER, "/arenas/");
    for (File file : dir.listFiles()) {
      Arena arena = new Arena(file.getName().replace(".yml", ""));
      if (arena.exists()) {
        arenas.add(arena);
      }
    }
    AuroraDuels.INSTANCE.getLogger().info("All arenas loaded!");
  }
}