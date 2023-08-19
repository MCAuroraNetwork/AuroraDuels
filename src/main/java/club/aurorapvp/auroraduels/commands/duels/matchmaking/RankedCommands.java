package club.aurorapvp.auroraduels.commands.duels.matchmaking;

import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.Objects;
import org.bukkit.entity.Player;

// TODO implement Queue GUI for ranked commands
@CommandAlias("ranked")
public class RankedCommands extends BaseCommand {
  @Default
  @CommandCompletion("@queues")
  @Syntax("<queue>")
  @Description("Joins a provided queue")
  @SuppressWarnings("unused")
  public void onFindMatch(Player player, @Optional String name) {
    Queue queue = Queue.getQueue(Objects.requireNonNullElse(name, "default"));

    if (queue == null) {
      player.sendMessage(Lang.getComponent("queue-not-found"));
      return;
    }

    queue.findMatch(player, true);
  }

  @CatchUnknown
  @SuppressWarnings("unused")
  public void onUnknown(Player player) {
    player.sendMessage(Lang.getComponent("unknown-matchmaking-command"));
  }
}
