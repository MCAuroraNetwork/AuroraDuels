package club.aurorapvp.commands;

import club.aurorapvp.modules.duels.Duel;
import club.aurorapvp.modules.duels.PartyDuel;
import club.aurorapvp.modules.duels.TeamDuel;
import club.aurorapvp.modules.requests.PartyRequest;
import club.aurorapvp.modules.requests.StandardRequest;
import club.aurorapvp.modules.requests.TeamRequest;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import java.util.Set;
import org.bukkit.entity.Player;

@CommandAlias("rematch")
public class RematchCommand extends BaseCommand {
  @Default
  public void onRematch(Player p) {
    Duel lastDuel = Duel.getLastDuel(p);

    for (Player duelPlayer : lastDuel.getPlayers()) {
      if (Duel.inDuel(duelPlayer)) {
        return;
      }
    }

    if (lastDuel.getPlayers().size() == 2) {
      for (Player requested : lastDuel.getPlayers()) {
        if (!requested.equals(p)) {
          new StandardRequest(lastDuel.getQueue().getName(), p, requested);
        }
      }
    } else if (lastDuel instanceof TeamDuel teamDuel) {
      new TeamRequest(lastDuel.getQueue().getName(), p, teamDuel.getTeam1(), teamDuel.getTeam2());
    } else if (lastDuel instanceof PartyDuel partyDuel) {
      Set<Player> requested = partyDuel.getPlayers();
      requested.remove(p);

      new PartyRequest(lastDuel.getQueue().getName(), p, requested.toArray(new Player[0]));
    }
  }

  @CatchUnknown
  @Subcommand("help")
  public void onUnknown() {

  }
}
