package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
import club.aurorapvp.auroraduels.modules.duels.WinstreakTracker;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

  @Override
  public @NotNull String getIdentifier() {
    return "auroraduels";
  }

  @Override
  public @NotNull String getAuthor() {
    return "Villagers654";
  }

  @Override
  public @NotNull String getVersion() {
    return AuroraDuels.getInstance().getPluginMeta().getVersion();
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public String onRequest(OfflinePlayer player, @NotNull String params) {
    return getResult(player, params);
  }

  private String getResult(OfflinePlayer offlinePlayer, String params) {
    if (params.startsWith("in_duels_ranked")) {
      return String.valueOf(Duel.inDuels(true));
    }

    if (params.startsWith("in_duels_unranked")) {
      return String.valueOf(Duel.inDuels(false));
    }

    if (params.startsWith("in_duels")) {
      return String.valueOf(Duel.inDuels());
    }

    if (params.startsWith("in_duel_")) {
      String queueName = params.split("_")[2];

      return String.valueOf(Duel.inDuel(queueName));
    }

    if (params.startsWith("in_queues")) {
      return String.valueOf(Queue.inQueues());
    }

    if (params.startsWith("in_queue_")) {
      String queueName = params.split("_")[2];

      Queue queue = Queue.getQueue(queueName);
      assert queue != null;
      return String.valueOf(queue.inQueue());
    }

    if (!(offlinePlayer instanceof Player player)) {
      return "Player offline";
    }

    if (params.startsWith("winstreak")) {
      return String.valueOf(WinstreakTracker.getTracker(player).getWins());
    }

    if (params.startsWith("opponent_ping")) {
      if (!Duel.inDuel(player)) {
        return "Not in duel";
      }

      for (Player duelPlayer : Objects.requireNonNull(Duel.getDuel(player)).getPlayers()) {
        if (player != duelPlayer) {
          return String.valueOf(duelPlayer.getPing());
        }
      }
    }

    if (params.startsWith("opponent")) {
      if (!(Duel.inDuel(player))) {
        return "Not in duel";
      }

      if (Duel.getDuel(player) instanceof StandardDuel duel) {
        return duel.getOpponent(player).getName();
      }

      Duel duel = Duel.getDuel(player);
      Set<String> opponentNames = new HashSet<>();

      if (duel instanceof TeamDuel teamDuel) {
        for (Player opponent : teamDuel.getOpponents(player)) {
          opponentNames.add(opponent.getName());
        }
      }

      return String.join(", ", opponentNames);
    }

    if (params.startsWith("duel_duration")) {
      if (StandardDuel.inDuel(player)) {
        Long elapsed = Objects.requireNonNull(Duel.getDuel(player)).getElapsed();

        return String.format(
            "%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(elapsed),
            TimeUnit.MILLISECONDS.toSeconds(elapsed)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed)));
      }
    }

    return null;
  }
}
