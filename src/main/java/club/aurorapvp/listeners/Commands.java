package club.aurorapvp.listeners;

import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Queue;
import org.bukkit.Bukkit;
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
      case "duel" -> Queue.enterDuelQueue(p, Bukkit.getPlayer(args[0]));
      case "createarena" -> Arena.create(args[0], p);
      case "resetarena" -> Arena.reset(args[0], p);
      case "deletearena" -> Arena.delete(args[0], p);
    }
    return true;
  }
}
