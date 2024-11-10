package club.aurorapvp.auroraduels.commands.duels.matchmaking;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.auroraduels.modules.queues.DuelQueueGUI;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.entity.Player;

@CommandAlias("ranked")
@CommandPermission("auroraduels.command.ranked")
public class RankedCommands extends BaseCommand {

  @Default
  @CommandCompletion("@queues")
  @Syntax("<queue>")
  @Description("Joins a provided queue")
  @SuppressWarnings("unused")
  public void onFindMatch(Player player, @Optional String name) {
    if (name == null) {
      new DuelQueueGUI("ranked-match").open(player);
      return;
    }
    
    DuelQueue duelQueue = DuelQueue.getQueue(name);

    if (duelQueue == null) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("queue-not-found"));
      return;
    }

    duelQueue.findMatch(player, true);
  }
}
