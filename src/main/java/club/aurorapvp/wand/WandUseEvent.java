package club.aurorapvp.wand;

import static club.aurorapvp.wand.Wand.wandActions;

import com.sk89q.worldedit.math.BlockVector3;
import java.io.Serial;
import java.util.EventObject;

import org.bukkit.event.block.Action;

public class WandUseEvent extends EventObject {
  @Serial
  private static final long serialVersionUID = 1L;

  private Action wandAction;
  private BlockVector3 wandVector;

  public WandUseEvent(Object source) {
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
  }
}