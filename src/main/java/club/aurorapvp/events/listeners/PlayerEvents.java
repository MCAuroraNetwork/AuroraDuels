package club.aurorapvp.events.listeners;

import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.duels.Duel;
import club.aurorapvp.modules.duels.PartyDuel;
import club.aurorapvp.modules.Queue;
import club.aurorapvp.modules.duels.StandardDuel;
import club.aurorapvp.modules.Wand;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerEvents implements Listener {
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
      Arena.onWandUse(event);
    }
  }

  @EventHandler
  public void onPlayerChat(AsyncChatEvent event) {
    if (Queue.isCreator(event.getPlayer()) && !Queue.isArenasSet()) {
      Queue.isUnFinishedQueue().createArenaList(event.getPlayer(),
          MiniMessage.miniMessage().serialize(event.originalMessage()));
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    if (!Duel.inDuel(event.getPlayer())) {
      return;
    }

    if (Duel.getDuel(event.getPlayer()).getPlayers().size() == 2) {
      StandardDuel duel = (StandardDuel) Duel.getDuel(event.getPlayer());

      duel.endDuel(new Player[]{duel.getOpponent(event.getPlayer())}, new Player[]{event.getPlayer()});
    } else {
      PartyDuel duel = (PartyDuel) Duel.getDuel(event.getPlayer());

      boolean teammatesDead = true;
      for (Player p : duel.getTeammates(event.getPlayer())) {
        if (!(p.getGameMode() == GameMode.SPECTATOR) && !p.isDead() && p.isOnline()) {
          teammatesDead = false;
          break;
        }
      }

      if (teammatesDead) {
        duel.endDuel(duel.getOpponents(event.getPlayer()), duel.getTeammates(event.getPlayer()));
      } else {
        event.getPlayer().spigot().respawn();
        duel.setSpectator(event.getPlayer(), true);
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (Duel.getDuel(event.getPlayer()).getPlayers().size() == 2) {
      StandardDuel duel = (StandardDuel) Duel.getDuel(event.getPlayer());
      duel.endDuel(new Player[]{duel.getOpponent(event.getPlayer())}, new Player[]{event.getPlayer()});
    } else {
      PartyDuel duel = (PartyDuel) Duel.getDuel(event.getPlayer());

      boolean teammatesDead = true;
      for (Player p : duel.getTeammates(event.getPlayer())) {
        if (!(p.getGameMode() == GameMode.SPECTATOR) && !p.isDead() && p.isOnline()) {
          teammatesDead = false;
          break;
        }
      }

      if (teammatesDead) {
        duel.endDuel(duel.getOpponents(event.getPlayer()), duel.getTeammates(event.getPlayer()));
      }
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