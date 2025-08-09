package club.aurorapvp.auroraduels.commands.duels.requests;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.DuelRequest;
import club.aurorapvp.auroraduels.modules.DuelStats;
import club.aurorapvp.auroraduels.modules.duels.Duel;
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
    if (!DuelRequest.hasPendingRequest(player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("no-requests"));
      return;
    }

    Player requester = null;

    if (requesterName != null) {
      requester = Bukkit.getPlayer(requesterName);
    }

    if (requester == null) {
      DuelRequest duelRequest = DuelRequest.getRequest(player);

      for (DuelRequest pendingDuelRequest : DuelRequest.getRequests(player)) {
        if (pendingDuelRequest != duelRequest) {
          pendingDuelRequest.deny();
        }
      }

      assert duelRequest != null;
      duelRequest.accept(player);
      return;
    }

    if (!DuelRequest.hasPendingRequest(requester, player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("no-requests"));
      return;
    }

    DuelRequest duelRequest = DuelRequest.getRequest(requester, player);

    for (DuelRequest pendingDuelRequest : DuelRequest.getRequests(player)) {
      if (pendingDuelRequest != duelRequest) {
        pendingDuelRequest.deny();
      }
    }

    assert duelRequest != null;
    duelRequest.accept(player);
  }

  @Subcommand("deny")
  @CommandPermission("auroraduels.command.duel.deny")
  @Syntax("<player>")
  @CommandCompletion("@players")
  @Description("Denies the duel request")
  @SuppressWarnings("unused")
  public void onDeny(Player player, @Optional String requesterName) {
    if (!DuelRequest.hasPendingRequest(player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("no-requests"));
      return;
    }

    Player requester = null;

    if (requesterName != null) {
      requester = Bukkit.getPlayer(requesterName);
    }

    if (requester == null) {
      DuelRequest duelRequest = DuelRequest.getRequest(player);

      assert duelRequest != null;
      duelRequest.deny();
      return;
    }

    if (!DuelRequest.hasPendingRequest(requester, player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("no-requests"));
      return;
    }

    Objects.requireNonNull(DuelRequest.getRequest(requester, player)).deny();
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

  @Subcommand("stats")
  @CommandPermission("auroraduels.command.duel.stats")
  @Syntax("[n]")
  @Description("Views your duel stats")
  @SuppressWarnings("unused")
  public void onStats(Player player, @Optional String nStr) {
    int index = 0;

    if (nStr != null) {
      try {
        int n = Integer.parseInt(nStr);
        if (n < 1) {
          player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("invalid-number"));
          return;
        }
        index = n - 1;
      } catch (NumberFormatException e) {
        player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("invalid-number"));
        return;
      }
    }

    DuelStats duelStats = DuelStats.get(player);
    DuelStats.Stats stats = duelStats.getStats(index);

    if (stats == null) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("no-stats-available"));
      return;
    }

    if (AuroraDuels.getInstance().isAuroraCombatInstalled()) {
      player.sendMessage(
          AuroraDuels.getInstance()
              .getLang()
              .formatComponent(
                  "duel-summary-elo",
                  player.getName(),
                  stats.getElo(),
                  stats.getTotemPops(),
                  stats.getDamageDealt(),
                  stats.getDamageReceived(),
                  stats.getHealthRemaining()));
    } else {
      player.sendMessage(
          AuroraDuels.getInstance()
              .getLang()
              .formatComponent(
                  "duel-summary",
                  player.getName(),
                  stats.getTotemPops(),
                  stats.getDamageDealt(),
                  stats.getDamageReceived(),
                  stats.getHealthRemaining()));
    }
  }
}
