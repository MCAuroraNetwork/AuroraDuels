package club.aurorapvp.events.custom;

import club.aurorapvp.modules.Duel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is fired after a duel ends
 */
public class DuelStartEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private final Duel duel;
  private final Player playerOne;
  private final Player playerTwo;

  public DuelStartEvent(Duel duel) {
    this.duel = duel;
    this.playerOne = duel.getPlayerOne();
    this.playerTwo = duel.getPlayerTwo();
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

  public Player getPlayerOne() {
    return playerOne;
  }

  public Player getPlayerTwo() {
    return playerTwo;
  }
}
