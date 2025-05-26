package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.modules.DuelRequest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired when a duel request is accepted */
// TODO make this cancellable
public class DuelRequestSendEvent extends Event implements Cancellable {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Player player;
  private final DuelRequest duelRequest;
  protected boolean cancelled;

  public DuelRequestSendEvent(Player player, DuelRequest duelRequest) {
    this.player = player;
    this.duelRequest = duelRequest;
  }

  @SuppressWarnings("unused")
  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  public Player getPlayer() {
    return player;
  }

  public DuelRequest getRequest() {
    return duelRequest;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    cancelled = cancel;
  }
}
