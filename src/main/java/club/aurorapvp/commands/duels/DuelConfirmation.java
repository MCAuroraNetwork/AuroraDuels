package club.aurorapvp.commands.duels;

import club.aurorapvp.modules.Request;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DuelConfirmation implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player p)) {
      return false;
    }

    if (args[0].equals("accept")) {
      Request.acceptDuelRequest(p);
    } else if (args[0].equals("deny")) {
      Request.denyDuelRequest(p);
    }

    return true;
  }
}
