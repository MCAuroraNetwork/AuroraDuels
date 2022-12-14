package club.aurorapvp.modules.WandModule;

import com.sk89q.worldedit.math.BlockVector3;
import java.util.LinkedHashMap;
import org.bukkit.Material;
import org.bukkit.event.block.Action;

public class WandModule {
  public static boolean wandActive = false;
  public static LinkedHashMap<Action, BlockVector3> wandActions = new LinkedHashMap<>();
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
