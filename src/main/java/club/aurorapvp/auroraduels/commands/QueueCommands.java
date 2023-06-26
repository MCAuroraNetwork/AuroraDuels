package club.aurorapvp.auroraduels.commands;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.entity.Player;

@CommandAlias("queue")
public class QueueCommands extends BaseCommand {
  @Subcommand("create")
  @Syntax("[name]")
  @Description("Creates a queue")
  @SuppressWarnings("unused")
  public void onCreate(Player p, String name, @Optional String kitName) {
    if (kitName != null && AuroraDuels.isAuroraKitsInstalled()) {
      new Queue(name).create(p, kitName);
    } else {
      new Queue(name).create(p);
    }
  }

  @CatchUnknown
  @Subcommand("help")
  @SuppressWarnings("unused")
  public void onUnknown(Player p) {
    p.sendMessage(Lang.getComponent("unknown-queue-command"));
  }
}
