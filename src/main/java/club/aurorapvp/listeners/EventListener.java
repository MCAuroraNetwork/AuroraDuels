package club.aurorapvp.listeners;

import static club.aurorapvp.modules.WandModule.WandModule.getWandAction;
import static club.aurorapvp.modules.WandModule.WandModule.wandActive;
import static com.sk89q.worldedit.bukkit.BukkitAdapter.asBlockVector;

import club.aurorapvp.modules.WandModule.WandUseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventListener implements Listener {

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (wandActive) {
      if (getWandAction(event.getAction(), event.getItem().getType(),
          event.getClickedBlock().getType()) != 0) {
        WandUseEvent wandEvent = new WandUseEvent(this);
        wandEvent.trigger(event.getAction(), asBlockVector(event.getClickedBlock().getLocation()));
      }
    }
  }
}