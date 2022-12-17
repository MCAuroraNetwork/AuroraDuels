package club.aurorapvp.listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static club.aurorapvp.AuroraDuels.DataFolder;
import static club.aurorapvp.datahandlers.SchematicsHandler.pasteArenaSchematic;
import static club.aurorapvp.modules.ArenaModule.createArena;

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
      case "resetarena" ->
        pasteArenaSchematic(new File(DataFolder, "/arenas/" + args[0] + ".schem"));
    }
    return true;
  }
}
