package club.aurorapvp.modules;

import club.aurorapvp.datahandlers.ArenaDataHandler;
import club.aurorapvp.wand.WandListener;
import club.aurorapvp.wand.WandEvent;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.IOException;

import static club.aurorapvp.config.LangHandler.getLangComponent;
import static club.aurorapvp.datahandlers.ArenaDataHandler.getArenaDataFile;
import static club.aurorapvp.datahandlers.SchematicsHandler.pasteArenaSchematic;
import static club.aurorapvp.datahandlers.SchematicsHandler.saveArenaSchematic;
import static club.aurorapvp.wand.Wand.wandActions;

public class Arena implements WandListener {
  private static int creationPhase = 0;
  private static BlockVector3 pos1 = null;
  private static BlockVector3 pos2 = null;
  private static Location center = null;
  private static String arenaName;
  private static Player p;

  public static void startArenaCreation(String arena, Player player) {
    if (creationPhase == 0) {
      p = player;
      arenaName = arena;

      creationPhase = 1;
      createArena(arenaName, p);
    } else {
      p.sendMessage(getLangComponent("arena-already-being-created"));
    }
  }

  @Override
  public void onWandEvent(WandEvent event) {
    if (creationPhase != 0 ) {
      createArena(arenaName, p);
    }
  }

  public static void createArena(String arenaName, Player p) {
    switch (creationPhase) {
      case 1 -> {
        p.getInventory().getItemInMainHand().setType(Material.IRON_HOE);
        p.sendMessage(getLangComponent("set-center"));
        creationPhase++;
      }
      case 2 -> {
        BlockVector3 centerVector = wandActions.values().stream().findFirst().orElse(null);
        center =
                new Location(
                        p.getWorld(),
                        centerVector.getBlockX(),
                        centerVector.getBlockY(),
                        centerVector.getBlockZ());

        p.sendMessage(getLangComponent("set-pos1-and-pos2"));
        creationPhase++;
      }
      case 3 -> {
        if (wandActions.keySet().stream().findFirst().orElse(null).isLeftClick()) {
          pos1 = wandActions.values().stream().findFirst().orElse(null);
          p.sendMessage(getLangComponent("pos1-set"));
          p.sendMessage(getLangComponent("set-pos2"));
        } else {
          pos2 = wandActions.values().stream().findFirst().orElse(null);
          p.sendMessage(getLangComponent("pos2-set"));
          p.sendMessage(getLangComponent("set-pos1"));
        }
        if (pos1 != null && pos2 != null) {
          creationPhase++;
        }
      }
      case 4 -> {
        try {
          saveArenaSchematic(arenaName, BukkitAdapter.adapt(p.getWorld()), pos1, pos2);
          ArenaDataHandler.createArenaData(arenaName, center);
        } catch (WorldEditException | IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static void resetArena(String arenaName, Player p) {
    if (getArenaDataFile(arenaName) != null) {
      pasteArenaSchematic(getArenaDataFile(arenaName));
      p.sendMessage(getLangComponent("arena-reset"));
    } else {
      p.sendMessage(getLangComponent("arena-does-not-exist"));
    }
  }

  public static void deleteArena(String arenaName, Player p) {
    if (getArenaDataFile(arenaName) != null) {
      getArenaDataFile(arenaName).delete();
      p.sendMessage(getLangComponent("arena-deleted"));
    } else {
      p.sendMessage(getLangComponent("arena-does-not-exist"));
    }
  }
}