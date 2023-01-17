package club.aurorapvp.listeners;

import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Duel;
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
      case "duel" -> {
        if (args[0].equals("accept")) {
          Duel.acceptDuelRequest(p);
        } else if (args[0].equals("deny")) {
          Duel.denyDuelRequest(p);
        } else {
          Duel.sendDuelRequest(p, Bukkit.getPlayer(args[0]));
        }
      }
      case "createarena" -> new Arena(args[0], p);
      case "resetarena" -> new Arena(args[0], p).reset();
      case "deletearena" -> Arena.getArena(args[0]).delete();
      case "findmatch" -> Queue.findMatch(p);
    }
    return true;
  }
}
