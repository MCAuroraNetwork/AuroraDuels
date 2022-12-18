package club.aurorapvp.modules;

import club.aurorapvp.datahandlers.ArenaDataHandler;
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
import static club.aurorapvp.listeners.EventListener.wandEvent;
import static club.aurorapvp.modules.WandModule.WandModule.wandActions;
import static club.aurorapvp.modules.WandModule.WandModule.wandActive;

public class ArenaModule {

  public static void createArena(String arenaName, Player p) throws InterruptedException {
    BlockVector3 pos1 = null;
    BlockVector3 pos2 = null;
    boolean posSet = false;
    wandActive = true;
    Location center;

    p.getInventory().getItemInMainHand().setType(Material.IRON_HOE);
    p.sendMessage(getLangComponent("set-center"));

    synchronized (wandEvent) {
      wandEvent.wait();
    }
    BlockVector3 centerVector = wandActions.values().stream().findFirst().orElse(null);
    center =
        new Location(
            p.getWorld(),
            centerVector.getBlockX(),
            centerVector.getBlockY(),
            centerVector.getBlockZ());


    p.sendMessage(getLangComponent("set-pos1-and-pos2"));
    while (!posSet) {
      synchronized (wandEvent) {
        wandEvent.wait();
      }
      if (wandActions.keySet().stream().findFirst().orElse(null).isLeftClick()) {
        pos1 = wandActions.values().stream().findFirst().orElse(null);
        p.sendMessage(getLangComponent("pos1-set"));
      } else {
        pos2 = wandActions.values().stream().findFirst().orElse(null);
        p.sendMessage(getLangComponent("pos2-set"));
      }
      if (pos1 != null && pos2 != null) {
        posSet = true;
      }
    }
    try {
      saveArenaSchematic(arenaName, BukkitAdapter.adapt(p.getWorld()), pos1, pos2);
      ArenaDataHandler.createArenaData(arenaName, center);
    } catch (WorldEditException | IOException e) {
      throw new RuntimeException(e);
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