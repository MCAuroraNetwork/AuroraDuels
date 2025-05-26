package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroraduels.events.custom.QueueSelectEvent;
import club.aurorapvp.auroraduels.modules.DuelRequest;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.aurorakits.events.custom.KitSelectEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUIEventListener implements Listener {

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    for (DuelRequest duelRequest : DuelRequest.getPendingRequests()) {
      duelRequest.onInventoryClose(event);
    }
  }

  @EventHandler
  public void onQueueSelect(QueueSelectEvent event) {
    for (DuelRequest duelRequest : DuelRequest.getPendingRequests()) {
      duelRequest.onQueueSelect(event);
    }

    DuelQueue.onQueueSelect(event);
  }

  @EventHandler
  public void onKitSelect(KitSelectEvent event) {
    for (DuelRequest duelRequest : DuelRequest.getPendingRequests()) {
      duelRequest.onKitSelect(event);
    }
  }
}
