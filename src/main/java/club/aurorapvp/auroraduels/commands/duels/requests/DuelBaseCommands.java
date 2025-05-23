package club.aurorapvp.auroraduels.commands.duels.requests;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("duel|teamduel|ffaduel")
@CommandPermission("auroraduels.command.duel")
public class DuelBaseCommands extends BaseCommand {

  @Subcommand("accept")
  @CommandPermission("auroraduels.command.duel.accept")
  @Syntax("<player>")
  @CommandCompletion("@players")
  @Description("Accepts the duel request")
  @SuppressWarnings("unused")
  public void onAccept(Player player, @Optional String requesterName) {
    if (!Request.hasPendingRequest(player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("no-requests"));
      return;
    }

    Player requester = null;

    if (requesterName != null) {
      requester = Bukkit.getPlayer(requesterName);
    }

    if (requester == null) {
      Request request = Request.getRequest(player);

      for (Request pendingRequest : Request.getRequests(player)) {
        if (pendingRequest != request) {
          pendingRequest.deny();
        }
      }

      assert request != null;
      request.accept(player);
      return;
    }

    if (!Request.hasPendingRequest(requester, player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("no-requests"));
      return;
    }

    Request request = Request.getRequest(requester, player);

    for (Request pendingRequest : Request.getRequests(player)) {
      if (pendingRequest != request) {
        pendingRequest.deny();
      }
    }

    assert request != null;
    request.accept(player);
  }

  @Subcommand("deny")
  @CommandPermission("auroraduels.command.duel.deny")
  @Syntax("<player>")
  @CommandCompletion("@players")
  @Description("Denies the duel request")
  @SuppressWarnings("unused")
  public void onDeny(Player player, @Optional String requesterName) {
    if (!Request.hasPendingRequest(player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("no-requests"));
      return;
    }

    Player requester = null;

    if (requesterName != null) {
      requester = Bukkit.getPlayer(requesterName);
    }

    if (requester == null) {
      Request request = Request.getRequest(player);

      assert request != null;
      request.deny();
      return;
    }

    if (!Request.hasPendingRequest(requester, player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("no-requests"));
      return;
    }

    Objects.requireNonNull(Request.getRequest(requester, player)).deny();
  }

  @Subcommand("spectate")
  @CommandPermission("auroraduels.command.duel.spectate")
  @CommandCompletion("@players")
  @Syntax("<player>")
  @Description("Spectates the duel of a given player")
  @SuppressWarnings("unused")
  public void onSetSpectator(Player player, @Optional String playerName) {
    if (playerName == null) {
      if (Duel.isSpectator(player)) {
        for (Duel duel : Duel.getDuels()) {
          if (duel.isSpectating(player)) {
            duel.setSpectator(player, false);
            return;
          }
        }
      } else {
        player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("unknown-player"));
        return;
      }
    }

    assert playerName != null;
    Duel duel = Duel.getDuel(Bukkit.getPlayer(playerName));

    if (duel != null && (!Duel.inDuel(player) || duel.isSpectating(player))) {
      duel.setSpectator(player, !duel.isSpectating(player));
    } else {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("unknown-duel"));
    }
  }

  @Subcommand("lobby")
  @CommandPermission("auroraduels.command.duel.lobby")
  @Description("Sets the duel lobby")
  @SuppressWarnings("unused")
  public void onLobbySet(Player player) {
    Duel.setDuelLobby(player.getLocation());
    player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("duel-lobby-set"));
  }
}
