package club.aurorapvp.auroraduels.commands.duels.requests;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.aurorawarps.modules.WorldSpawn;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@CommandAlias("duel")
@CommandPermission("auroraduels.command.duel")
public class StandardDuelCommands extends BaseCommand {

  @Default
  @CommandCompletion("@players @int")
  @Syntax("<player>")
  @Description("Sends a duel request to another player")
  @SuppressWarnings("unused")
  public void onNewRequest(Player player, String playerName, @Optional String str) {
    Player requested = Bukkit.getPlayer(playerName);

    if (requested == null || requested == player) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("unknown-opponent"));
      return;
    }

      DuelQueue.findRespawnLocation(player);

      int rounds = 1;

    if (str != null && str.matches("\\d+")) {
      rounds = Integer.parseInt(str);

      if (rounds % 2 == 0) {
        player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("even-rounds"));
        rounds--;
      }

      if (rounds <= 1) {
        rounds = 1;
      }
    }

    new Request(player, new StandardDuel(player, requested, loc, false, rounds));
  }
}
