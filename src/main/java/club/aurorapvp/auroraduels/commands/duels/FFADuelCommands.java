package club.aurorapvp.auroraduels.commands.duels;

import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.requests.FFARequest;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("ffaduel")
public class FFADuelCommands extends BaseCommand {
  @Default
  @CommandCompletion("@players @players @players @players")
  @Syntax("[player1 player2 player3]")
  @Description("Sends a duel request to another player")
  @SuppressWarnings("unused")
  public void onNewRequest(Player p, String[] playerNames) {
    if (playerNames.length < 1) {
      p.sendMessage(Lang.getComponent("not-enough-players"));
      return;
    }

    Player[] requested = new Player[playerNames.length];

    int i = 0;
    for (String playerName : playerNames) {
      requested[i] = Bukkit.getPlayer(playerName);
      i++;
    }

    new FFARequest(p, Set.of(requested));
  }
}
