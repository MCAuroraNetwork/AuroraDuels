package club.aurorapvp.wand;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.bukkit.Material;
import org.bukkit.event.block.Action;

public class Wand {
  public static HashMap<String, Boolean> wandActive = new HashMap<>();
  public static LinkedHashMap<Action, BlockVector3> wandActions = new LinkedHashMap<>();

  public static void setupWand() {
    wandActive.put("right", false);
    wandActive.put("left", false);
  }

  public static int getWandAction(Action action, Material hand, Material block) {
    if (hand.equals(Material.IRON_HOE) && !block.isAir()) {
      if (action.isRightClick()) {
        return 1;
      } else if (action.isLeftClick()) {
        return 2;
      }
    }
    return 0;
  }
}
