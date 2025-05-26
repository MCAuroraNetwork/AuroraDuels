package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.aurorawarps.modules.TeleportRequest;
import org.bukkit.entity.Player;
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

    if (AuroraDuels.getInstance().isAuroraWarpsInstalled()) {
      for (Player player : duel.getPlayers()) {
        if (TeleportRequest.hasPendingRequest(player)) {
          for (TeleportRequest teleportRequest : TeleportRequest.getRequests(player)) {
            teleportRequest.deny();
          }
        }
      }
    }
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
