package club.aurorapvp.auroraduels.commands.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.DuelRequest;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.FFADuel;
import club.aurorapvp.auroraduels.modules.duels.RandomTeamDuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.teamduels.TeamDuel;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player;

import java.util.HashSet;

@CommandAlias("rematch")
@CommandPermission("auroraduels.command.rematch")
public class RematchCommand extends BaseCommand {

  @Default
  @CatchUnknown
  @SuppressWarnings("unused")
  public void onRematch(Player player) {
    Duel lastDuel = Duel.getLastDuel(player);
    if (lastDuel == null) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("no-last-duel"));
      return;
    }

    for (Player duelPlayer : lastDuel.getPlayers()) {
      if (Duel.inDuel(duelPlayer)) {
        return;
      }
    }

    if (lastDuel.getPlayers().size() == 2) {
      for (Player requested : lastDuel.getPlayers()) {
        if (!requested.equals(player)) {
          DuelRequest duelRequest = new DuelRequest(
                  player,
                  new StandardDuel(
                          player,
                          requested,
                          lastDuel.getQueue(),
                          lastDuel.getKit(),
                          lastDuel.getRespawnLocation(),
                          false,
                          lastDuel.getRounds()
                  )
          );
          DuelQueue.getPlayerSelectedQueues().put(
                  player,
                  DuelQueue.getPlayerSelectedQueues().getOrDefault(player, new HashSet<>())
          );
        }
      }
    } else if (lastDuel instanceof RandomTeamDuel) {
      DuelRequest duelRequest = new DuelRequest(
              player,
              new RandomTeamDuel(lastDuel.getPlayers(), lastDuel.getRespawnLocation(), false)
      );
      DuelQueue.getPlayerSelectedQueues().put(
              player,
              DuelQueue.getPlayerSelectedQueues().getOrDefault(player, new HashSet<>())
      );
    } else if (lastDuel instanceof TeamDuel teamDuel) {
      DuelRequest duelRequest = new DuelRequest(
              player,
              new TeamDuel(
                      teamDuel.getTeamOneMembers(),
                      teamDuel.getTeamTwoMembers(),
                      lastDuel.getQueue(),
                      lastDuel.getKit(),
                      lastDuel.getRespawnLocation(),
                      false
              )
      );
      DuelQueue.getPlayerSelectedQueues().put(
              player,
              DuelQueue.getPlayerSelectedQueues().getOrDefault(player, new HashSet<>())
      );
    } else if (lastDuel instanceof FFADuel ffaDuel) {
      DuelRequest duelRequest = new DuelRequest(
              player,
              new FFADuel(
                      ffaDuel.getPlayers(),
                      lastDuel.getQueue(),
                      lastDuel.getKit(),
                      lastDuel.getRespawnLocation(),
                      false
              )
      );
      DuelQueue.getPlayerSelectedQueues().put(
              player,
              DuelQueue.getPlayerSelectedQueues().getOrDefault(player, new HashSet<>())
      );
    }
  }
}