package club.aurorapvp.events.wand;

import club.aurorapvp.modules.Wand;
import com.sk89q.worldedit.math.BlockVector3;
import java.io.Serial;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class WandEvent extends EventObject {
  @Serial
  private static final long serialVersionUID = 1L;
  private static final Set<WandListener> LISTENERS = new HashSet<>();
  public WandEvent(Object source) {
    super(source);
  }
  public void trigger(Action action, BlockVector3 vector, Player p) {
    Wand.actions.add(action);
    Wand.vectors.add(vector);

    p.getPlayer().setCooldown(Material.IRON_HOE, 20);

    for (WandListener listener : LISTENERS) {
      listener.onWandEvent(this);
    }
  }

  public void registerListener(WandListener listener) {
    LISTENERS.add(listener);
  }
}
