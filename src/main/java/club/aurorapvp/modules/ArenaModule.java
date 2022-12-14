package club.aurorapvp.modules;

import static club.aurorapvp.config.LangHandler.getLangComponent;
import static club.aurorapvp.datahandlers.ArenaDataHandler.saveArenaData;
import static club.aurorapvp.datahandlers.SchematicHandler.saveArenaSchematic;
import static club.aurorapvp.listeners.EventListener.wandEvent;
import static club.aurorapvp.modules.WandModule.WandModule.wandActions;
import static club.aurorapvp.modules.WandModule.WandModule.wandActive;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import java.io.IOException;
import org.bukkit.entity.Player;

public class ArenaModule {
  public static void createArena(String arenaName, Player p) throws InterruptedException {
    BlockVector3 minPoint = null;
    BlockVector3 pos1 = null;
    BlockVector3 pos2 = null;
    boolean posSet = false;
    wandActive = true;

    p.sendMessage(getLangComponent("set-center"));

    while (!posSet) {
      wandEvent.wait();
      minPoint = wandActions.values().stream().findFirst().orElse(null);
      if (minPoint != null) {
        posSet = true;
      }
    }
    posSet = false;

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
      saveArenaData(arenaName, p.getWorld(), minPoint);
    } catch (WorldEditException | IOException e) {
      throw new RuntimeException();
    }
  }
}