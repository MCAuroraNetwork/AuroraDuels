package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroraduels.events.custom.DuelKitSelectEvent;
import club.aurorapvp.auroraduels.events.custom.QueueSelectEvent;
import club.aurorapvp.auroraduels.modules.Request;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class RequestEventListener implements Listener {
  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    for (Request request : Request.getPendingRequests()) {
      request.onInventoryClose(event);
    }
  }

  @EventHandler
  public void onQueueSelect(QueueSelectEvent event) {
    for (Request request : Request.getPendingRequests()) {
      request.onQueueSelect(event);
    }
  }

  @EventHandler
  public void onKitSelect(DuelKitSelectEvent event) {
    for (Request request : Request.getPendingRequests()) {
      request.onKitSelect(event);
    }
  }
}
