package club.aurorapvp.events.custom;

import club.aurorapvp.modules.Duel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is fired after a duel ends
 */
public class DuelEndEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();

  private final Player[] winners;
  private final Player[] losers;
  private final Duel duel;
  public DuelEndEvent(Duel duel, Player[] winners, Player[] losers) {
    this.duel = duel;
    this.winners = winners;
    this.losers = losers;
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

  public Player[] getLosers() {
    return losers;
  }

  public Player[] getWinners() {
    return winners;
  }
}