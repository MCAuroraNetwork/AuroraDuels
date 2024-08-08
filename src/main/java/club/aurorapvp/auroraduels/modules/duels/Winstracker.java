package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroracombat.AuroraCombat;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.data.WinsDataHandler;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Winstracker {

  private static final Map<UUID, Winstracker> TRACKERS = new HashMap<>();
  private final WinsDataHandler data;
  private final Player player;
  private int winstreak;
  private int losses;
  private int wins;

  public Winstracker(Player player) {
    this.player = player;
    data = new WinsDataHandler(this);

    if (this.exists()) {
      this.reload();
    } else {
      winstreak = 0;
      losses = 0;
      wins = 0;
      this.save();
    }
  }

  public static void register(Player player) {
    TRACKERS.put(player.getUniqueId(), new Winstracker(player));
  }

  public static void unregister(Player player) {
    TRACKERS.remove(player.getUniqueId());
  }

  public Player getPlayer() {
    return player;
  }

  public int getWinstreak() {
    return this.winstreak;
  }

  public int getWins() {
    return wins;
  }

  public int getLosses() {
    return losses;
  }

  public boolean exists() {
    return this.data.exists();
  }

  public void reload() {
    winstreak = data.getWinstreak();
    losses = data.getLosses();
    wins = data.getWins();
  }

  public void save() {
    this.data.save();
  }

  public void addWin() {
    this.wins++;
    this.winstreak++;

    if (winstreak % AuroraCombat.getInstance().getConfig().getInt("misc.min-killstreak-to-announce")
        == 0) {
      Bukkit.broadcast(
          AuroraCombat.getInstance()
              .getLang()
              .formatComponent("on-winstreak", player.getName(), winstreak));

      if (AuroraDuels.getInstance().isDiscordSRVInstalled()) {
        DiscordSRV.getPlugin()
            .getOptionalTextChannel("global")
            .sendMessageEmbeds(
                new EmbedBuilder()
                    .setAuthor(
                        AuroraDuels.getInstance()
                            .getLang()
                            .getString("discord-on-winstreak")
                            .formatted(player.getName(), winstreak),
                        null,
                        "https://cravatar.eu/helmavatar/" + player.getUniqueId())
                    .setColor(Color.BLACK)
                    .build())
            .queue();
      }
    }

    this.save();
  }

  public void addLoss() {
    this.losses++;

    if (winstreak
        >= AuroraDuels.getInstance().getConfig().getInt("misc.min-winstreak-to-announce")) {
      Bukkit.broadcast(
          AuroraCombat.getInstance()
              .getLang()
              .formatComponent("winstreak-lost", player.getName(), winstreak));

      if (AuroraDuels.getInstance().isDiscordSRVInstalled()) {
        DiscordSRV.getPlugin()
                .getOptionalTextChannel("global")
                .sendMessageEmbeds(
                        new EmbedBuilder()
                                .setAuthor(
                                        AuroraDuels.getInstance()
                                                .getLang()
                                                .getString("discord-winstreak-lost")
                                                .formatted(player.getName(), winstreak),
                                        null,
                                        "https://cravatar.eu/helmavatar/" + player.getUniqueId())
                                .setColor(Color.BLACK)
                                .build())
                .queue();
      }
    }

    this.winstreak = 0;

    this.save();
  }

  public static Winstracker getTracker(Player player) {
    return TRACKERS.get(player.getUniqueId());
  }
}
