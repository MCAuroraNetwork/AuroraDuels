package club.aurorapvp.auroraduels.commands;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import java.util.Set;
import java.util.TreeSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

@CommandAlias("arena")
@CommandPermission("auroraduels.command.arena")
public class ArenaCommands extends BaseCommand {

  @Subcommand("create")
  @CommandPermission("auroraduels.command.arena.create")
  @Syntax("[name]")
  @Description("Creates an arena")
  @SuppressWarnings("unused")
  public void onCreate(Player player, String name) {
    new Arena(name).startCreation(player);
  }

  @Subcommand("delete")
  @CommandPermission("auroraduels.command.arena.delete")
  @CommandCompletion("@arenas")
  @Syntax("[name]")
  @Description("Deletes an arena")
  @SuppressWarnings("unused")
  public void onDelete(Player player, String name) {
    Arena arena = Arena.getArena(name);

    if (arena != null) {
      arena.delete(player);
    } else {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("arena-does-not-exist"));
    }
  }

  @Subcommand("reset")
  @CommandPermission("auroraduels.command.arena.reset")
  @CommandCompletion("@arenas")
  @Syntax("[name]")
  @Description("Resets an arena")
  @SuppressWarnings("unused")
  public void onReset(Player player, String name) {
    Arena arena = Arena.getArena(name);

    if (arena != null) {
      arena.reset(player);
    } else {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("arena-does-not-exist"));
    }
  }

  @Subcommand("list")
  @CommandPermission("auroraduels.command.arena.list")
  @SuppressWarnings("unused")
  public void onList(Player player) {
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
}
