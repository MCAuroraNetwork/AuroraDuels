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
