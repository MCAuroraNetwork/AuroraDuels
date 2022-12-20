package club.aurorapvp.wand;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.event.block.Action;

import java.io.Serial;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import static club.aurorapvp.wand.Wand.wandActions;

public class WandEvent extends EventObject {
  @Serial private static final long serialVersionUID = 1L;

  private Action wandAction;
  private BlockVector3 wandVector;
  private Set<WandListener> listeners = new HashSet<>();

  public WandEvent(Object source) {
    super(source);
  }

  public Action getWandAction() {
    return wandAction;
  }

  public BlockVector3 getWandVector() {
    return wandVector;
  }

  public void trigger(Action action, BlockVector3 pos) {
    wandAction = action;
    wandVector = pos;
    wandActions.put(action, pos);

    for (WandListener listener : listeners) {
      listener.onWandEvent(this);
    }
  }

  public void addWandListener(WandListener listener) {
    listeners.add(listener);
  }

  public void removeWandListener(WandListener listener) {
    listeners.remove(listener);
  }
}
