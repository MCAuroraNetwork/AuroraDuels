package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.auroraduels.modules.queues.QueueGUI;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class QueueSelectEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private final Player p;
  private final Queue queue;

  public QueueSelectEvent(Player p, Queue queue) {
    this.queue = queue;
    this.p = p;
  }

  public Player getPlayer() {
    return p;
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
