package club.aurorapvp.modules;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.config.Lang;
import club.aurorapvp.datahandlers.ArenaData;
import club.aurorapvp.datahandlers.Schematics;
import club.aurorapvp.wand.Wand;
import club.aurorapvp.wand.WandEvent;
import club.aurorapvp.wand.WandListener;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import java.io.IOException;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Arena implements WandListener {
  private static int creationPhase = 0;
  private static BlockVector3 spawn1 = null;
  private static BlockVector3 spawn2 = null;
  private static BlockVector3 pos1 = null;
  private static BlockVector3 pos2 = null;
  private static String arenaName;
  private static World arenaWorld;
  private static Player p;

  public static void create(String arena, Player p) {
    if (creationPhase == 0 && ArenaData.get(arenaName) == null) {
      Arena.p = p;
      arenaName = arena;
      Wand.setActive("right", true);
      Wand.setActive("left", true);
      spawn1 = null;
      spawn2 = null;
      pos1 = null;
      pos2 = null;

      creationPhase = 1;
      run(arenaName, p);
    } else if (creationPhase != 0) {
      p.sendMessage(Lang.getComponent("arena-already-being-created"));
    } else if (ArenaData.get(arenaName) != null) {
      p.sendMessage(Lang.getComponent("arena-already-exists"));
    }
  }

  @Override
  public void onWandEvent(WandEvent event) {
    if (creationPhase != 0) {
      run(arenaName, p);
    }
  }

  private static void run(String arenaName, Player p) {
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
              AuroraDuels.COMPONENT_DESERIALIZER.deserialize(Lang.getComponent("spawn1-set") +
                  " (" + spawn1.getBlockX() + ", " + spawn1.getBlockY() + ", " +
                  spawn1.getBlockZ() +
                  ")"));
          if (spawn2 == null) {
            p.sendMessage(Lang.getComponent("set-spawn2"));
          }
        } else if (Wand.getLastAction().isRightClick()) {
          spawn2 = Wand.getLastVector();
          p.sendMessage(
              AuroraDuels.COMPONENT_DESERIALIZER.deserialize(Lang.getComponent("spawn2-set") +
                  " (" + spawn2.getBlockX() + ", " + spawn2.getBlockY() + ", " +
                  spawn2.getBlockZ() + ")"));
          if (spawn1 == null) {
            p.sendMessage(Lang.getComponent("set-spawn1"));
          }
        }
        if (spawn1 != null && spawn2 != null) {
          Lang.getComponent("spawns-set");
          Lang.getComponent("set-pos1-and-pos2");
          arenaWorld = p.getWorld();
          creationPhase++;
        }
      }
      case 3 -> {
        if (Wand.getLastAction().isLeftClick()) {
          pos1 = Wand.getLastVector();
          p.sendMessage(
              AuroraDuels.COMPONENT_DESERIALIZER.deserialize(Lang.getComponent("pos1-set") +
                  " (" + pos1.getBlockX() + ", " + pos1.getBlockY() + ", " + pos1.getBlockZ() +
                  ")"));
          if (pos2 == null) {
            p.sendMessage(Lang.getComponent("set-pos2"));
          }
        } else if (Wand.getLastAction().isRightClick()) {
          pos2 = Wand.getLastVector();
          p.sendMessage(
              AuroraDuels.COMPONENT_DESERIALIZER.deserialize(Lang.getComponent("pos2-set") +
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

  public static void reset(String arenaName, Player p) {
    if (ArenaData.get(arenaName) != null) {
      Schematics.paste(arenaName);
      p.sendMessage(Lang.getComponent("arena-reset"));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  public static void delete(String arenaName, Player p) {
    if (ArenaData.get(arenaName) != null) {
      ArenaData.get(arenaName).delete();
      p.sendMessage(Lang.getComponent("arena-deleted"));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }
}