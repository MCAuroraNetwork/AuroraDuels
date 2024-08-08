package club.aurorapvp.auroraduels.events.custom;

import static club.aurorapvp.auroraduels.events.listeners.PlayerEventListener.DUEL_RESPAWN;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.Winstracker;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import java.awt.Color;
import java.util.Collection;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired after a duel ends not in a draw */
public class DuelEndEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Collection<Player> winners;
  private final Collection<Player> losers;
  private final Duel duel;
  private final Arena arena;

  public DuelEndEvent(
      Duel duel, Arena arena, Collection<Player> winners, Collection<Player> losers) {
    this.duel = duel;
    this.arena = arena;
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
        Winstracker tracker = Winstracker.getTracker(player);

        tracker.addWin();

        player.sendMessage(
            AuroraDuels.getInstance().getLang().formatComponent("new-win", tracker.getWinstreak()));
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

  public Arena getArena() {
    return arena;
  }

  @SuppressWarnings("unused")
  public Long timeElapsed() {
    return duel.getElapsed();
  }

  public Collection<Player> getLosers() {
    return losers;
  }

  public Collection<Player> getWinners() {
    return winners;
  }
}
