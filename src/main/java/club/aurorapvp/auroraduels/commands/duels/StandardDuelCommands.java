package club.aurorapvp.auroraduels.commands.duels;

import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.requests.StandardRequest;
import club.aurorapvp.aurorakits.modules.Kit;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("duel")
public class StandardDuelCommands extends BaseCommand {
  @Subcommand("kit")
  @Description("Sets the duel kit")
  @SuppressWarnings("unused")
  public void onKitSet(Player p, @Optional String kitName) {
    if (kitName != null && Kit.getKit(p, kitName) != null) {
      Duel.setDuelKit(p, kitName);
    } else {
      Duel.setDuelKit(p, Kit.getLastUsedKit(p).getName());
    }
  }

  @Default
  @CommandCompletion("@players @types @queues")
  @Syntax("[player]")
  @Description("Sends a duel request to another player")
  @SuppressWarnings("unused")
  public void onNewRequest(Player p, String playerName) {
    Player requested = Bukkit.getPlayer(playerName);

    if (requested == null) {
      p.sendMessage(Lang.getComponent("unknown-opponent"));
      return;
    }

    new StandardRequest(p, requested);
  }
}
