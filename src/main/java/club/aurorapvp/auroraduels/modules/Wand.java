package club.aurorapvp.auroraduels.modules;

import org.bukkit.event.block.Action;

public class Wand {

  private static WandActivity activity;

  public static void init() {
    activity = WandActivity.INACTIVE;
  }

  public static boolean isActive(Action action) {
    return ((activity == WandActivity.LEFT && action.isLeftClick())
        || (activity == WandActivity.RIGHT && action.isRightClick())
        || activity == WandActivity.BOTH);
  }

  public static void setActive(WandActivity value) {
    activity = value;
  }

  public enum WandActivity {
    RIGHT,
    LEFT,
    BOTH,
    INACTIVE
  }
}
