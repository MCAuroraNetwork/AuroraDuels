package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired after a duel ends */
public class DuelEndEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Player[] winners;
  private final Player[] losers;
  private final Duel duel;

  public DuelEndEvent(Duel duel, Player[] winners, Player[] losers) {
    this.duel = duel;
    this.winners = winners;
    this.losers = losers;

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                for (Player player : duel.getPlayers()) {
                  player.teleportAsync(duel.getRespawnLocation());
                }
                duel.getArena().reset();
                duel.getArena().setOccupied(false);
                this.cancel();
              }
            },
            3000);

    for (Player player : duel.getSpectators()) {
      duel.setSpectator(player, false);
    }

    if (duel.isRanked()) {
      AuroraDuels.INSTANCE.getLogger().log(Level.INFO, "joe1");

      for (Player player : this.getWinners()) {
        AuroraDuels.INSTANCE.getLogger().log(Level.INFO, "joe2");
        Duel.addWin(player);
      }

      for (Player player : this.getLosers()) {
        Duel.resetStreak(player);
      }
    }

    duel.setActive(false);

    if (duel instanceof TeamDuel teamDuel) {
      for (Player player : teamDuel.getTeam1Members()) {
        TeamDuel.team1.removePlayer(player);
      }

      for (Player player : teamDuel.getTeam2Members()) {
        TeamDuel.team2.removePlayer(player);
      }
    }
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
