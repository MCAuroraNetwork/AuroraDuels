package club.aurorapvp.modules;

import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.entity.Player;

public class PartyRequest extends Request {
  public PartyRequest(String queueName, Player requester, Player[] requested) {
    super(queueName, requester, requested);

    if (queue == null) {
      requester.sendMessage(Lang.getComponent("queue-not-found"));
      return;
    } else if (requested == null || requested.length == 0) {
      requester.sendMessage(Lang.getComponent("unknown-opponent"));
      return;
    }
    if (hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("already-has-pending-request"));
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

    requester.sendMessage(Lang.getComponent("sent-request"));

    for (Player p : requested) {
      p.sendMessage(Lang.formatComponent("received-request", requester.getName()));
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

  public void accept() {
    pendingRequests.remove(this);
    requester.sendMessage(Lang.getComponent("request-accepted"));

    for (Player p : requested) {
      p.sendMessage(Lang.getComponent("request-accepted"));
    }
  }

  public void deny() {
    pendingRequests.remove(this);
    requester.sendMessage(Lang.getComponent("request-denied"));

    for (Player p : requested) {
      p.sendMessage(Lang.getComponent("request-accepted"));
    }
  }
}
