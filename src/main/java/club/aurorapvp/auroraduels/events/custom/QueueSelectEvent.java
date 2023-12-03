package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.modules.queues.Queue;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired when the player selects which queue to use for a duel */
// TODO make this cancellable
public class QueueSelectEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Player player;
  private final Queue queue;

  public QueueSelectEvent(Player player, Queue queue) {
    this.queue = queue;
    this.player = player;
  }

  public Player getPlayer() {
    return player;
  }

  public Queue getQueue() {
    return queue;
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
