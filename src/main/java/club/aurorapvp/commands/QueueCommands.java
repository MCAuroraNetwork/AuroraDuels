package club.aurorapvp.commands;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.modules.Queue;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("queue")
public class QueueCommands extends BaseCommand {
  @Subcommand("create")
  @CommandPermission("auroraduels.admin")
  @Syntax("[name]")
  @Description("Creates a queue")
  public void onCreate(Player p, String name, @Optional String kitName) {
    if (kitName != null && AuroraDuels.isAuroraKitsInstalled()) {
      new Queue(name).create(p, kitName);
    } else {
      new Queue(name).create(p);
    }
  }

  @CatchUnknown
  @Subcommand("help")
  public void onUnknown() {

  }
}
