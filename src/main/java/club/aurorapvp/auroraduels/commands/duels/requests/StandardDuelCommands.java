package club.aurorapvp.auroraduels.commands.duels.requests;

import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.requests.StandardRequest;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("duel")
public class StandardDuelCommands extends BaseCommand {
  @Default
  @CommandCompletion("@players @int")
  @Syntax("[player]")
  @Description("Sends a duel request to another player")
  @SuppressWarnings("unused")
  public void onNewRequest(Player p, String playerName, @Optional String rounds) {


    Player requested = Bukkit.getPlayer(playerName);

    if (requested == null) {
      p.sendMessage(Lang.getComponent("unknown-opponent"));
      return;
    }

    try {
      if (Integer.parseInt(rounds) > 1) {
        new StandardRequest(p, requested, Integer.parseInt(rounds));
      }
    } catch (NumberFormatException e) {
      new StandardRequest(p, requested);
    }
  }
}
