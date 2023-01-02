package club.aurorapvp.wand;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.event.block.Action;

import java.io.Serial;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

public class WandEvent extends EventObject {
  @Serial private static final long serialVersionUID = 1L;
  private static final Set<WandListener> LISTENERS = new HashSet<>();

  public WandEvent(Object source) {
    super(source);
  }
  public void trigger(Action action, BlockVector3 vector) {
    Wand.actions.add(action);
    Wand.vectors.add(vector);

    for (WandListener listener : LISTENERS) {
      listener.onWandEvent(this);
    }
  }
  public void registerListener(WandListener listener) {
    LISTENERS.add(listener);
  }
}
