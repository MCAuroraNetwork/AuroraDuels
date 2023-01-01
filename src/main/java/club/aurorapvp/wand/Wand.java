package club.aurorapvp.wand;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.bukkit.event.block.Action;

public class Wand {
  public static HashMap<String, Boolean> active = new HashMap<>();
  public static LinkedHashMap<Action, BlockVector3> actions = new LinkedHashMap<>();

  public static void setup() {
    active.put("right", false);
    active.put("left", false);
  }
}
