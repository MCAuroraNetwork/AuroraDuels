package club.aurorapvp.auroraduels.commands.duels.requests;

import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
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
  public void onNewRequest(Player player, String playerName, @Optional String rounds) {
    Player requested = Bukkit.getPlayer(playerName);

    if (requested == null) {
      player.sendMessage(Lang.getComponent("unknown-opponent"));
      return;
    }

    Location loc = Duel.getDuelLobby();

    if (loc == null) {
      WorldSpawn worldSpawn = WorldSpawn.getWorldSpawn(player.getWorld());

      if (worldSpawn != null) {
        loc = worldSpawn.getSpawn();
      }

      if (loc == null) {
        loc = WorldSpawn.getGlobalSpawn();
      }

      if (loc == null) {
        loc = Bukkit.getWorlds().get(0).getSpawnLocation();
      }
    }

    // TODO less jank
    try {
      if (Integer.parseInt(rounds) > 1) {
        new Request(player, new StandardDuel(player, requested, loc, false, Integer.parseInt(rounds)));
      }
    } catch (NumberFormatException e) {
      new Request(player, new StandardDuel(player, requested, loc, false, 1));
    }
  }
}
