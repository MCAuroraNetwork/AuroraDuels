package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroracombat.modules.CombatTag;
import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DuelEventListener implements Listener {
  @EventHandler
  public void onDuelStart(DuelStartEvent event) {
    if (!AuroraDuels.isAuroraCombatInstalled()) {
      return;
    }

    for (Player p : event.getDuel().getPlayers()) {
      CombatTag.setTaggable(p, false);
    }
  }

  @EventHandler
  public void onDuelEnd(DuelEndEvent event) {
    if (!AuroraDuels.isAuroraCombatInstalled()) {
      return;
    }

    for (Player p : event.getDuel().getPlayers()) {
      CombatTag.setTaggable(p, true);

      Duel.setLastDuel(p, event.getDuel());
    }

    if (event.getDuel().isRanked() && event.getDuel().getPlayers().size() == 2) {
      Rating.changeRating(event.getLosers()[0], event.getWinners()[0], event.getDuel().getQueue().getName() + "_duels");
    } else if (event.getDuel().isRanked() && event.getDuel().getPlayers().size() > 2) {
      Player[] losers = event.getLosers();
      Player[] winners = event.getWinners();

      double loserAvgRating = Arrays.stream(losers).mapToDouble(p -> Rating.getRating(p, event.getDuel().getQueue().getName() + "_duels")
          .getPoints()).average().orElse(0.0);
      double winnerAvgRating = Arrays.stream(winners).mapToDouble(p -> Rating.getRating(p, event.getDuel().getQueue().getName() + "_duels")
          .getPoints()).average().orElse(0.0);

      for (Player loser : losers) {
        Rating.changeRating(loser, (int) winnerAvgRating, event.getDuel().getQueue().getName() + "_duels");
      }

      for (Player winner : winners) {
        Rating.changeRating(winner, (int) loserAvgRating, event.getDuel().getQueue().getName() + "_duels");
      }
    }

    for (Player p : event.getDuel().getSpectators()) {
      event.getDuel().setSpectator(p, false);
    }
  }
}