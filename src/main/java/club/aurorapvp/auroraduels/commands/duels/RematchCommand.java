package club.aurorapvp.auroraduels.commands.duels;

import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.FFADuel;
import club.aurorapvp.auroraduels.modules.duels.PartyDuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import java.util.Arrays;
import org.bukkit.entity.Player;

@CommandAlias("rematch")
public class RematchCommand extends BaseCommand {
  @Default
  @SuppressWarnings("unused")
  public void onRematch(Player p) {
    Duel lastDuel = Duel.getLastDuel(p);

    if (lastDuel == null) {
      p.sendMessage(Lang.getComponent("no-last-duel"));
      return;
    }

    for (Player duelPlayer : lastDuel.getPlayers()) {
      if (Duel.inDuel(duelPlayer)) {
        return;
      }
    }

    if (lastDuel.getPlayers().size() == 2) {
      for (Player requested : lastDuel.getPlayers()) {
        if (!requested.equals(p)) {
          new Request(p,
              new StandardDuel(p, requested, lastDuel.getQueue(), lastDuel.getKit(), lastDuel.getRespawnLocation(), false,
                  lastDuel.getRounds()));
        }
      }
    } else if (lastDuel instanceof TeamDuel teamDuel) {
      boolean isInTeam1 = Arrays.asList(teamDuel.getTeam1()).contains(p);
      Player[] team1 = isInTeam1 ?
          Arrays.stream(teamDuel.getTeam1()).filter(player -> !player.equals(p))
              .toArray(Player[]::new) : teamDuel.getTeam1();
      Player[] team2 = isInTeam1 ? teamDuel.getTeam2() :
          Arrays.stream(teamDuel.getTeam2()).filter(player -> !player.equals(p))
              .toArray(Player[]::new);

      new Request(p, new TeamDuel(team1, team2, lastDuel.getQueue(), lastDuel.getKit(), false, lastDuel.getRespawnLocation()));
    } else if (lastDuel instanceof PartyDuel partyDuel) {

      new Request(p,
          new PartyDuel(partyDuel.getPlayers(), lastDuel.getQueue(), lastDuel.getKit(), lastDuel.getRespawnLocation(), false));
    } else if (lastDuel instanceof FFADuel ffaDuel) {
      new Request(p,
          new FFADuel(ffaDuel.getPlayers(), lastDuel.getQueue(), lastDuel.getKit(), lastDuel.getRespawnLocation(), false));
    }
  }
}
