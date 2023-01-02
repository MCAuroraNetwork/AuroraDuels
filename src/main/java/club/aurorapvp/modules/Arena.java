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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;

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
    if (creationPhase == 0) {
      Arena.p = p;
      arenaName = arena;
      Wand.setActive("right", true);
      Wand.setActive("left", true);

      creationPhase = 1;
      run(arenaName, p);
    } else {
      p.sendMessage(Lang.getComponent("arena-already-being-created"));
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
          p.sendMessage(Lang.getComponent("spawn1-set"));
          if (spawn2 == null) {
            p.sendMessage(Lang.getComponent("set-spawn2"));

          }
        } else if (Wand.getLastAction().isRightClick()) {
          spawn2 = Wand.getLastVector();
          p.sendMessage(Lang.getComponent("spawn2-set"));
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
        p.sendMessage(Lang.getComponent("pos1-set"));
        p.sendMessage(Lang.getComponent("set-pos2"));
      } else if (Wand.getLastAction().isRightClick()) {
        pos2 = Wand.getLastVector();
        p.sendMessage(Lang.getComponent("pos2-set"));
        p.sendMessage(Lang.getComponent("set-pos1"));
      }
      if (pos1 != null && pos2 != null) {
        creationPhase++;
      }
    }
      case 4 -> {
        try {
          Schematics.save(arenaName, BukkitAdapter.adapt(p.getWorld()), pos1, pos2);
          ArenaData.create(arenaName, arenaWorld, spawn1, spawn2);
        } catch (WorldEditException | IOException e) {
          throw new RuntimeException(e);
        }
        creationPhase = 0;
      }
      default -> {
        AuroraDuels.PLUGIN.getLogger().warning("Invalid arena creation phase " + creationPhase + "! Resetting to phase 0.");
        creationPhase = 0;
      }
    }
  }

  public static void reset(String arenaName, Player p) {
    if (ArenaData.get(arenaName) != null) {
      Schematics.paste(ArenaData.get(arenaName));
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