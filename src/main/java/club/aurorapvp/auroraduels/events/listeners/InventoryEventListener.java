package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.DuelKitGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryEventListener implements Listener {
  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    Request.onInventoryClose(event);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    Request.onInventoryClicked(event);
  }
}
