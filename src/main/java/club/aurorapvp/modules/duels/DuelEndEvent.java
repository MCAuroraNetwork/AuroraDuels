package club.aurorapvp.modules.duels;

import club.aurorapvp.modules.Queue;
import java.io.Serial;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;

public class DuelEndEvent extends EventObject {
  @Serial
  private static final long serialVersionUID = 1L;
  private static final Set<DuelEndEventListener> LISTENERS = new HashSet<>();
  public DuelEndEvent(Object source) {
    super(source);
  }
  public void trigger(Player p1, Player p2) {
    Duels.playersInDuel.remove(p1);
    Duels.playersInDuel.remove(p2);

    for (DuelEndEventListener listener : LISTENERS) {
      listener.onDuelsEvent(this);
    }
  }

  public void registerListener(DuelEndEventListener listener) {
    LISTENERS.add(listener);
  }
}
