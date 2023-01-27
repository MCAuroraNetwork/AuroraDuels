package club.aurorapvp.listeners;

import club.aurorapvp.events.WandEvent;
import club.aurorapvp.modules.Duel;
import club.aurorapvp.modules.Wand;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null) {
      return;
    }

    if (event.getPlayer().getCooldown(Material.IRON_HOE) > 0) {
      return;
    }

    if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.IRON_HOE)) {
      return;
    }

    if (Wand.isActive(event.getAction())) {
      Bukkit.getPluginManager().callEvent(new WandEvent(event.getAction(),
          BukkitAdapter.asBlockVector(event.getClickedBlock().getLocation()), event.getPlayer()));
    }
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    if (Duel.inDuel(event.getPlayer())) {
      Duel duel = Duel.getDuel(event.getPlayer());
      duel.endDuel();
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (Duel.inDuel(event.getPlayer())) {
      Duel duel = Duel.getDuel(event.getPlayer());
      duel.endDuel();
    }
  }
}