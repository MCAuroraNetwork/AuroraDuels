package club.aurorapvp.wand;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.HashMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.bukkit.event.block.Action;

public class Wand {
  private static final HashMap<String, Boolean> ACTIVE = new HashMap<>();
  public static LinkedMap<Action, BlockVector3> actions = new LinkedMap<>();
  public static void setup() {
    ACTIVE.put("right", false);
    ACTIVE.put("left", false);
  }
  public static Action getLastAction() {
    return Wand.actions.lastKey();
  }
  public static BlockVector3 getLastVector() {
    return Wand.actions.get(Wand.actions.lastKey());
  }
  public static boolean isActive(Action action) {
    return (ACTIVE.get("left") && action.isLeftClick()) || (ACTIVE.get("right") && action.isRightClick());
  }
  public static void setActive(String type, boolean value) {
    if (type.equals("left") || type.equals("right")) {
      ACTIVE.put(type, value);
    }
  }
}