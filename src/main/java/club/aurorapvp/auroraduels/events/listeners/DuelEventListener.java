package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroracombat.modules.CombatTag;
import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import java.awt.Color;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DuelEventListener implements Listener {

  @EventHandler
  public void onDuelStart(DuelStartEvent event) {
    if (!AuroraDuels.isAuroraCombatInstalled()) {
      return;
    }

    Rating.getRating("default").setEnabled(false);

    if (event.getDuel().isRanked()) {
      if (Config.get()
          .getBoolean("optional-plugins.auroracombat-compatibility.always-enable-default-elo")) {
        Rating.getRating("default_duels").setEnabled(true);
      }
    }

    for (Player player : event.getDuel().getPlayers()) {
      CombatTag.setTaggable(player, false);
    }
  }

  @EventHandler
  public void onDuelEnd(DuelEndEvent event) {
    for (Player player : event.getDuel().getPlayers()) {
      Duel.setLastDuel(player, event.getDuel());
    }

    if (AuroraDuels.isAuroraKitsInstalled()) {
      for (Player player : event.getDuel().getPlayers()) {
        Kit.getLastUsedKit(player).giveKit(player);
      }
    }

    if (!AuroraDuels.isAuroraCombatInstalled()) {
      return;
    }

    Rating.getRating("default").setEnabled(true);

    for (Player player : event.getDuel().getPlayers()) {
      CombatTag.setTaggable(player, true);
    }

    Player[] winners = event.getWinners();
    Player[] losers = event.getLosers();

    if (!event.getDuel().isRanked()) {
      return;
    }

    if (Config.get()
        .getBoolean("optional-plugins.auroracombat-compatibility.always-enable-default-elo")) {
      Rating.getRating("default_duels").setEnabled(false);
    }

    if (event.getDuel().getPlayers().size() == 2) {
      Rating.getRating(event.getDuel().getQueue().getName() + "_duels")
          .updateElo(event.getLosers()[0], event.getWinners()[0]);
    } else if (event.getDuel().getPlayers().size() > 2) {
      double loserAvgRating =
          Arrays.stream(losers)
              .mapToDouble(
                  player ->
                      Rating.getRating(event.getDuel().getQueue().getName() + "_duels")
                          .getScore(player)
                          .getPoints())
              .average()
              .orElse(club.aurorapvp.auroracombat.config.Config.get().getInt("elo.default-points"));
      double winnerAvgRating =
          Arrays.stream(winners)
              .mapToDouble(
                  player ->
                      Rating.getRating(event.getDuel().getQueue().getName() + "_duels")
                          .getScore(player)
                          .getPoints())
              .average()
              .orElse(club.aurorapvp.auroracombat.config.Config.get().getInt("elo.default-points"));

      for (Player loser : losers) {
        Rating.getRating(event.getDuel().getQueue().getName() + "_duels")
            .updateElo(loser, (int) winnerAvgRating, false);
      }

      for (Player winner : winners) {
        Rating.getRating(event.getDuel().getQueue().getName() + "_duels")
            .updateElo(winner, (int) loserAvgRating, true);
      }
    }
  }
}
