package club.aurorapvp.listeners;

import club.aurorapvp.modules.wand.Wand;
import club.aurorapvp.modules.wand.WandEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class Events implements Listener {
  public static WandEvent wandEvent = new WandEvent(new Events());

  @EventHandler
  public void onWandUse(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null) {
      return;
    }

    if(event.getPlayer().getCooldown(Material.IRON_HOE) > 0) {
      return;
    }

    if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.IRON_HOE)) {
      return;
    }

    if (Wand.isActive(event.getAction())) {
      wandEvent.trigger(event.getAction(),
          BukkitAdapter.asBlockVector(event.getClickedBlock().getLocation()),
          event.getPlayer());
    }
  }
}