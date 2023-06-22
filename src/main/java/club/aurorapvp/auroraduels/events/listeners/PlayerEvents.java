package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroraduels.configs.Config;
import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.Queue;
import club.aurorapvp.auroraduels.modules.Wand;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.PartyDuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
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

    Duel duel = Duel.getDuel(event.getPlayer());

    if (duel instanceof StandardDuel standardDuel) {
      standardDuel.endDuel(new Player[] {standardDuel.getOpponent(event.getPlayer())},
          new Player[] {event.getPlayer()});
    } else if (duel instanceof PartyDuel partyDuel){
      boolean teammatesDead = true;
      for (Player p : partyDuel.getTeammates(event.getPlayer())) {
        if (!(p.getGameMode() == GameMode.SPECTATOR) && !p.isDead() && p.isOnline()) {
          teammatesDead = false;
          break;
        }
      }

      if (teammatesDead) {
        duel.endDuel(partyDuel.getOpponents(event.getPlayer()), partyDuel.getTeammates(event.getPlayer()));
      }
    } else if (duel instanceof TeamDuel teamDuel) {
      boolean teammatesDead = true;
      for (Player p : teamDuel.getTeammates(event.getPlayer())) {
        if (!(p.getGameMode() == GameMode.SPECTATOR) && !p.isDead() && p.isOnline()) {
          teammatesDead = false;
          break;
        }
      }

      if (teammatesDead) {
        duel.endDuel(teamDuel.getOpponents(event.getPlayer()), teamDuel.getTeammates(event.getPlayer()));
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (!Duel.inDuel(event.getPlayer())) {
      return;
    }

    Duel duel = Duel.getDuel(event.getPlayer());

    if (duel instanceof StandardDuel standardDuel) {
      standardDuel.endDuel(new Player[] {standardDuel.getOpponent(event.getPlayer())},
          new Player[] {event.getPlayer()});
    } else if (duel instanceof PartyDuel partyDuel){
      boolean teammatesDead = true;
      for (Player p : partyDuel.getTeammates(event.getPlayer())) {
        if (!(p.getGameMode() == GameMode.SPECTATOR) && !p.isDead() && p.isOnline()) {
          teammatesDead = false;
          break;
        }
      }

      if (teammatesDead) {
        duel.endDuel(partyDuel.getOpponents(event.getPlayer()), partyDuel.getTeammates(event.getPlayer()));
      }
    } else if (duel instanceof TeamDuel teamDuel) {
      boolean teammatesDead = true;
      for (Player p : teamDuel.getTeammates(event.getPlayer())) {
        if (!(p.getGameMode() == GameMode.SPECTATOR) && !p.isDead() && p.isOnline()) {
          teammatesDead = false;
          break;
        }
      }

      if (teammatesDead) {
        duel.endDuel(teamDuel.getOpponents(event.getPlayer()), teamDuel.getTeammates(event.getPlayer()));
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