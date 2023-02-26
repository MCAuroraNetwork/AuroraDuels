package club.aurorapvp.commands.duels;

import club.aurorapvp.modules.Queue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FindMatch implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player p)) {
      return false;
    }

    Queue.findMatch(p, args[0]);

    return true;
  }
}
