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
  private static final HandlerList HANDLERS = new HandlerList();
  private final Duel duel;
  private final Player playerOne;
  private final Player playerTwo;

  public DuelEndEvent(Duel duel) {
    this.duel = duel;
    this.playerOne = duel.getPlayerOne();
    this.playerTwo = duel.getPlayerTwo();

    duel.endDuel();
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  /**
   * Gets the duel that ended
   * @return the duel
   */
  public Duel getDuel() {
    return duel;
  }

  /**
   * Gets one of the players in the duel
   * @return the player
   */
  public Player getPlayerOne() {
    return playerOne;
  }

  /**
   * Gets one of the players in the duel
   * @return the player
   */
  public Player getPlayerTwo() {
    return playerTwo;
  }

  /**
   * Gets the time elapsed during the duel, in milliseconds
   * @return time elapsed
   */
  public Long timeElapsed() {
    return duel.getElapsed();
  }

  /**
   * Gets the winner of the duel
   * @return the winner
   */
  public Player getWinner() {
    if (playerOne.isDead() || !playerOne.isOnline()) {
      return playerTwo;
    } else if (playerTwo.isDead() || !playerTwo.isOnline()) {
      return playerOne;
    } else {
      return null;
    }
  }
}
