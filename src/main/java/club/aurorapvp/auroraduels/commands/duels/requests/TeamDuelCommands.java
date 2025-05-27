package club.aurorapvp.auroraduels.commands.duels.requests;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.DuelRequest;
import club.aurorapvp.auroraduels.modules.duels.RandomTeamDuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.teamduels.TeamDuel;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;

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
  public void onNewRequest(
      Player player, @Split String[] team1Names, @Optional @Split String[] team2Names) {
    if (team1Names.length == 0) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("not-enough-players"));
      return;
    }

    Set<Player> team1 = new HashSet<>();

    for (String playerName : team1Names) {
      if (playerName.equals(player.getName())) {
        player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("unknown-opponent"));
        return;
      }
      Player teamPlayer = Bukkit.getPlayer(playerName);
      if (teamPlayer == null) {
        player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("unknown-opponent"));
        return;
      }
      team1.add(teamPlayer);
    }

    if (team2Names == null && !team1.contains(player)) {
      Location loc = DuelQueue.findRespawnLocation(player);

      new DuelRequest(player, new TeamDuel(team1, Set.of(player), loc, false));

      return;
    } else if (team2Names == null) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("not-enough-players"));
      return;
    }

    Set<Player> team2 = new HashSet<>();

    for (String playerName : team2Names) {
      Player teamPlayer = Bukkit.getPlayer(playerName);
      if (teamPlayer == null) {
        player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("unknown-opponent"));
        return;
      }
      team2.add(teamPlayer);
    }

    if (!team1.contains(player) && !team2.contains(player)) {
      team1.add(player);
    }

    Location loc = DuelQueue.findRespawnLocation(player);

    new DuelRequest(player, new TeamDuel(team1, team2, loc, false));
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
      Player requestedPlayer = Bukkit.getPlayer(playerName);
      if (requestedPlayer == null) {
        player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("unknown-opponent"));
        return;
      }
      requested[i] = requestedPlayer;
      i++;
    }

    Set<Player> players = new HashSet<>(Set.of(requested));
    players.add(player);

    Location loc = DuelQueue.findRespawnLocation(player);

    if (requested.length == 1) {
      new DuelRequest(player, new StandardDuel(player, requested[0], loc, false, 1));
      return;
    }

    new DuelRequest(player, new RandomTeamDuel(players, loc, false));
  }
}
