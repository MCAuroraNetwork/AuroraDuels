package club.aurorapvp.modules.arenas;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.datahandlers.ArenaData;
import club.aurorapvp.datahandlers.Schematics;
import club.aurorapvp.modules.Wand;
import club.aurorapvp.events.wand.WandEvent;
import club.aurorapvp.events.wand.WandListener;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
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
  private boolean availibility = true;
  private static int creationPhase = 0;
  private static String arenaName;
  private static World arenaWorld;
  private static Player p;

  public Arena(String arenaName) {
    Arena.arenaName = arenaName;
  }
  @Override
  public String getName() {
    return arenaName;
  }

  @Override
  public boolean exists() {
    return (ArenaData.get(this) != null && Schematics.get(this) != null);
  }

  @Override
  public boolean inUse() {
    return availibility;
  }

  @Override
  public void setAvailibility(boolean value) {
    availibility = value;
  }

  public static void create(Player p, Arena arena) {
    if (creationPhase == 0 && ArenaData.get(arena) == null) {
      Arena.p = p;
      Wand.setActive("right", true);
      Wand.setActive("left", true);
      spawn1 = null;
      spawn2 = null;
      pos1 = null;
      pos2 = null;

      creationPhase = 1;
      run(p, arenaName);
    } else if (creationPhase != 0) {
      p.sendMessage(Lang.getComponent("arena-already-being-created"));
    } else if (ArenaData.get(arena) != null) {
      p.sendMessage(Lang.getComponent("arena-already-exists"));
    }
  }

  @Override
  public void onWandEvent(WandEvent event) {
    if (creationPhase != 0) {
      run(p, arenaName);
    }
  }

  private static void run(Player p, String arena) {
    switch (creationPhase) {
      case 1 -> {
        p.getInventory().getItemInMainHand().setType(Material.IRON_HOE);
        p.sendMessage(Lang.getComponent("set-spawn1-and-spawn2"));
        creationPhase++;
      }
      case 2 -> {
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
      case 3 -> {
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
          try {
            Schematics.save(arenaName, BukkitAdapter.adapt(p.getWorld()), pos1, pos2);
            ArenaData.create(arenaName, arenaWorld, spawn1, spawn2);
            p.sendMessage(Lang.getComponent("arena-created"));
          } catch (WorldEditException | IOException e) {
            throw new RuntimeException(e);
          }
          creationPhase = 0;
          Wand.setActive("right", false);
          Wand.setActive("left", false);
        }
      }
      default -> {
        AuroraDuels.PLUGIN.getLogger()
            .warning("Invalid arena creation phase " + creationPhase + "! Resetting to phase 0.");
        creationPhase = 0;
      }
    }
  }

  public static void reset(Player p, Arena arena) {
    if (arena.exists()) {
      Schematics.paste(arena);
      p.sendMessage(Lang.getComponent("arena-reset"));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  public static void delete(Player p, Arena arena) {
    if (arena.exists()) {
      ArenaData.get(arena).delete();
      Schematics.get(arena).delete();
      p.sendMessage(Lang.getComponent("arena-deleted"));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }
}