package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroracombat.AuroraCombat;
import club.aurorapvp.auroracombat.modules.CombatTag;
import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.DuelDrawEvent;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import club.aurorapvp.aurorawarps.events.custom.NewDeathLocationEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DuelEventListener implements Listener {

  @EventHandler
  public void onNewDeathLocation(NewDeathLocationEvent event) {
    for (Arena arena : Arena.getArenas()) {
      if (arena.isInBounds(event.getLocation())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onDuelStart(DuelStartEvent event) {
    if (!AuroraDuels.getInstance().isAuroraCombatInstalled()) {
      return;
    }

    Rating rating = Rating.getRating("overall");

    for (Player player : event.getDuel().getPlayers()) {
      rating.setPlayerEnabled(player, false);

      CombatTag.setTaggable(player, false);
    }
  }

  @EventHandler
  public void onDuelEnd(DuelEndEvent event) {
    for (Player player : event.getDuel().getPlayers()) {
      Duel.setLastDuel(player, event.getDuel());

      player.setGlowing(false);

      player.sendMessage(
          AuroraDuels.getInstance()
              .getLang()
              .getComponentCommand("rematch", "Click to request rematch", "/rematch"));
    }

    if (AuroraDuels.getInstance().isAuroraKitsInstalled()) {
      for (Player player : event.getDuel().getPlayers()) {
        Kit.refillKit(player);
      }
    }

    if (!AuroraDuels.getInstance().isAuroraCombatInstalled()) {
      return;
    }

    for (Player player : event.getDuel().getPlayers()) {
      Rating.getRating("overall").setPlayerEnabled(player, true);

      CombatTag.setTaggable(player, true);
    }

    if (!event.getDuel().isRanked()) {
      return;
    }

    String ratingName = event.getDuel().getQueue().getName() + "_duels";

    Collection<Player> winners = event.getWinners();
    Collection<Player> losers = event.getLosers();

    Map<Rating, Integer> updatedRatings = new HashMap<>();

    updatedRatings.put(Rating.getRating("overall_duels"), updateRatings(winners, losers, "overall_duels"));

    if (!ratingName.equals("random_duels")) {
      updatedRatings.put(Rating.getRating(ratingName), updateRatings(winners, losers, ratingName));
    }

    StringBuilder killerMessage = new StringBuilder();

    for (Rating rating : updatedRatings.keySet()) {
      killerMessage
              .append("\n")
              .append(rating.getFriendlyName())
              .append(": +")
              .append(updatedRatings.get(rating));
    }

    StringBuilder deadMessage = new StringBuilder();

    for (Rating rating : updatedRatings.keySet()) {
      deadMessage
              .append("\n")
              .append(rating.getFriendlyName())
              .append(": -")
              .append(updatedRatings.get(rating));
    }

    for (Player loser : losers) {
      loser.sendMessage(
              AuroraDuels.getInstance()
                      .getLang()
                      .formatComponent("duel-lost", deadMessage.toString()));
    }

    for (Player winner : winners) {
      winner.sendMessage(
              AuroraDuels.getInstance()
                      .getLang()
                      .formatComponent("duel-won", killerMessage.toString()));
    }
  }

  @EventHandler
  public void onDuelDraw(DuelDrawEvent event) {
    for (Player player : event.getDuel().getPlayers()) {
      Duel.setLastDuel(player, event.getDuel());

      player.setGlowing(false);

      player.sendMessage(
          AuroraDuels.getInstance()
              .getLang()
              .getComponentCommand("rematch", "Click to request rematch", "/rematch"));
    }

    if (AuroraDuels.getInstance().isAuroraKitsInstalled()) {
      for (Player player : event.getDuel().getPlayers()) {
        Kit.refillKit(player);
      }
    }

    if (!AuroraDuels.getInstance().isAuroraCombatInstalled()) {
      return;
    }

    for (Player player : event.getDuel().getPlayers()) {
      Rating.getRating("overall").setPlayerEnabled(player, true);

      CombatTag.setTaggable(player, true);
    }
  }

  public int updateRatings(
      Collection<Player> winners, Collection<Player> losers, String ratingName) {
    Rating rating = Rating.getRating(ratingName);

    int change = 0;

    if (winners.size() + losers.size() == 2) {
      change = rating.updateElo(losers.iterator().next(), winners.iterator().next());
    } else {
      for (Player loser : losers) {
        rating.updateElo(loser, (int) getAverageRating(winners, ratingName), false);
      }

      for (Player winner : winners) {
        change = rating.updateElo(winner, (int) getAverageRating(losers, ratingName), true);
      }
    }

    return change;
  }

  public double getAverageRating(Collection<Player> players, String queueName) {
    return players.stream()
        .mapToDouble(player -> Rating.getRating(queueName).getScore(player).getPoints())
        .average()
        .orElse(AuroraCombat.getInstance().getConfig().getInt("elo.default-points"));
  }
}
