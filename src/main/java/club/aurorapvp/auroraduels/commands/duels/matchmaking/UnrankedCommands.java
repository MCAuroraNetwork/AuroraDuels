package club.aurorapvp.auroraduels.commands.duels.matchmaking;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.auroraduels.modules.queues.QueueSelectionGUI;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import org.bukkit.entity.Player;

@CommandAlias("unranked")
@CommandPermission("auroraduels.command.unranked")
public class UnrankedCommands extends BaseCommand {

  @Default
  @Description("Joins an unranked duel queue")
  @SuppressWarnings("unused")
  public void onFindMatch(Player player, @Optional String name) {
    if (DuelQueue.inQueue(player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("in-queue"));
      return;
    }

    new QueueSelectionGUI(player, false, "matchmaking").open();
  }
}
