package club.aurorapvp.auroraduels.commands.arena;

import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.Arena;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@CommandAlias("arena")
public class ArenaCommands extends BaseCommand {
  @Subcommand("create")
  @Syntax("[name]")
  @Description("Creates an arena")
  @SuppressWarnings("unused")
  public void onCreate(Player p, String name) {
    new Arena(name).startCreation(p);
  }

  @Subcommand("delete")
  @CommandCompletion("@arenas")
  @Syntax("[name]")
  @Description("Deletes an arena")
  @SuppressWarnings("unused")
  public void onDelete(Player p, String name) {
    Arena arena = Arena.getArena(name);

    if (arena != null) {
      arena.delete(p);
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  @Subcommand("reset")
  @CommandCompletion("@arenas")
  @Syntax("[name]")
  @Description("Resets an arena")
  @SuppressWarnings("unused")
  public void onReset(Player p, String name) {
    Arena arena = Arena.getArena(name);

    if (arena != null) {
      arena.reset(p);
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  @Subcommand("list")
  @SuppressWarnings("unused")
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
  @SuppressWarnings("unused")
  public void onUnknown(Player p) {
    p.sendMessage(Lang.getComponent("unknown-arena-command"));
  }
}
