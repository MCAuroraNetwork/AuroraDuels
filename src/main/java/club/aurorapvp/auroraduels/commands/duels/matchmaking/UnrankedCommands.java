package club.aurorapvp.auroraduels.commands.duels.matchmaking;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.Objects;
import org.bukkit.entity.Player;

@CommandAlias("unranked")
@CommandPermission("auroraduels.command.unranked")
public class UnrankedCommands extends BaseCommand {

  @Default
  @CommandCompletion("@queues")
  @Syntax("<queue>")
  @Description("Joins a provided queue")
  @SuppressWarnings("unused")
  public void onFindMatch(Player player, @Optional String name) {
    Queue queue = Queue.getQueue(Objects.requireNonNullElse(name, "default"));

    if (queue == null) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("queue-not-found"));
      return;
    }

    queue.findMatch(player, false);
  }
}
