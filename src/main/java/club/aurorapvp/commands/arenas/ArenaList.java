package club.aurorapvp.commands.arenas;

import club.aurorapvp.modules.Arena;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ArenaList implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player p)) {
      return false;
    }

    // TODO clean up or remove
    StringBuilder str = new StringBuilder("Arenas:");
    for (Arena arena : Arena.getArenas()) {
      str.append("\n").append(arena.getName());
    }
    p.sendMessage(Component.text(str.toString()));

    return false;
  }
}
