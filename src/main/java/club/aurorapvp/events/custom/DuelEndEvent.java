package club.aurorapvp.events.custom;

import club.aurorapvp.modules.Duel;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is fired after a duel ends
 */
public class DuelEndEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private final Duel duel;

  public DuelEndEvent(Duel duel) {
    this.duel = duel;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  public Duel getDuel() {
    return duel;
  }

  public Long timeElapsed() {
    return duel.getElapsed();
  }

  public Set<Player> getLosers() {
    Set<Player> losers = new HashSet<>();

    for (Player p : duel.getPlayers()) {
      if (p.isDead()) {
        losers.add(p);
      }
    }

    return losers;
  }

  public Set<Player> getWinners() {
    Set<Player> winners = new HashSet<>();

    for (Player p : duel.getPlayers()) {
      if (!p.isDead()) {
        winners.add(p);
      }
    }

    return winners;
  }
}