package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.modules.duels.Duel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

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

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        for (Player p : duel.getPlayers()) {
          p.teleportAsync(duel.getRespawnLocation());
        }
        duel.getArena().resetAsync();
        duel.getArena().setOccupied(false);
        this.cancel();
      }
    }, 3000);

    for (Player p : winners) {
      Duel.addWin(p);
    }

    for (Player p : losers) {
      Duel.resetStreak(p);
    }

    duel.setActive(false);
  }

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
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