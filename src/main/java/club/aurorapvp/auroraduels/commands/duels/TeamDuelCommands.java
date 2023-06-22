package club.aurorapvp.auroraduels.commands.duels;

import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.modules.requests.StandardRequest;
import club.aurorapvp.auroraduels.modules.requests.TeamRequest;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Split;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("teamduel")
public class TeamDuelCommands extends BaseCommand {
  @Default
  @CommandCompletion("@players @players @types @queues")
  @Syntax("[player1,player2,player3] [player4,player5,player6] [param] [queue/kit]")
  @Description("Sends a duel request to another player")
  @SuppressWarnings("unused")
  public void onNewRequest(Player p, @Split String[] team1Names,
                           @Split String[] team2Names, @Optional String... params) {
    if (team1Names.length < 1) {
      p.sendMessage(Lang.getComponent("not-enough-players"));
      return;
    }

    String queueName = "default";
    String kitName = null;

    for (int i = 0; i < params.length; i++) {
      if (params[i].equals("-k")) {
        kitName = params[i + 1];
      } else if (params[i].equals("-q")) {
        queueName = params[i + 1];
      }
    }

    if (team2Names.length < 1 && team1Names.length == 1) {
      Player requested = Bukkit.getPlayer(team1Names[0]);

      if (requested == null) {
        p.sendMessage(Lang.getComponent("unknown-opponent"));
        return;
      }

      new StandardRequest(p, requested, queueName, kitName);
      return;
    }

    Player[] team1 = new Player[team1Names.length];
    Player[] team2 = new Player[team2Names.length];

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

    new TeamRequest(p, team1, team2, queueName, kitName);
  }
}