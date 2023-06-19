package club.aurorapvp.commands;

import club.aurorapvp.configs.Lang;
import club.aurorapvp.modules.duels.Duel;
import club.aurorapvp.modules.duels.PartyDuel;
import club.aurorapvp.modules.duels.TeamDuel;
import club.aurorapvp.modules.requests.PartyRequest;
import club.aurorapvp.modules.requests.StandardRequest;
import club.aurorapvp.modules.requests.TeamRequest;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import java.util.Arrays;
import java.util.Set;
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
          new StandardRequest(p, requested, lastDuel.getQueue().getName(), lastDuel.getKit().getName());
        }
      }
    } else if (lastDuel instanceof TeamDuel teamDuel) {
      boolean isInTeam1 = Arrays.asList(teamDuel.getTeam1()).contains(p);
      Player[] team1 = isInTeam1 ? Arrays.stream(teamDuel.getTeam1()).filter(player -> !player.equals(p)).toArray(Player[]::new) : teamDuel.getTeam1();
      Player[] team2 = isInTeam1 ? teamDuel.getTeam2() : Arrays.stream(teamDuel.getTeam2()).filter(player -> !player.equals(p)).toArray(Player[]::new);

      new TeamRequest(p, team1, team2, lastDuel.getQueue().getName(), lastDuel.getKit().getName());
    } else if (lastDuel instanceof PartyDuel partyDuel) {
      Set<Player> requested = partyDuel.getPlayers();
      requested.remove(p);

      new PartyRequest(p, requested, lastDuel.getQueue().getName(), lastDuel.getKit().getName());
    }
  }
}
