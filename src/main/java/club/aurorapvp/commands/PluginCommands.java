package club.aurorapvp.commands;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Queue;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;

@CommandAlias("auroraduels")
public class PluginCommands extends BaseCommand {
  @Subcommand("reload")
  @CommandPermission("auroraduels.admin")
  @Description("Reloads all plugin data")
  @SuppressWarnings("unused")
  public void onReload() {
    long startTime = System.currentTimeMillis();

    Config.reload();
    Lang.reload();
    Arena.reloadAll();
    Queue.reloadAll();

    AuroraDuels.INSTANCE.getLogger().info(
        "AuroraDuels reloaded in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @CatchUnknown
  @Subcommand("help")
  @Description("Lists all available plugin commands and their function")
  @SuppressWarnings("unused")
  public void onHelp(Player p) {
    p.sendMessage(Lang.getComponent("help-command"));
  }
}
