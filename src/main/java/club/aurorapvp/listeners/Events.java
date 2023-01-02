package club.aurorapvp.listeners;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.wand.Wand;
import club.aurorapvp.wand.WandEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class Events implements Listener {
  public static WandEvent wandEvent = new WandEvent(new Events());
  @EventHandler
  public void onWandUse(PlayerInteractEvent event) {
    if (!event.getClickedBlock().getType().isAir() && event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.IRON_HOE)) {
      if (Wand.active.get("right")) {
        wandEvent.trigger(event.getAction(), BukkitAdapter.asBlockVector(event.getClickedBlock().getLocation()));
      } else if (Wand.active.get("left")) {
        wandEvent.trigger(event.getAction(), BukkitAdapter.asBlockVector(event.getClickedBlock().getLocation()));
      }
    }
  }
}