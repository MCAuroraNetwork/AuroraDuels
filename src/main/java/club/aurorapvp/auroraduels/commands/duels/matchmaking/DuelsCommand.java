package club.aurorapvp.auroraduels.commands.duels.matchmaking;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.auroraduels.modules.queues.RankedUnrankedGUI;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;

@CommandAlias("duels")
@CommandPermission("auroraduels.command.duels")
public class DuelsCommand extends BaseCommand {

  @Default
  @CommandCompletion("@queues")
  @Syntax("<queue>")
  @Description("Opens duels GUI")
  @SuppressWarnings("unused")
  public void onFindMatch(Player player) {
    if (DuelQueue.inQueue(player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("in-queue"));
      return;
    }

    new RankedUnrankedGUI(player).open();
  }
}
