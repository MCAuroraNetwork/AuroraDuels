package club.aurorapvp.commands.duels;

import club.aurorapvp.modules.Request;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DuelRequest implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player p)) {
      return false;
    }

    if (args[0].equals("accept") || args[0].equals("deny")) {
      return false;
    }

    if (args.length > 1) {
      new Request(args[1], p, Bukkit.getPlayer(args[0]));
    } else {
      new Request("Default", p, Bukkit.getPlayer(args[0]));
    }

    return false;
  }
}
