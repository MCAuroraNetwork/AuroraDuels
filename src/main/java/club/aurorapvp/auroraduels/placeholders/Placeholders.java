package club.aurorapvp.auroraduels.placeholders;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Queue;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.PartyDuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
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

  private String getResult(OfflinePlayer p, String params) {
    if (params.startsWith("in_duels")) {
      return String.valueOf(Duel.inDuels());
    }

    if (params.startsWith("in_queue_")) {
      String queueName = params.split("_")[1];

      Queue queue = Queue.getQueue(queueName);
      assert queue != null;
      return queue.inQueue() + " players in queue";
    }

    if (!p.isOnline()) {
      return "Player offline";
    }

    if (params.startsWith("opponent_ping")) {
      if (StandardDuel.inDuel(p.getPlayer())) {
        return String.valueOf(Objects.requireNonNull(StandardDuel.getStandardDuel(p.getPlayer()))
            .getOpponent(p.getPlayer()).getPing());
      } else if (Duel.inDuel(p.getPlayer())) {
        for (Player player : Objects.requireNonNull(Duel.getDuel(p.getPlayer())).getPlayers()) {
          if (player != p.getPlayer()) {
            return String.valueOf(player.getPing());
          }
        }
      }
    }

    if (params.startsWith("opponent")) {
      if (!(Duel.inDuel(p.getPlayer()))) {
        return "Not in duel";
      }

      if (StandardDuel.inDuel(p.getPlayer())) {
        return Objects.requireNonNull(StandardDuel.getStandardDuel(p.getPlayer()))
            .getOpponent(p.getPlayer()).getName();
      }

      Duel duel = Duel.getDuel(p.getPlayer());
      Set<String> opponentNames = new HashSet<>();

      if (duel instanceof TeamDuel teamDuel) {
        for (Player opponent : teamDuel.getOpponents(p.getPlayer())) {
          opponentNames.add(opponent.getName());
        }
      }

      if (duel instanceof PartyDuel partyDuel) {
        for (Player opponent : partyDuel.getOpponents(p.getPlayer())) {
          opponentNames.add(opponent.getName());
        }
      }

      return String.join(", ", opponentNames);
    }

    if (params.startsWith("duel_duration")) {
      if (StandardDuel.inDuel(p.getPlayer())) {
        Long elapsed = Objects.requireNonNull(Duel.getDuel(p.getPlayer())).getElapsed();

        return String.format("%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(elapsed),
            TimeUnit.MILLISECONDS.toSeconds(elapsed) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed))
        );

      }
    }

    return null;
  }
}
