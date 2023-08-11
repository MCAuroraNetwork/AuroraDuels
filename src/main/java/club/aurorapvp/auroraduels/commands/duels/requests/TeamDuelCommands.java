package club.aurorapvp.auroraduels.commands.duels.requests;

import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
import club.aurorapvp.aurorawarps.modules.WorldSpawn;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Split;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@CommandAlias("teamduel")
public class TeamDuelCommands extends BaseCommand {
  @Default
  @CommandCompletion("@players @players")
  @Syntax("[player1,player2,player3] [player4,player5,player6] [param] [queue/kit]")
  @Description("Sends a duel request to another player")
  @SuppressWarnings("unused")
  public void onNewRequest(Player p, @Split String[] team1Names, @Split String[] team2Names) {
    if (team1Names.length == 0 || team2Names.length == 0) {
      p.sendMessage(Lang.getComponent("not-enough-players"));
      return;
    }

    Player[] team1 = new Player[team1Names.length + 1];
    Player[] team2 = new Player[team2Names.length];

    team1[team1Names.length - 1] = p;

    int i = 0;
    for (String playerName : team1Names) {
      team1[i] = Bukkit.getPlayer(playerName);
      i++;
    }

    i = 0;
    for (String playerName : team2Names) {
      team2[i] = Bukkit.getPlayer(playerName);
      i++;
    }

    WorldSpawn worldSpawn = WorldSpawn.getWorldSpawn(p.getWorld());
    Location loc = null;

    if (worldSpawn != null) {
      loc = worldSpawn.getSpawn();
    }

    if (loc == null) {
      loc = WorldSpawn.getGlobalSpawn();
    }

    new Request(p, new TeamDuel(team1, team2, loc, false));
  }
}
