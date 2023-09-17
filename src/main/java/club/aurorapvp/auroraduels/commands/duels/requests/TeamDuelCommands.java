package club.aurorapvp.auroraduels.commands.duels.requests;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.RandomTeamDuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
import club.aurorapvp.aurorawarps.modules.WorldSpawn;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Split;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@CommandAlias("teamduel")
@CommandPermission("auroraduels.command.teamduel")
public class TeamDuelCommands extends BaseCommand {

  @Default
  @CommandCompletion("@players @players")
  @Syntax("<player1,player2,player3> <player4,player5,player6>")
  @Description("Sends a duel request to a set of two teams of players")
  @SuppressWarnings("unused")
  public void onNewRequest(Player player, @Split String[] team1Names, @Split String[] team2Names) {
    if (team1Names.length == 0 || team2Names.length == 0) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("not-enough-players"));
      return;
    }

    Player[] team1 = new Player[team1Names.length + 1];
    Player[] team2 = new Player[team2Names.length];

    team1[team1.length - 1] = player;

    int i = 0;
    for (String playerName : team1Names) {
      if (playerName.equals(player.getName())) {
        player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("unknown-opponent"));
        return;
      }

      team1[i] = Bukkit.getPlayer(playerName);
      i++;
    }

    i = 0;
    for (String playerName : team2Names) {
      team2[i] = Bukkit.getPlayer(playerName);
      i++;
    }

    Location loc = Duel.getDuelLobby();

    if (loc == null && AuroraDuels.getInstance().isAuroraWarpsInstalled()) {
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

    new Request(player, new TeamDuel(team1, team2, loc, false));
  }

  @Subcommand("random|rand")
  @CommandPermission("auroraduels.command.teamduel.random")
  @CommandCompletion("@players")
  @Syntax("<player1 player2 player3>")
  @Description("Sends a duel request to other players")
  @SuppressWarnings("unused")
  public void onNewRandRequest(Player player, String[] playerNames) {
    if (playerNames.length < 1) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("not-enough-players"));
      return;
    }

    Player[] requested = new Player[playerNames.length];

    int i = 0;
    for (String playerName : playerNames) {
      if (playerName.equals(player.getName())) {
        player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("unknown-opponent"));
        return;
      }

      requested[i] = Bukkit.getPlayer(playerName);
      i++;
    }

    Set<Player> players = new HashSet<>(Set.of(requested));
    players.add(player);

    Location loc = Duel.getDuelLobby();

    if (loc == null && AuroraDuels.getInstance().isAuroraWarpsInstalled()) {
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

    new Request(player, new RandomTeamDuel(players, loc, false));
  }
}
