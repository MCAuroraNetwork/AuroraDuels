package club.aurorapvp.modules;

import club.aurorapvp.datahandlers.ArenaDataHandler;
import club.aurorapvp.datahandlers.SchematicsHandler;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static club.aurorapvp.AuroraDuels.DataFolder;
import static club.aurorapvp.config.ConfigHandler.getConfig;
import static club.aurorapvp.config.LangHandler.getLangComponent;
import static club.aurorapvp.datahandlers.SchematicsHandler.pasteArenaSchematic;
import static club.aurorapvp.datahandlers.SchematicsHandler.saveArenaSchematic;
import static club.aurorapvp.listeners.EventListener.wandEvent;
import static club.aurorapvp.modules.WandModule.WandModule.wandActions;
import static club.aurorapvp.modules.WandModule.WandModule.wandActive;

public class ArenaModule {

  public static void setupArenaLogic() {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Runnable clearViolations = ArenaModule::resetArenas;
    executor.scheduleAtFixedRate(
        clearViolations, 0, getConfig().getInt("arenas.reset.interval"), TimeUnit.MINUTES);
  }

  public static void createArena(String arenaName, Player p) throws InterruptedException {
    BlockVector3 pos1 = null;
    BlockVector3 pos2 = null;
    boolean posSet = false;
    wandActive = true;
    Location center;

    p.sendMessage(getLangComponent("set-center"));

    // Sets center value as a Location

    wandEvent.wait();
    BlockVector3 centerVector = wandActions.values().stream().findFirst().orElse(null);
    center =
        new Location(
            p.getWorld(),
            centerVector.getBlockX(),
            centerVector.getBlockY(),
            centerVector.getBlockZ());

    // Sets position 1 and 2 when wand event is fired

    p.sendMessage(getLangComponent("set-pos1-and-pos2"));
    while (!posSet) {
      wandEvent.wait();
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

  public static void resetArenas() {
    for (File arenaData : new File(DataFolder, "/arenas/").listFiles()) {
      pasteArenaSchematic(arenaData);
    }
  }
}