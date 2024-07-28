package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired after a duel ends in a draw */
public class DuelDrawEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Duel duel;
  private final Arena arena;

  public DuelDrawEvent(Duel duel, Arena arena) {
    this.duel = duel;
    this.arena = arena;

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

    duel.setActive(false);
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
}
