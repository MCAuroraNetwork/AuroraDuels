package club.aurorapvp.auroraduels.events.custom;

import static club.aurorapvp.auroraduels.events.listeners.PlayerEventListener.DUEL_RESPAWN;

import club.aurorapvp.auroracombat.AuroraCombat;
import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.DuelStats;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.Winstracker;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import java.awt.Color;
import java.util.*;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/* Event that is fired after a duel ends not in a draw */
public class DuelEndEvent extends DuelOverEvent {

  private final Collection<Player> winners;
  private final Collection<Player> losers;

  public DuelEndEvent(
      Duel duel, Arena arena, Collection<Player> winners, Collection<Player> losers) {
    super(duel, arena);
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
                arena.reset();
                arena.setOccupied(false);
                this.cancel();
              }
            },
            3000);

    Set<Component> summaries = new HashSet<>();

    for (Player player : duel.getPlayers()) {
      duel.setHealthRemaining(player, player.getHealth() + player.getAbsorptionAmount());

      DuelStats stats = duel.getStats(player);

      stats.save();

      if (AuroraDuels.getInstance().isAuroraCombatInstalled()) {
        summaries.add(
                AuroraDuels.getInstance()
                        .getLang()
                        .formatComponent(
                                "duel-summary-elo",
                                player.getName(),
                                stats.getElo(),
                                stats.getTotemPops(),
                                stats.getDamageDealt(),
                                stats.getDamageReceived(),
                                stats.getHealthRemaining()));
      } else {
        summaries.add(
                AuroraDuels.getInstance()
                        .getLang()
                        .formatComponent(
                                "duel-summary",
                                player.getName(),
                                stats.getTotemPops(),
                                stats.getDamageDealt(),
                                stats.getDamageReceived(),
                                stats.getHealthRemaining()));
      }
    }

    for (Player player : duel.getPlayers()) {
      for (Component message : summaries) {
        player.sendMessage(message);
      }
    }

    for (Player player : Set.copyOf(duel.getSpectators())) {
      duel.setSpectator(player, false);
    }

    for (Player player : duel.getPlayers()) {
      if (player.isDead()) {
        DUEL_RESPAWN.add(player);
      }
    }

    for (Player player : this.getWinners()) {
      player.playSound(Sound.sound().type(org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_TWINKLE).build());

      if (duel.isRanked()) {
        Winstracker.getTracker(player).addWin();
      }
    }

    for (Player player : this.getLosers()) {
      player.playSound(Sound.sound().type(org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL).build());

      if (duel.isRanked()) {
        Winstracker.getTracker(player).addLoss();
      }
    }

    StringBuilder sb = new StringBuilder();
    int count = 0;
    for (Player winner : winners) {
      sb.append(winner.getName());
      if (count < winners.size() - 2) {
        sb.append(", ");
      } else if (count == winners.size() - 2) {
        sb.append(" and ");
      }
      count++;
    }

    String winnersText = sb.toString();

    sb = new StringBuilder();
    count = 0;
    for (Player loser : losers) {
      sb.append(loser.getName());
      if (count < losers.size() - 2) {
        sb.append(", ");
      } else if (count == losers.size() - 2) {
        sb.append(" and ");
      }
      count++;
    }

    String losersText = sb.toString();

    Bukkit.broadcast(
        AuroraDuels.getInstance().getLang().formatComponent("duel-over", winnersText, losersText));

    if (AuroraDuels.getInstance().isDiscordSRVInstalled()) {
      DiscordSRV.getPlugin()
          .getOptionalTextChannel("global")
          .sendMessageEmbeds(
              new EmbedBuilder()
                  .setAuthor(
                      AuroraDuels.getInstance()
                          .getLang()
                          .getString("discord-duel-over")
                          .formatted(winnersText, losersText),
                      null,
                      "https://cravatar.eu/helmavatar/" + winners.iterator().next().getUniqueId())
                  .setColor(Color.BLACK)
                  .build())
          .queue();
    }

    if (!duel.isRanked()) {
      return;
    }

    String ratingName = duel.getQueue().getName() + "_duels";

    Map<Rating, Integer> updatedRatings = new HashMap<>();

    updatedRatings.put(
        Rating.getRating("overall_duels"), updateRatings(winners, losers, "overall_duels"));

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
          AuroraDuels.getInstance().getLang().formatComponent("duel-lost", deadMessage.toString()));
    }

    for (Player winner : winners) {
      killerMessage.append("\nWinstreak: ").append(Winstracker.getTracker(winner).getWinstreak());

      winner.sendMessage(
          AuroraDuels.getInstance()
              .getLang()
              .formatComponent("duel-won", killerMessage.toString()));
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

  public Collection<Player> getLosers() {
    return losers;
  }

  public Collection<Player> getWinners() {
    return winners;
  }
}
