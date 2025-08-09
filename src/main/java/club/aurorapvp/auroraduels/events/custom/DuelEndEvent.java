package club.aurorapvp.auroraduels.events.custom;

import static club.aurorapvp.auroraduels.events.listeners.PlayerEventListener.DUEL_RESPAWN;

import club.aurorapvp.auroracombat.AuroraCombat;
import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.data.StatsDataHandler;
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
import org.bukkit.potion.PotionEffect;

/* Event that is fired after a duel ends not in a draw */
public class DuelEndEvent extends DuelOverEvent {

  private final Collection<Player> winners;
  private final Collection<Player> losers;

  public DuelEndEvent(
      Duel duel, Arena arena, Collection<Player> winners, Collection<Player> losers) {
    super(duel, arena);
    this.winners = winners;
    this.losers = losers;

    for (Player player : duel.getPlayers()) {
      duel.setHealthRemaining(player, player.getHealth() + player.getAbsorptionAmount());

      DuelStats.Stats stats = duel.getStats(player);

      StatsDataHandler.save(stats);

      player.sendMessage(
          AuroraDuels.getInstance()
              .getLang()
              .getComponentCommand("duel-stats", "Click to see duel stats!", "/duel stats"));
    }

    for (Player player : duel.getPlayers()) {
      player.setFoodLevel(20);
      player.setSaturation(5f);
      player.setExhaustion(0f);
      player.setAbsorptionAmount(0f);

      player.setRemainingAir(player.getMaximumAir());

      for (PotionEffect effect : player.getActivePotionEffects()) {
        player.removePotionEffect(effect.getType());
      }
    }

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

    Set<Rating> ratingsToUpdate = new HashSet<>();
    Rating overall = Rating.getRating("overall_duels");
    ratingsToUpdate.add(overall);

    Rating specific = null;
    if (!ratingName.equals("overall_duels")) {
      specific = Rating.getRating(ratingName);
      ratingsToUpdate.add(specific);
    }

    Map<Rating, Map<Player, Integer>> oldScores = new HashMap<>();

    for (Rating rating : ratingsToUpdate) {
      Map<Player, Integer> playerOldScores = new HashMap<>();
      for (Player player : duel.getPlayers()) {
        playerOldScores.put(player, rating.getScore(player).getPoints());
      }
      oldScores.put(rating, playerOldScores);
    }

    updateRatings(winners, losers, "overall_duels");

    if (specific != null) {
      updateRatings(winners, losers, ratingName);
    }

    for (Player loser : losers) {
      StringBuilder deadMessage = new StringBuilder();
      for (Rating rating : ratingsToUpdate) {
        int delta = rating.getScore(loser).getPoints() - oldScores.get(rating).get(loser);
        deadMessage.append("\n").append(rating.getFriendlyName()).append(": ").append(delta);
      }
      loser.sendMessage(
          AuroraDuels.getInstance().getLang().formatComponent("duel-lost", deadMessage.toString()));
    }

    for (Player winner : winners) {
      StringBuilder killerMessage = new StringBuilder();
      for (Rating rating : ratingsToUpdate) {
        int delta = rating.getScore(winner).getPoints() - oldScores.get(rating).get(winner);
        killerMessage.append("\n").append(rating.getFriendlyName()).append(": ").append(delta);
      }
      killerMessage.append("\nWinstreak: ").append(Winstracker.getTracker(winner).getWinstreak());
      winner.sendMessage(
          AuroraDuels.getInstance()
              .getLang()
              .formatComponent("duel-won", killerMessage.toString()));
    }
  }

  public void updateRatings(
      Collection<Player> winners, Collection<Player> losers, String ratingName) {
    Rating rating = Rating.getRating(ratingName);

    if (winners.size() + losers.size() == 2) {
      rating.updateElo(losers.iterator().next(), winners.iterator().next());
      return;
    }

    double factor = 1.0 / losers.size();
    int K = AuroraCombat.getInstance().getConfig().getInt("elo.max-change");

    Map<Player, Integer> totalChanges = new HashMap<>();
    for (Player player : winners) {
      totalChanges.put(player, 0);
    }
    for (Player player : losers) {
      totalChanges.put(player, 0);
    }

    for (Player winner : winners) {
      for (Player loser : losers) {
        int loserElo = rating.getScore(loser).getPoints();
        int winnerElo = rating.getScore(winner).getPoints();
        double eloChange = Rating.getELOChange(loserElo, winnerElo);
        double changeD = K * -eloChange * factor;
        int changePair = (int) Math.round(changeD);
        totalChanges.put(winner, totalChanges.get(winner) + changePair);
        totalChanges.put(loser, totalChanges.get(loser) - changePair);
      }
    }

    for (Map.Entry<Player, Integer> entry : totalChanges.entrySet()) {
      rating.getScore(entry.getKey()).changePoints(entry.getValue());
    }
  }

  public Collection<Player> getLosers() {
    return losers;
  }

  public Collection<Player> getWinners() {
    return winners;
  }
}
