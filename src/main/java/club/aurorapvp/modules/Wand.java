package club.aurorapvp.modules;

import java.util.HashMap;
import org.bukkit.event.block.Action;

public class Wand {
  private static final HashMap<String, Boolean> ACTIVE = new HashMap<>();

  public static void setup() {
    ACTIVE.put("right", false);
    ACTIVE.put("left", false);
  }

  public static boolean isActive(Action action) {
    return (ACTIVE.get("left") && action.isLeftClick()) ||
        (ACTIVE.get("right") && action.isRightClick());
  }

  public static void setActive(String type, boolean value) {
    if (type.equals("left") || type.equals("right")) {
      ACTIVE.put(type, value);
    }
  }
}