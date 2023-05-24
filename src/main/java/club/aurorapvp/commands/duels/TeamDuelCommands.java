package club.aurorapvp.commands.duels;

import club.aurorapvp.modules.duels.TeamDuel;
import club.aurorapvp.modules.requests.PartyRequest;
import club.aurorapvp.modules.requests.Request;
import club.aurorapvp.modules.requests.TeamRequest;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Split;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("teamduel")
public class TeamDuelCommands extends DuelBaseCommand {
  @Default
  @CommandCompletion("@players @queues")
  @Syntax("[player1, player2, player3] [queue]")
  @Description("Sends a duel request to another player")
  public void onNewRquest(Player p, @Split(",") String[] team1Names, @Split(",") String[] team2Names, @Optional String queueName) {
    if (team1Names.length < 2 && team2Names.length < 2) {
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

    new TeamRequest(Objects.requireNonNullElse(queueName, "Default"), p, team1, team2);
  }


  @CatchUnknown
  @Subcommand("help")
  public void onUnknown() {

  }
}