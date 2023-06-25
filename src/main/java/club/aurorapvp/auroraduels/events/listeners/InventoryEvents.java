package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.duels.DuelKitGUI;
import club.aurorapvp.auroraduels.modules.queues.QueueGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryEvents implements Listener {
  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (AuroraDuels.isAuroraKitsInstalled()) {
      DuelKitGUI.onInventoryClose(event);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    QueueGUI.onInventoryClicked(event);

    if (AuroraDuels.isAuroraKitsInstalled()) {
      DuelKitGUI.onInventoryClicked(event);
    }
  }
}
