package club.aurorapvp.events.custom;

import club.aurorapvp.modules.duels.Duel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is fired after a duel ends
 */
public class DuelStartEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private final Duel duel;

  public DuelStartEvent(Duel duel) {
    this.duel = duel;
  }

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
