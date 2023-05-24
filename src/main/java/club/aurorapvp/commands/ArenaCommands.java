package club.aurorapvp.commands;

import club.aurorapvp.configs.Lang;
import club.aurorapvp.modules.Arena;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("arena")
public class ArenaCommands extends BaseCommand {
  @Subcommand("create")
  @CommandPermission("auroraduels.admin")
  @Syntax("[name]")
  @Description("Creates an arena")
  public void onCreate(Player p, String name) {
    new Arena(name).startCreation(p);
  }

  @Subcommand("delete")
  @CommandPermission("auroraduels.admin")
  @CommandCompletion("@arenas")
  @Syntax("[name]")
  @Description("Deletes an arena")
  public void onDelete(Player p, String name) {
    Arena arena = Arena.getArena(name);

    if (arena != null) {
      arena.delete(p);
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  @Subcommand("reset")
  @CommandPermission("auroraduels.admin")
  @CommandCompletion("@arenas")
  @Syntax("[name]")
  @Description("Resets an arena")
  public void onReset(Player p, String name) {
    Arena arena = Arena.getArena(name);

    if (arena != null) {
      arena.reset(p);
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  @Subcommand("list")
  public void onList(Player p) {
    // TODO clean up or remove
    StringBuilder str = new StringBuilder("Arenas:");
    for (Arena arena : Arena.getArenas()) {
      str.append("\n").append(arena.getName());
    }
    p.sendMessage(Component.text(str.toString()));
  }

  @CatchUnknown
  @Subcommand("help")
  public void onUnknown() {

  }
}
