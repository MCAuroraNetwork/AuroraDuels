package club.aurorapvp.listeners;

import club.aurorapvp.wand.WandEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import static club.aurorapvp.wand.Wand.wandActive;

public class EventListener implements Listener {

  public static final WandEvent wandEvent = new WandEvent(new EventListener());

  @EventHandler
  public void onWandUse(PlayerInteractEvent event) {
    if (!event.getClickedBlock().getType().isAir() && event.getItem().getType().equals(Material.IRON_HOE)) {
      if (wandActive.get("right")) {
        wandEvent.trigger(event.getAction(), BukkitAdapter.asBlockVector(event.getClickedBlock().getLocation()));
      } else if (wandActive.get("left")) {
        wandEvent.trigger(event.getAction(), BukkitAdapter.asBlockVector(event.getClickedBlock().getLocation()));
      }
    }
  }
}