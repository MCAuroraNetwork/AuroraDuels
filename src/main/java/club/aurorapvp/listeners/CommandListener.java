package club.aurorapvp.listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static club.aurorapvp.modules.ArenaModule.*;

public class CommandListener implements CommandExecutor {
  private Player p;
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (sender instanceof Player) {
      p = (Player) sender;
    }

    switch (command.getName()) {
      case "createarena" -> {
        try {
          createArena(args[0],p);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      case "resetarena" -> resetArena(args[0], p);
      case "deletearena" -> deleteArena(args[0], p);
    }
    return true;
  }
}
