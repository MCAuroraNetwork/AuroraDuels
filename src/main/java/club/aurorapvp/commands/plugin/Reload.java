package club.aurorapvp.commands.plugin;

import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Queue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Reload implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (args[0].equals("reload")) {
      Config.reload();
      Lang.reload();
      Arena.reloadAll();
      Queue.reloadAll();
    }

    return true;
  }
}
