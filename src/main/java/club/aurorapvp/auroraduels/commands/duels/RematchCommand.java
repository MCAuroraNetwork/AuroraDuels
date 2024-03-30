package club.aurorapvp.auroraduels.commands.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.FFADuel;
import club.aurorapvp.auroraduels.modules.duels.RandomTeamDuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import java.util.Arrays;
import org.bukkit.entity.Player;

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
          new Request(
              player,
              new StandardDuel(
                  player,
                  requested,
                  lastDuel.getQueue(),
                  lastDuel.getKit(),
                  lastDuel.getRespawnLocation(),
                  false,
                  lastDuel.getRounds()));
        }
      }
    } else if (lastDuel instanceof RandomTeamDuel) {
      new Request(
          player, new RandomTeamDuel(lastDuel.getPlayers(), lastDuel.getRespawnLocation(), false));
    } else if (lastDuel instanceof TeamDuel teamDuel) {
      new Request(
          player,
          new TeamDuel(
              teamDuel.getTeamOneMembers(),
              teamDuel.getTeamTwoMembers(),
              lastDuel.getQueue(),
              lastDuel.getKit(),
              lastDuel.getRespawnLocation(),
              false));
    } else if (lastDuel instanceof FFADuel ffaDuel) {
      new Request(
          player,
          new FFADuel(
              ffaDuel.getPlayers(),
              lastDuel.getQueue(),
              lastDuel.getKit(),
              lastDuel.getRespawnLocation(),
              false));
    }
  }
}
