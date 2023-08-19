package club.aurorapvp.auroraduels.commands;

import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.Arena;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import java.util.Set;
import java.util.TreeSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

@CommandAlias("arena")
public class ArenaCommands extends BaseCommand {
  @Subcommand("create")
  @Syntax("[name]")
  @Description("Creates an arena")
  @SuppressWarnings("unused")
  public void onCreate(Player player, String name) {
    new Arena(name).startCreation(player);
  }

  @Subcommand("delete")
  @CommandCompletion("@arenas")
  @Syntax("[name]")
  @Description("Deletes an arena")
  @SuppressWarnings("unused")
  public void onDelete(Player player, String name) {
    Arena arena = Arena.getArena(name);

    if (arena != null) {
      arena.delete(player);
    } else {
      player.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  @Subcommand("reset")
  @CommandCompletion("@arenas")
  @Syntax("[name]")
  @Description("Resets an arena")
  @SuppressWarnings("unused")
  public void onReset(Player player, String name) {
    Arena arena = Arena.getArena(name);

    if (arena != null) {
      arena.resetAsync(player);
    } else {
      player.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  @Subcommand("list")
  @SuppressWarnings("unused")
  public void onList(Player player) {
    // TODO clean up or remove
    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow><bold>Arenas:"));

    Set<String> arenaNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    for (Arena arena : Arena.getArenas()) {
      arenaNames.add(arena.getName());
    }

    StringBuilder str = new StringBuilder();
    for (String name : arenaNames) {
      str.append("\n").append(name);
    }

    player.sendMessage(Component.text(str.toString()));
  }

  @CatchUnknown
  @SuppressWarnings("unused")
  public void onUnknown(Player player) {
    player.sendMessage(Lang.getComponent("unknown-arena-command"));
  }
}
