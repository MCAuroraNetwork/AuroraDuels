package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.Wand;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.DuelKitGUI;
import club.aurorapvp.auroraduels.modules.duels.FFADuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashSet;
import java.util.Set;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerEventListener implements Listener {

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
      Queue.getUnFinishedQueue()
          .createArenaList(
              event.getPlayer(), MiniMessage.miniMessage().serialize(event.originalMessage()));
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onCommandRun(PlayerCommandPreprocessEvent event) {
    if (!Duel.inDuel(event.getPlayer())) {
      return;
    }

    if (Config.get().getBoolean("duels.allow-commands")) {
      return;
    }

    if (event.getPlayer().hasPermission("auroraduels.bypass.commands")) {
      return;
    }

    event.getPlayer().sendMessage(Lang.getComponent("commands-disabled"));

    event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    if (!Duel.inDuel(event.getPlayer())) {
      return;
    }

    checkIfDuelOver(event.getPlayer());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (AuroraDuels.isAuroraKitsInstalled()) {
      DuelKitGUI.remove(event.getPlayer());
    }

    if (Request.hasPendingRequest(event.getPlayer())) {
      Request.getRequest(event.getPlayer()).deny();
    }

    if (!Duel.inDuel(event.getPlayer())) {
      return;
    }

    checkIfDuelOver(event.getPlayer());
  }

  public void checkIfDuelOver(Player player) {
    Duel duel = Duel.getDuel(player);

    if (duel instanceof StandardDuel standardDuel) {
      standardDuel.endDuel(new Player[]{standardDuel.getOpponent(player)}, new Player[]{player});
    } else if (duel instanceof TeamDuel teamDuel) {
      teamDuel.setDead(player);

      if ((teamDuel.teamDead(player))) {
        duel.endDuel(teamDuel.getOpponents(player), teamDuel.getTeammates(player));
      } else {
        duel.setSpectator(player, true);
      }
    } else if (duel instanceof FFADuel ffaDuel) {
      ffaDuel.setDead(player);

      if (ffaDuel.onePlayerAlive()) {
        Set<Player> losers = new HashSet<>(ffaDuel.getPlayers());
        losers.remove(ffaDuel.getFirstAlive());

        duel.endDuel(new Player[]{ffaDuel.getFirstAlive()}, losers.toArray(new Player[0]));
      } else {
        duel.setSpectator(player, true);
      }
    }
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    if (!Duel.inDuel(event.getPlayer())) {
      return;
    }

    if (Config.get().getBoolean("duels.enable-teleports")) {
      return;
    }

    if (event.getPlayer().hasPermission("auroraduels.bypass.teleports")) {
      return;
    }

    event.getPlayer().sendMessage(Lang.getComponent("no-teleport"));

    event.setCancelled(true);
  }
}
