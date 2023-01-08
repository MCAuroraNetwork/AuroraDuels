package club.aurorapvp.events.duels;

import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Duels;
import java.io.Serial;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;

public class DuelStartEvent extends EventObject {
  @Serial
  private static final long serialVersionUID = 1L;
  private static final Set<DuelStartEventListener> LISTENERS = new HashSet<>();
  public DuelStartEvent(Object source) {
    super(source);
  }
  public void trigger(Player p1, Player p2, Arena arena) {
    Duels.playersInDuel.add(p1);
    Duels.playersInDuel.add(p2);
    arena.setOccupied(true);

    for (DuelStartEventListener listener : LISTENERS) {
      listener.onDuelStartEvent(this);
    }
  }

  public void registerListener(DuelStartEventListener listener) {
    LISTENERS.add(listener);
  }
}
