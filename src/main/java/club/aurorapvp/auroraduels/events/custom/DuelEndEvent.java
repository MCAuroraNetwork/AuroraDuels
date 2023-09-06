package club.aurorapvp.auroraduels.events.custom;

import static club.aurorapvp.auroraduels.events.listeners.PlayerEventListener.DUEL_RESPAWN;

import club.aurorapvp.auroraduels.modules.duels.Duel;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.sound.Sound;
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

    for (Player player : duel.getSpectators()) {
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
