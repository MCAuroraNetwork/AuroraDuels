package club.aurorapvp.auroraduels.commands;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.entity.Player;

@CommandAlias("queue")
@CommandPermission("auroraduels.command.queue")
public class QueueCommands extends BaseCommand {

  @Subcommand("create")
  @CommandPermission("auroraduels.command.queue.create")
  @Syntax("[name]")
  @Description("Creates a queue")
  @SuppressWarnings("unused")
  public void onCreate(Player player, String name, @Optional String kitName) {
    if (kitName != null && AuroraDuels.isAuroraKitsInstalled()) {
      new Queue(name).create(player, kitName);
    } else {
      new Queue(name).create(player);
    }
  }
}
