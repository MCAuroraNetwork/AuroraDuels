package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.modules.duels.Duel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired when a duel enters the duel queue */
// TODO make this cancellable
public class QueueJoinEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Duel duel;

  public QueueJoinEvent(Duel duel) {
    this.duel = duel;
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
}
