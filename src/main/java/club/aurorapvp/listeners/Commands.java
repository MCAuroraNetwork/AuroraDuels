package club.aurorapvp.listeners;

import club.aurorapvp.configs.Lang;
import club.aurorapvp.datahandlers.Schematics;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Duel;
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
      case "createarena" -> {
        Arena arena = new Arena(args[0]);
        arena.create(p, arena);
      }
      case "resetarena" -> {
        Arena arena = new Arena(args[0]);
        arena.reset(p);
      }
      case "deletearena" -> Arena.delete(p, new Arena(args[0]));
    }
    return true;
  }
}
