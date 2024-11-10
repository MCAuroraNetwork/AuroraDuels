package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired when the player selects which queue to use for a duel */
// TODO make this cancellable
public class QueueSelectEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Player player;
  private final String name;
  private final DuelQueue duelQueue;

  public QueueSelectEvent(Player player, String name, DuelQueue duelQueue) {
    this.duelQueue = duelQueue;
    this.name = name;
    this.player = player;
  }

  public Player getPlayer() {
    return player;
  }

  public String getName() {
    return name;
  }

  public DuelQueue getQueue() {
    return duelQueue;
  }

  @SuppressWarnings("unused")
  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }
}
