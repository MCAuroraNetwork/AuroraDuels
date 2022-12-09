package club.aurorapvp.modules.WandModule;

import static club.aurorapvp.config.LangHandler.getLangComponent;
import static club.aurorapvp.datahandlers.SchematicHandler.saveArenaSchematic;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import java.io.IOException;
import java.util.LinkedHashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class WandModule {
  public static boolean wandActive = false;
  public static LinkedHashMap<Action, BlockVector3> wandActions = new LinkedHashMap<>();
  WandUseEvent wandEvent = new WandUseEvent(this);


  public void createArena(String arenaName, Player p) throws InterruptedException {
    WandUseEvent wandEvent = new WandUseEvent(this);
    BlockVector3 center = null;
    BlockVector3 pos1 = null;
    BlockVector3 pos2 = null;
    boolean posSet = false;
    wandActive = true;

    p.sendMessage(getLangComponent("set-center"));

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
      if (!pos1.equals(null) && !pos2.equals(null)) {
        posSet = true;
      }
    }
    try {
      saveArenaSchematic(arenaName, BukkitAdapter.adapt(p.getWorld()), pos1, pos2);
    } catch (WorldEditException | IOException e) {
      throw new RuntimeException();
    }
  }

  public static int getWandAction(Action action, Material hand, Material block) {
    if (hand.equals(Material.WOODEN_HOE) && !block.isAir()) {
      if (action.isRightClick()) {
        return 1;
      } else if (action.isLeftClick()) {
        return 2;
      }
    }
    return 0;
  }
}
