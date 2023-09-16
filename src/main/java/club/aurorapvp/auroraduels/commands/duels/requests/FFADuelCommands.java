package club.aurorapvp.auroraduels.commands.duels.requests;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.FFADuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.aurorawarps.modules.WorldSpawn;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@CommandAlias("ffaduel")
@CommandPermission("auroraduels.command.ffaduel")
public class FFADuelCommands extends BaseCommand {

  @Default
  @CommandCompletion("@players @players @players @players")
  @Syntax("<player1 player2 player3>")
  @Description("Sends a duel request to other players")
  @SuppressWarnings("unused")
  public void onNewRequest(Player player, String[] playerNames) {
    if (playerNames.length < 1) {
      player.sendMessage(AuroraDuels.INSTANCE.getLang().getComponent("not-enough-players"));
      return;
    }

    Player[] requested = new Player[playerNames.length];

    int i = 0;
    for (String playerName : playerNames) {
      if (playerName.equals(player.getName())) {
        player.sendMessage(AuroraDuels.INSTANCE.getLang().getComponent("unknown-opponent"));
        return;
      }

      requested[i] = Bukkit.getPlayer(playerName);
      i++;
    }

    Set<Player> players = new HashSet<>(Set.of(requested));
    players.add(player);

    Location loc = Duel.getDuelLobby();

    if (loc == null && AuroraDuels.isAuroraWarpsInstalled()) {
      WorldSpawn worldSpawn = WorldSpawn.getWorldSpawn(player.getWorld());

      if (worldSpawn != null) {
        loc = worldSpawn.getSpawn();
      }

      if (loc == null) {
        loc = WorldSpawn.getGlobalSpawn();
      }
    }

    if (loc == null) {
      loc = Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    if (requested.length == 1) {
      new Request(player, new StandardDuel(player, requested[0], loc, false, 1));
      return;
    }

    new Request(player, new FFADuel(players, loc, false));
  }
}
