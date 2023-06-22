package club.aurorapvp.auroraduels.modules.requests;

import club.aurorapvp.auroraduels.configs.Config;
import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.entity.Player;

public class PartyRequest extends Request {

  private final HashMap<Player, Boolean> playersAccepted = new HashMap<>();

  public PartyRequest(Player requester, Set<Player> requested, String queueName, String kitName) {
    super(requester, requested, queueName, kitName);

    if (queue == null) {
      requester.sendMessage(Lang.getComponent("queue-not-found"));
      return;
    } else if (requested == null || requested.size() == 0) {
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

  public void accept(Player acceptor) {
    acceptor.sendMessage(Lang.getComponent("request-accepted"));
    playersAccepted.put(acceptor, true);

    if (!playersAccepted.containsValue(false)) {
      pendingRequests.remove(this);
      requester.sendMessage(Lang.getComponent("request-accepted"));

      Set<Player> players = new HashSet<>(requested);
      players.add(requester);

      queue.enterDuelQueue(players, kit, false);
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
