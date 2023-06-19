package club.aurorapvp.commands.duels;

import club.aurorapvp.modules.requests.PartyRequest;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Split;
import co.aikar.commands.annotation.Syntax;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("partyduel")
public class PartyDuelCommands extends BaseCommand {
  @Default
  @CommandCompletion("@players @queues")
  @Syntax("[player1, player2, player3] [queue]")
  @Description("Sends a duel request to another player")
  @SuppressWarnings("unused")
  public void onNewRequest(Player p, @Split String[] playerNames, @Optional String queueName) {
    if (playerNames.length < 2) {
      return;
    }

    Player[] requested = new Player[playerNames.length];

    int i = 0;
    for (String playerName : playerNames) {
      requested[i] = Bukkit.getPlayer(playerName);
      i++;
    }

    new PartyRequest(Objects.requireNonNullElse(queueName, "Default"), p, requested);
  }
}
