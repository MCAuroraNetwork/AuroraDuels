package club.aurorapvp.api.events;

import club.aurorapvp.modules.Duel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is fired after a duel ends
 */
public class DuelEndEvent extends Event {

  public DuelEndEvent(Duel duel) {
    throw new UnsupportedOperationException("This is an API jar for AuroraDuels, used for development purposes only");
  }

  public static HandlerList getHandlerList() {
    throw new UnsupportedOperationException("This is an API jar for AuroraDuels, used for development purposes only");
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    throw new UnsupportedOperationException("This is an API jar for AuroraDuels, used for development purposes only");
  }

  /**
   * Gets the duel that ended
   * @return the duel
   */
  public Duel getDuel() {
    throw new UnsupportedOperationException("This is an API jar for AuroraDuels, used for development purposes only");
  }

  /**
   * Gets one of the players in the duel
   * @return the player
   */
  public Player getPlayerOne() {
    throw new UnsupportedOperationException("This is an API jar for AuroraDuels, used for development purposes only");
  }

  /**
   * Gets one of the players in the duel
   * @return the player
   */
  public Player getPlayerTwo() {
    throw new UnsupportedOperationException("This is an API jar for AuroraDuels, used for development purposes only");
  }

  /**
   * Gets the time elapsed during the duel, in milliseconds
   * @return time elapsed
   */
  public Long timeElapsed() {
    throw new UnsupportedOperationException("This is an API jar for AuroraDuels, used for development purposes only");
  }

  /**
   * Gets the winner of the duel
   * @return the winner
   */
  public Player getWinner() {
    throw new UnsupportedOperationException("This is an API jar for AuroraDuels, used for development purposes only");
  }
}
