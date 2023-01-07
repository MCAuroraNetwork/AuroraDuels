package club.aurorapvp.listeners;

import club.aurorapvp.modules.arenas.Arena;
import club.aurorapvp.modules.Queue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {
  private Player p;
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (sender instanceof Player) {
      p = (Player) sender;
    }

    switch (command.getName()) {
      case "duel" -> Queue.enterDuelQueue(p, args[0]);
      case "createarena" -> Arena.create(p, new Arena(args[0]));
      case "resetarena" -> Arena.reset(p, new Arena(args[0]));
      case "deletearena" -> Arena.delete(p, new Arena(args[0]));
    }
    return true;
  }
}
