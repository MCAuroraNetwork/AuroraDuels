package club.aurorapvp.auroraduels.commands.duels.requests;

import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.requests.Request;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
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
  @Description("Accepts the duel request")
  @SuppressWarnings("unused")
  public void onAccept(Player p) {
    Request.acceptDuelRequest(p);
  }

  @Subcommand("accept")
  @Description("Accepts the duel request")
  @SuppressWarnings("unused")
  public void onAccept(Player p, @Optional String requesterName) {
    Player requester = null;

    if (requesterName != null) {
      requester = Bukkit.getPlayer(requesterName);
    }
    if (requester == null || !Request.hasPendingRequest(requester, p)) {
      p.sendMessage(Lang.getComponent("no-requests"));
      return;
    }

    Request request = Request.getRequest(requester, p);

    Request.acceptDuelRequest(p, request);
  }

  @Subcommand("deny")
  @Description("Denies the duel request")
  @SuppressWarnings("unused")
  public void onDeny(Player p) {
    Request.denyDuelRequest(p);
  }

  @Subcommand("deny")
  @Description("Accepts the duel request")
  @SuppressWarnings("unused")
  public void onDeny(Player p, @Optional String requesterName) {
    Player requester = null;

    if (requesterName != null) {
      requester = Bukkit.getPlayer(requesterName);
    }

    if (requester == null || !Request.hasPendingRequest(requester, p)) {
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

  @CatchUnknown
  @Subcommand("help")
  @SuppressWarnings("unused")
  public void onUnknown(Player p) {
    p.sendMessage(Lang.getComponent("unknown-duel-command"));
  }
}