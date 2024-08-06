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

    Rating rating = Rating.getRating("default");

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

    Rating rating = Rating.getRating("default");

    for (Player player : event.getDuel().getPlayers()) {
      rating.setPlayerEnabled(player, false);

      CombatTag.setTaggable(player, true);
    }

    if (!event.getDuel().isRanked()) {
      return;
    }

    String queueName = event.getDuel().getQueue().getName() + "_duels";

    Collection<Player> winners = event.getWinners();
    Collection<Player> losers = event.getLosers();

    updateRatings(winners, losers, queueName);

    if (AuroraDuels.getInstance()
        .getConfig()
        .getBoolean("optional-plugins.auroracombat-compatibility.always-enable-default-elo")) {
      updateRatings(winners, losers, "random_duels");
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
      Rating.getRating("default").setPlayerEnabled(player, true);

      CombatTag.setTaggable(player, true);
    }
  }

  public void updateRatings(
      Collection<Player> winners, Collection<Player> losers, String queueName) {
    Rating rating = Rating.getRating(queueName);

    if (winners.size() + losers.size() == 2) {
      rating.updateElo(losers.iterator().next(), winners.iterator().next());
    } else {
      for (Player loser : losers) {
        rating.updateElo(loser, (int) getAverageRating(winners, queueName), false);
      }

      for (Player winner : winners) {
        rating.updateElo(winner, (int) getAverageRating(losers, queueName), true);
      }
    }
  }

  public double getAverageRating(Collection<Player> players, String queueName) {
    return players.stream()
        .mapToDouble(player -> Rating.getRating(queueName).getScore(player).getPoints())
        .average()
        .orElse(AuroraCombat.getInstance().getConfig().getInt("elo.default-points"));
  }
}
