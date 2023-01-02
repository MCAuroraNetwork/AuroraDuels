package club.aurorapvp.wand;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.event.block.Action;

import java.io.Serial;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

public class WandEvent extends EventObject {
  @Serial private static final long serialVersionUID = 1L;

  private static Action action;
  private static BlockVector3 vector;
  private static Set<WandListener> listeners = new HashSet<>();

  public WandEvent(Object source) {
    super(source);
  }

  public Action getAction() {
    return action;
  }

  public BlockVector3 getVector() {
    return vector;
  }

  public void trigger(Action action, BlockVector3 vector) {
    WandEvent.action = action;
    WandEvent.vector = vector;
    Wand.actions.put(action, vector);

    for (WandListener listener : listeners) {
      listener.onWandEvent(this);
    }
  }

  public void registerListener(WandListener listener) {
    listeners.add(listener);
  }

  public void removeListener(WandListener listener) {
    listeners.remove(listener);
  }
}
