package club.aurorapvp.commands.queues;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.modules.Queue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CreateQueue implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player p)) {
      return false;
    }

    if (args.length > 1 && AuroraDuels.isAuroraKitsInstalled()) {
      new Queue(args[0]).create(p, args[1]);
    } else {
      new Queue(args[0]).create(p);
    }

    return true;
  }
}
