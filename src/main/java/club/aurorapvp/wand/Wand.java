package club.aurorapvp.wand;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.HashMap;
import org.bukkit.event.block.Action;
import org.apache.commons.collections4.map.LinkedMap;

public class Wand {
  public static HashMap<String, Boolean> active = new HashMap<>();
  public static LinkedMap<Action, BlockVector3> actions = new LinkedMap<>();
  public static void setup() {
    active.put("right", false);
    active.put("left", false);
  }
}
