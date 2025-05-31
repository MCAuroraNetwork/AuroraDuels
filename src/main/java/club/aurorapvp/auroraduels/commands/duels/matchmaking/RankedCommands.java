package club.aurorapvp.auroraduels.commands.duels.matchmaking;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.auroraduels.modules.queues.QueueSelectionGUI;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import org.bukkit.entity.Player;

@CommandAlias("ranked")
@CommandPermission("auroraduels.command.ranked")
public class RankedCommands extends BaseCommand {

  @Default
  @Description("Joins a ranked duel queue")
  @SuppressWarnings("unused")
  public void onFindMatch(Player player) {
    if (DuelQueue.inQueue(player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("in-queue"));
      return;
    }

    new QueueSelectionGUI(player, true, "matchmaking").open();
  }
}