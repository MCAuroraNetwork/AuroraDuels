package club.aurorapvp.placeholders;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.modules.duels.Duel;
import club.aurorapvp.modules.Queue;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
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
      String queue = params.split("_")[1];

      return Queue.getQueue(queue).inQueue() + " players in queue";
    }

    if (!p.isOnline()) {
      return "Player offline";
    }

    // TODO: update to account for 2v2+ duels

    /*
    if (params.startsWith("opponent_ping")) {
      if (p.isOnline()) {
        if (StandardDuel.inDuel(p.getPlayer())) {
          return String.valueOf(StandardDuel.getDuel(p.getPlayer()).getOpponent(p.getPlayer()).getPing());
        }
      }
    }

    if (params.startsWith("opponent")) {
      if (p.isOnline()) {
        if (StandardDuel.inDuel(p.getPlayer())) {
          return StandardDuel.getDuel(p.getPlayer()).getOpponent(p.getPlayer()).getName();
        }
      }
    }

    if (params.startsWith("duel_duration")) {
      if (p.isOnline()) {
        if (StandardDuel.inDuel(p.getPlayer())) {
          Long elapsed = StandardDuel.getDuel(p.getPlayer()).getElapsed();

          return String.format("%d min, %d sec",
              TimeUnit.MILLISECONDS.toMinutes(elapsed),
              TimeUnit.MILLISECONDS.toSeconds(elapsed) -
                  TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed))
          );
        }
      }
    }
    */

    return null;
  }
}
