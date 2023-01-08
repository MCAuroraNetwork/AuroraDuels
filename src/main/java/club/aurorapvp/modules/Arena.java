package club.aurorapvp.modules;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.datahandlers.ArenaData;
import club.aurorapvp.datahandlers.Schematics;
import club.aurorapvp.events.wand.WandEvent;
import club.aurorapvp.events.wand.WandListener;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import java.io.IOException;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Arena implements WandListener, ArenaInterface {
  private static BlockVector3 spawn1 = null;
  private static BlockVector3 spawn2 = null;
  private static BlockVector3 pos1 = null;
  private static BlockVector3 pos2 = null;
  private static int creationPhase = 0;
  private boolean availability = true;
  private static String arenaName;
  private static World arenaWorld;
  private static Player p;

  public Arena(String arenaName) {
    Arena.arenaName = arenaName;
  }

  @Override
  public BlockVector3 getSpawn(int num) {
    switch (num) {
      case 1 -> {
        return spawn1;
      }
      case 2 -> {
        return spawn2;
      }
      default -> {
        return null;
      }
    }
  }

  @Override
  public BlockVector3 getFirstPosition() {
    return pos1;
  }

  @Override
  public BlockVector3 getSecondPosition() {
    return pos2;
  }

  @Override
  public World getWorld() {
    return arenaWorld;
  }

  @Override
  public String getName() {
    return arenaName;
  }

  @Override
  public boolean exists() {
    return (this.getData().getFile() != null && this.getSchematic().getFormat() != null);
  }

  @Override
  public boolean inUse() {
    return availability;
  }

  @Override
  public void setAvailability(boolean value) {
    availability = value;
  }

  @Override
  public ArenaData getData() {
    return new ArenaData(this);
  }

  @Override
  public Schematics getSchematic() {
    return new Schematics(this);
  }


  public void create(Player p, Arena arena) {
    if (creationPhase == 0 && !arena.exists()) {
      Arena.p = p;
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
    } else if (arena.exists()) {
      p.sendMessage(Lang.getComponent("arena-already-exists"));
    }
  }

  @Override
  public void onWandEvent(WandEvent event) {
    if (creationPhase != 0) {
      switch (creationPhase) {
        case 1 -> setSpawns(p);
        case 2 -> setCorners(p);
        default -> {
          AuroraDuels.PLUGIN.getLogger()
              .warning("Invalid arena creation phase " + creationPhase + "! Resetting to phase 0.");
          creationPhase = 0;
        }
      }
    }
  }

  public void setSpawns(Player p) {
    if (Wand.getLastAction().isLeftClick()) {
      spawn1 = Wand.getLastVector();
      p.sendMessage(
          AuroraDuels.COMPONENT_DESERIALIZER.deserialize(Lang.getString("spawn1-set") +
              " (" + spawn1.getBlockX() + ", " + spawn1.getBlockY() + ", " +
              spawn1.getBlockZ() +
              ")"));
      if (spawn2 == null) {
        p.sendMessage(Lang.getComponent("set-spawn2"));
      }
    } else if (Wand.getLastAction().isRightClick()) {
      spawn2 = Wand.getLastVector();
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
      arenaWorld = p.getWorld();
      creationPhase++;
    }
  }

  public void setCorners(Player p) {
    if (Wand.getLastAction().isLeftClick()) {
      pos1 = Wand.getLastVector();
      p.sendMessage(
          AuroraDuels.COMPONENT_DESERIALIZER.deserialize(Lang.getString("pos1-set") +
              " (" + pos1.getBlockX() + ", " + pos1.getBlockY() + ", " + pos1.getBlockZ() +
              ")"));
      if (pos2 == null) {
        p.sendMessage(Lang.getComponent("set-pos2"));
      }
    } else if (Wand.getLastAction().isRightClick()) {
      pos2 = Wand.getLastVector();
      p.sendMessage(
          AuroraDuels.COMPONENT_DESERIALIZER.deserialize(Lang.getString("pos2-set") +
              " (" + pos2.getBlockX() + ", " + pos2.getBlockY() + ", " + pos2.getBlockZ() +
              ")"));
      if (pos1 == null) {
        p.sendMessage(Lang.getComponent("set-pos1"));
      }
    }
    if (pos1 != null && pos2 != null) {
      new Schematics(this).save();
      new ArenaData(this).create();
      p.sendMessage(Lang.getComponent("arena-created"));
      creationPhase = 0;
      Wand.setActive("right", false);
      Wand.setActive("left", false);
      Duels.arenas.add(this);
    }
  }

  public void reset(Player p, Arena arena) {
    if (arena.exists()) {
      new Schematics(this).paste();
      p.sendMessage(Lang.getComponent("arena-reset"));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  public static void delete(Player p, Arena arena) {
    if (arena.exists()) {
      arena.getData().getFile().delete();
      arena.getSchematic().getFile().delete();
      Duels.arenas.remove(arena);
      p.sendMessage(Lang.getComponent("arena-deleted"));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }
}

interface ArenaInterface {
  BlockVector3 getSpawn(int num);
  BlockVector3 getFirstPosition();
  BlockVector3 getSecondPosition();
  World getWorld();
  String getName();
  boolean exists();
  boolean inUse();
  void setAvailability(boolean value);
  ArenaData getData();
  Schematics getSchematic();
}