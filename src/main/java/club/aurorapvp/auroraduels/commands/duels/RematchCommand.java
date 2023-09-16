package club.aurorapvp.auroraduels.commands.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.FFADuel;
import club.aurorapvp.auroraduels.modules.duels.RandomTeamDuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import java.util.Arrays;
import org.bukkit.entity.Player;

@CommandAlias("rematch")
@CommandPermission("auroraduels.command.rematch")
public class RematchCommand extends BaseCommand {

  @Default
  @SuppressWarnings("unused")
  public void onRematch(Player player) {
    Duel lastDuel = Duel.getLastDuel(player);

    if (lastDuel == null) {
      player.sendMessage(AuroraDuels.INSTANCE.getLang().getComponent("no-last-duel"));
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
      boolean isInTeam1 = Arrays.asList(teamDuel.getTeamOneMembers()).contains(player);
      Player[] team1 =
          isInTeam1
              ? Arrays.stream(teamDuel.getTeamOneMembers())
              .filter(teamMember -> !teamMember.equals(player))
              .toArray(Player[]::new)
              : teamDuel.getTeamOneMembers();
      Player[] team2 =
          isInTeam1
              ? teamDuel.getTeamTwoMembers()
              : Arrays.stream(teamDuel.getTeamTwoMembers())
                  .filter(teamMember -> !teamMember.equals(player))
                  .toArray(Player[]::new);

      new Request(
          player,
          new TeamDuel(
              team1,
              team2,
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
