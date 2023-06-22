package club.aurorapvp.auroraduels.commands.duels;

import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.modules.requests.PartyRequest;
import club.aurorapvp.auroraduels.modules.requests.StandardRequest;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Split;
import co.aikar.commands.annotation.Syntax;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("partyduel")
public class PartyDuelCommands extends BaseCommand {
  @Default
  @CommandCompletion("@players @types @queues")
  @Syntax("[player1, player2, player3] [flag] [queue/kit]")
  @Description("Sends a duel request to another player")
  @SuppressWarnings("unused")
  public void onNewRequest(Player p, @Split String[] playerNames, @Optional String... params) {
    if (playerNames.length < 1) {
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

    if (playerNames.length == 1) {
      Player requested = Bukkit.getPlayer(playerNames[0]);

      if (requested == null) {
        p.sendMessage(Lang.getComponent("unknown-opponent"));
        return;
      }

      new StandardRequest(p, requested, queueName, kitName);
      return;
    }

    Player[] requested = new Player[playerNames.length];

    int i = 0;
    for (String playerName : playerNames) {
      requested[i] = Bukkit.getPlayer(playerName);
      i++;
    }

    new PartyRequest(p, Set.of(requested), queueName,  kitName);
  }
}
