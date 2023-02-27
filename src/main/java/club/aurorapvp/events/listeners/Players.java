package club.aurorapvp.events.listeners;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Duel;
import club.aurorapvp.modules.Queue;
import club.aurorapvp.modules.Wand;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Players implements Listener {
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null) {
      return;
    }

    if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.IRON_HOE)) {
      return;
    }

    if (event.getPlayer().getCooldown(Material.IRON_HOE) > 0) {
      return;
    }

    if (Wand.isActive(event.getAction())) {
      Arena.onWandEvent(event);
    }
  }

  @EventHandler
  public void onPlayerChat(AsyncChatEvent event) {
    if (Queue.isCreator(event.getPlayer()) && !Queue.isArenasSet()) {
      Queue.isUnFinishedQueue().createArenaList(event.getPlayer(),
          AuroraDuels.COMPONENT_SERIALIZER.serialize(event.originalMessage()));
      event.setCancelled(true);
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

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    if (Config.get().getBoolean("duels.enable-teleports")) {
      return;
    }

    if (Duel.inDuel(event.getPlayer())) {
      event.getPlayer().sendMessage(Lang.getComponent("no-teleport"));
      event.setCancelled(true);
    }
  }
}