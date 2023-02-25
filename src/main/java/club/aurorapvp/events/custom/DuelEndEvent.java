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

  public Duel getDuel() {
    return duel;
  }

  public Player getPlayerOne() {
    return playerOne;
  }

  public Player getPlayerTwo() {
    return playerTwo;
  }

  public Long timeElapsed() {
    return duel.getElapsed();
  }

  public Player getLoser() {
    if (playerOne.isDead() || !playerOne.isOnline()) {
      return playerOne;
    } else if (playerTwo.isDead() || !playerTwo.isOnline()) {
      return playerTwo;
    } else {
      return null;
    }
  }

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
