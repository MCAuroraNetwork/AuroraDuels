package club.aurorapvp.auroraduels.events.custom;

import static club.aurorapvp.auroraduels.events.listeners.PlayerEventListener.DUEL_RESPAWN;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import java.awt.Color;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired after a duel ends */
public class DuelEndEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Player[] winners;
  private final Player[] losers;
  private final Duel duel;

  public DuelEndEvent(Duel duel, Player[] winners, Player[] losers) {
    this.duel = duel;
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
                duel.getArena().reset();
                duel.getArena().setOccupied(false);
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
        Duel.addWin(player);
      }
    }

    for (Player player : this.getWinners()) {
      player.playSound(Sound.sound().type(org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL).build());

      if (duel.isRanked()) {
        Duel.resetStreak(player);
      }
    }

    if (!duel.checkBounds.isCancelled()) {
      duel.checkBounds.cancel();
    }

    if (duel.bossbarTask != null && !duel.bossbarTask.isCancelled()) {
      duel.bossbarTask.cancel();
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < winners.length; i++) {
      sb.append(winners[i].getName());
      if (i < winners.length - 2) {
        sb.append(", ");
      } else if (i == winners.length - 2) {
        sb.append(" and ");
      }
    }

    String winnersText = sb.toString();

    sb = new StringBuilder();
    for (int i = 0; i < losers.length; i++) {
      sb.append(losers[i].getName());
      if (i < losers.length - 2) {
        sb.append(", ");
      } else if (i == losers.length - 2) {
        sb.append(" and ");
      }
    }

    String losersText = sb.toString();

    Bukkit.broadcast(AuroraDuels.INSTANCE.getLang().formatComponent("duel-won", winnersText, losersText));

    if (AuroraDuels.isDiscordSRVInstalled()) {
      DiscordSRV.getPlugin().getOptionalTextChannel("global").sendMessageEmbeds(
          new EmbedBuilder().setAuthor(
                  AuroraDuels.INSTANCE.getLang().getString("discord-duel-won").formatted(winnersText, losersText), null,
                  "https://cravatar.eu/helmavatar/" + winners[0].getUniqueId())
              .setColor(Color.BLACK).build()).queue();
    }

    duel.setActive(false);

    Duel.getDuels().remove(duel);
  }

  @SuppressWarnings("unused")
  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  public Duel getDuel() {
    return duel;
  }

  @SuppressWarnings("unused")
  public Long timeElapsed() {
    return duel.getElapsed();
  }

  public Player[] getLosers() {
    return losers;
  }

  public Player[] getWinners() {
    return winners;
  }
}
