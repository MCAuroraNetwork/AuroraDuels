package club.aurorapvp.commands.duels;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.modules.Kit;
import club.aurorapvp.modules.Queue;
import club.aurorapvp.modules.duels.Duel;
import club.aurorapvp.modules.requests.StandardRequest;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import java.util.Objects;
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
  @CommandCompletion("@players @queues")
  @Syntax("[player] [queue]")
  @Description("Sends a duel request to another player")
  @SuppressWarnings("unused")
  public void onNewRequest(Player p, String playerName, @Optional String queueName) {
    new StandardRequest(Objects.requireNonNullElse(queueName, "default"), p,
        Bukkit.getPlayer(playerName));
  }
}
