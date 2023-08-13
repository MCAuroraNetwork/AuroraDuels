package club.aurorapvp.auroraduels.commands.duels.requests;

import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
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

@CommandAlias("duel|teamduel|partyduel")
public class DuelBaseCommands extends BaseCommand {
  @Subcommand("accept")
  @Syntax("[player]")
  @CommandCompletion("@players")
  @Description("Accepts the duel request")
  @SuppressWarnings("unused")
  public void onAccept(Player p, @Optional String requesterName) {
    if (!Request.hasPendingRequest(p)) {
      p.sendMessage(Lang.getComponent("no-requests"));
      return;
    }

    Player requester = null;

    if (requesterName != null) {
      requester = Bukkit.getPlayer(requesterName);
    }

    if (requester == null) {
      Request request = Request.getRequest(p);

      for (Request pendingRequest : Request.getRequests(p)) {
        if (pendingRequest != request) {
          pendingRequest.deny();
        }
      }

      assert request != null;
      request.accept(p);
      return;
    }

    if (!Request.hasPendingRequest(requester, p)) {
      p.sendMessage(Lang.getComponent("no-requests"));
      return;
    }

    Request request = Request.getRequest(requester, p);

    for (Request pendingRequest : Request.getRequests(p)) {
      if (pendingRequest != request) {
        pendingRequest.deny();
      }
    }

    assert request != null;
    request.accept(p);
  }

  @Subcommand("deny")
  @Syntax("[player]")
  @CommandCompletion("@players")
  @Description("Denies the duel request")
  @SuppressWarnings("unused")
  public void onDeny(Player p, @Optional String requesterName) {
    if (!Request.hasPendingRequest(p)) {
      p.sendMessage(Lang.getComponent("no-requests"));
      return;
    }

    Player requester = null;

    if (requesterName != null) {
      requester = Bukkit.getPlayer(requesterName);
    }

    if (requester == null) {
      Request request = Request.getRequest(p);

      assert request != null;
      request.deny();
      return;
    }

    if (!Request.hasPendingRequest(requester, p)) {
      p.sendMessage(Lang.getComponent("no-requests"));
      return;
    }

    Objects.requireNonNull(Request.getRequest(requester, p)).deny();
  }

  @Subcommand("spectate")
  @CommandCompletion("@players")
  @Syntax("[player]")
  @Description("Spectates the duel of a given player")
  @SuppressWarnings("unused")
  public void onSetSpectator(Player p, String player) {
    Duel duel = Duel.getDuel(Bukkit.getPlayer(player));

    if (!Duel.inDuel(p) && duel != null) {
      duel.setSpectator(p, true);
    } else {
      p.sendMessage(Lang.getComponent("unknown-duel"));
    }
  }

  @Subcommand("kit")
  @CommandCompletion("@kits")
  @Description("Sets the duel kit")
  @SuppressWarnings("unused")
  public void onKitSet(Player p, @Optional String kitName) {
    if (kitName != null && Kit.getKit(p, kitName) != null) {
      Duel.setDuelKit(p, kitName);
    } else {
      Duel.setDuelKit(p, Kit.getLastUsedKit(p).getName());
    }
  }

  @Subcommand("lobby")
  @Description("Sets the duel lobby")
  @CommandPermission("auroraduels.admin")
  @SuppressWarnings("unused")
  public void onLobbySet(Player p) {
    Duel.setDuelLobby(p.getLocation());
    p.sendMessage(Lang.getComponent("duel-lobby-set"));
  }

  @CatchUnknown
  @SuppressWarnings("unused")
  public void onUnknown(Player p) {
    p.sendMessage(Lang.getComponent("unknown-duel-command"));
  }
}
