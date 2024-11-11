package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroracombat.events.custom.PlayerDamagedByPlayerEvent;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.Wand;
import club.aurorapvp.auroraduels.modules.duels.*;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.aurorakits.events.custom.KitGiveEvent;
import club.aurorapvp.aurorakits.events.custom.KitRefillEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerEventListener implements Listener {

  public final static Set<Player> DUEL_RESPAWN = new HashSet<>();

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Winstracker.register(event.getPlayer());
  }

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
    if (DuelQueue.isCreator(event.getPlayer()) && !DuelQueue.isArenasSet()) {
      DuelQueue.getUnFinishedQueue()
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

    boolean commandMatched = false;

    if (!AuroraDuels.getInstance().getConfig().getBoolean("duels.commands.allow-commands")) {
      for (String command : AuroraDuels.getInstance().getConfig()
          .getStringList("duels.commands.whitelisted")) {
        if (event.getMessage().equals("/" + command)) {
          commandMatched = true;
          break;
        }
      }
    }

    if (commandMatched) {
      return;
    }

    if (event.getPlayer().hasPermission("auroraduels.bypass.commands")) {
      return;
    }

    event.getPlayer()
        .sendMessage(AuroraDuels.getInstance().getLang().getComponent("commands-disabled"));

    event.setCancelled(true);
  }

  @EventHandler
  public void onKitUse(KitGiveEvent event) {
    if (Duel.inDuel(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onKitRefill(KitRefillEvent event) {
    if (Duel.inDuel(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (Duel.inDuelCountdown(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerDamaged(PlayerDamagedByPlayerEvent event) {
    if (event.getDamaged().equals(event.getDamager())) {
      return;
    }

    if (!Duel.inDuel(event.getDamaged())) {
      return;
    }

    Duel duel = Duel.getDuel(event.getDamaged());

    if (!(duel instanceof TeamDuel teamDuel)) {
      return;
    }

    for (Player teamMate : teamDuel.getTeammates(event.getDamaged())) {
      if (teamMate.equals(event.getDamager())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onPlayerRespawn(PlayerPostRespawnEvent event) {
    if (DUEL_RESPAWN.remove(event.getPlayer())) {
      event.getPlayer().teleportAsync(
          Objects.requireNonNull(Duel.getLastDuel(event.getPlayer())).getRespawnLocation());
    }

    if (!Duel.inDuel(event.getPlayer())) {
      return;
    }

    Duel duel = Duel.getDuel(event.getPlayer());

    assert duel != null;
    if (duel.isSpectating(event.getPlayer())) {
      Location spawn1 =
          new Location(
              duel.getArena().getWorld(),
              duel.getArena().getFirstSpawn().x(),
              duel.getArena().getFirstSpawn().y(),
              duel.getArena().getFirstSpawn().z());

      event.getPlayer().teleportAsync(spawn1);
    }
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
    if (DuelQueue.inQueue(event.getPlayer())) {
      DuelQueue.removeFromQueue(event.getPlayer());
    }

    if (Request.hasPendingRequest(event.getPlayer())) {
      Objects.requireNonNull(Request.getRequest(event.getPlayer())).deny();
    }

    if (Duel.inDuel(event.getPlayer())) {
      checkIfDuelOver(event.getPlayer());
    }

    Winstracker.unregister(event.getPlayer());
  }

  public void checkIfDuelOver(Player player) {
    Duel duel = Duel.getDuel(player);

    if (duel instanceof StandardDuel standardDuel) {
      standardDuel.endDuel(Set.of(standardDuel.getOpponent(player)), Set.of(player));
    } else if (duel instanceof TeamDuel teamDuel) {
      teamDuel.setDead(player);

      if (teamDuel.isTeamDead(player)) {
        duel.endDuel(teamDuel.getOpponents(player), teamDuel.getTeammates(player));
      } else {
        duel.setSpectator(player, true);
      }
    } else if (duel instanceof FFADuel ffaDuel) {
      ffaDuel.setDead(player);

      if (ffaDuel.isOnePlayerAlive()) {
        Set<Player> losers = new HashSet<>(ffaDuel.getPlayers());
        losers.remove(ffaDuel.getFirstAlive());

        duel.endDuel(Set.of(ffaDuel.getFirstAlive()), losers);
      } else {
        duel.setSpectator(player, true);
      }
    }
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    if (Duel.inDuelCountdown(event.getPlayer())) {
      event.setCancelled(true);
    }

    if (!Duel.inDuel(event.getPlayer())) {
      return;
    }

    if (AuroraDuels.getInstance().getConfig().getBoolean("duels.enable-teleports")) {
      return;
    }

    if (event.getPlayer().hasPermission("auroraduels.bypass.teleports")) {
      return;
    }

    event.getPlayer().sendMessage(AuroraDuels.getInstance().getLang().getComponent("no-teleport"));

    event.setCancelled(true);
  }
}
