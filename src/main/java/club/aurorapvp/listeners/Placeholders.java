package club.aurorapvp.listeners;

import club.aurorapvp.modules.Duel;
import java.util.concurrent.TimeUnit;
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
    return "1.0.0";
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

    if (params.startsWith("opponent_ping")) {
      if (p.isOnline()) {
        if (Duel.inDuel(p.getPlayer())) {
          return String.valueOf(Duel.getDuel(p.getPlayer()).getOpponent(p.getPlayer()).getPing());
        }
      }
    }

    if (params.startsWith("opponent")) {
      if (p.isOnline()) {
        if (Duel.inDuel(p.getPlayer())) {
          return Duel.getDuel(p.getPlayer()).getOpponent(p.getPlayer()).getName();
        }
      }
    }

    if (params.startsWith("duel_duration")) {
      if (p.isOnline()) {
        if (Duel.inDuel(p.getPlayer())) {
          Long elapsed = Duel.getDuel(p.getPlayer()).getElapsed();

          return String.format("%d min, %d sec",
              TimeUnit.MILLISECONDS.toMinutes(elapsed),
              TimeUnit.MILLISECONDS.toSeconds(elapsed) -
                  TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed))
          );
        }
      }
    }
    return null;
  }
}
