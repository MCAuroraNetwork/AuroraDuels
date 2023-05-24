package club.aurorapvp.modules.requests;

import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.modules.duels.Duel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;
import org.bukkit.entity.Player;

public class TeamRequest extends Request {
  private final HashMap<Player, Boolean> playersAccepted = new HashMap<>();
  private final Player[] team1;
  private final Player[] team2;

  public TeamRequest(String queueName, Player requester, Player[] team1, Player[] team2) {
    super(queueName, requester, Stream.concat(Arrays.stream(team1), Arrays.stream(team2))
        .toArray(Player[]::new));

    ArrayList<Player> team1List = new ArrayList<>(Arrays.asList(team1));
    ArrayList<Player> team2List = new ArrayList<>(Arrays.asList(team2));

    if (!team1List.contains(requester) && !team2List.contains(requester)) {
      team1List.add(requester);
      team1 = team1List.toArray(new Player[0]);
    }

    this.team1 = team1;
    this.team2 = team2;

    if (queue == null) {
      requester.sendMessage(Lang.getComponent("queue-not-found"));
      return;
    } else if (requested == null || requested.length == 0) {
      requester.sendMessage(Lang.getComponent("unknown-opponent"));
      return;
    } else if (hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("already-has-request"));
      return;
    }

    for (Player p : requested) {
      if (hasPendingRequest(p)) {
        requester.sendMessage(Lang.getComponent("requested-player-has-request"));
        return;
      } else if (Duel.inDuel(p)) {
        requester.sendMessage(Lang.getComponent("player-in-duel"));
        return;
      }
    }

    pendingRequests.add(this);

    requester.sendMessage(Lang.getComponent("sent-request"));

    for (Player p : requested) {
      p.sendMessage(Lang.formatComponent("received-request", requester.getName()));
    }

    for (Player p : requested) {
      playersAccepted.put(p, false);
    }

    Request thisRequest = this;

    new Timer().schedule(new TimerTask() {
                           @Override
                           public void run() {
                             if (pendingRequests.contains(thisRequest)) {
                               requester.sendMessage(Lang.getComponent("request-timed-out"));

                               for (Player p : requested) {
                                 p.sendMessage(Lang.getComponent("request-timed-out"));
                               }

                               pendingRequests.remove(thisRequest);
                               this.cancel();
                             }
                           }
                         }, Config.get().getInt("duels.max-duration")
    );
  }

  public void accept(Player accepter) {
    accepter.sendMessage(Lang.getComponent("request-accepted"));
    playersAccepted.put(accepter, true);

    if (!playersAccepted.containsValue(false)) {
      pendingRequests.remove(this);
      requester.sendMessage(Lang.getComponent("request-accepted"));

      queue.enterDuelQueue(team1, team2, false);
    }
  }

  public void deny() {
    pendingRequests.remove(this);
    requester.sendMessage(Lang.getComponent("request-denied"));

    for (Player p : requested) {
      p.sendMessage(Lang.getComponent("request-denied"));
    }
  }
}
