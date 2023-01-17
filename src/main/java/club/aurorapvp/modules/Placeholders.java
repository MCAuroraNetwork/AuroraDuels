package club.aurorapvp.modules;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

  private final Plugin plugin;

  public Placeholders(Plugin plugin) {
    this.plugin = plugin;
  }

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
          return String.valueOf(Duel.getOpponent(p.getPlayer()).getPing());
        }
      }
    }

    if (params.startsWith("opponent")) {
      if (p.isOnline()) {
        if (Duel.inDuel(p.getPlayer())) {
          return Duel.getOpponent(p.getPlayer()).getName();
        }
      }
    }


    return null;
  }
}
