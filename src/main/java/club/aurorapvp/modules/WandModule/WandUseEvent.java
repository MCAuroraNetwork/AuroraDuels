package club.aurorapvp.modules.WandModule;

import static club.aurorapvp.modules.WandModule.WandModule.wandActions;

import com.sk89q.worldedit.math.BlockVector3;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import org.bukkit.event.block.Action;

public class WandUseEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public WandUseEvent(Object source) {
    super(source);
  }

  private static List<WandListener> listeners = new ArrayList<>();

  public void addListener(WandListener listener) {
    listeners.add(listener);
  }

  public void trigger(Action action, BlockVector3 pos) {
    for (WandListener listener : listeners) {
      listener.handleMyEvent(this, action, pos);
      wandActions.put(action, pos);
    }
  }
}

interface WandListener {
  void handleMyEvent(WandUseEvent event, Action action, BlockVector3 pos);
}