package club.aurorapvp.listeners;

import static club.aurorapvp.modules.ArenaModule.createArena;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandListener implements CommandExecutor {
  private Player p;
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (sender instanceof Player) {
      p = (Player) sender;
    }

    switch (command.getName()) {
      case "createarena" -> createArena(p);
    }
    return true;
  }
}
