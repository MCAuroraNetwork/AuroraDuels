package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.PartyDuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
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
    return AuroraDuels.INSTANCE.getPluginMeta().getVersion();
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public String onRequest(OfflinePlayer p, @NotNull String params) {
    return getResult(p, params);
  }

  private String getResult(OfflinePlayer player, String params) {
    if (params.startsWith("in_duels")) {
      return String.valueOf(Duel.inDuels());
    }

    if (params.startsWith("in_queue_")) {
      String queueName = params.split("_")[2];

      Queue queue = Queue.getQueue(queueName);
      assert queue != null;
      return String.valueOf(queue.inQueue());
    }

    if (params.startsWith("in_duel_")) {
      String queueName = params.split("_")[2];

      return String.valueOf(Duel.inDuel(queueName));
    }

    if (!(player instanceof Player p)) {
      return "Player offline";
    }

    if (params.startsWith("winstreak")) {
      return String.valueOf(Duel.getWins(p));
    }

    if (params.startsWith("opponent_ping")) {
      if (!Duel.inDuel(p)) {
        return "Not in duel";
      }

      for (Player duelPlayer : Objects.requireNonNull(Duel.getDuel(p)).getPlayers()) {
        if (player != duelPlayer) {
          return String.valueOf(duelPlayer.getPing());
        }
      }
    }

    if (params.startsWith("opponent")) {
      if (!(Duel.inDuel(p))) {
        return "Not in duel";
      }

      if (Duel.getDuel(p) instanceof StandardDuel duel) {
        return duel.getOpponent(p).getName();
      }

      Duel duel = Duel.getDuel(p);
      Set<String> opponentNames = new HashSet<>();

      if (duel instanceof TeamDuel teamDuel) {
        for (Player opponent : teamDuel.getOpponents(p)) {
          opponentNames.add(opponent.getName());
        }
      }

      if (duel instanceof PartyDuel partyDuel) {
        for (Player opponent : partyDuel.getOpponents(p)) {
          opponentNames.add(opponent.getName());
        }
      }

      return String.join(", ", opponentNames);
    }

    if (params.startsWith("duel_duration")) {
      if (StandardDuel.inDuel(p)) {
        Long elapsed = Objects.requireNonNull(Duel.getDuel(p)).getElapsed();

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
