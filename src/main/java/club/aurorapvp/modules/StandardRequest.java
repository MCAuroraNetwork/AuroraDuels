package club.aurorapvp.modules;

import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.entity.Player;

public class StandardRequest extends Request {
  public StandardRequest(String queueName, Player requester, Player requested) {
    super(queueName, requester, new Player[] {requested});

    if (queue == null) {
      requester.sendMessage(Lang.getComponent("queue-not-found"));
      return;
    } else if (requested == null) {
      requester.sendMessage(Lang.getComponent("unknown-opponent"));
      return;
    } else if (hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("already-has-request"));
      return;
    } else if (hasPendingRequest(requested)) {
      requester.sendMessage(Lang.getComponent("requested-player-has-request"));
      return;
    } else if (Duel.inDuel(requested)) {
      requester.sendMessage(Lang.getComponent("player-in-duel"));
      return;
    }

    pendingRequests.add(this);

    requester.sendMessage(Lang.getComponent("sent-request"));
    requested.sendMessage(Lang.formatComponent("received-request", requester.getName()));

    Request thisRequest = this;

    new Timer().schedule(new TimerTask() {
                           @Override
                           public void run() {
                             if (pendingRequests.contains(thisRequest)) {
                               requester.sendMessage(Lang.getComponent("request-timed-out"));
                               requested.sendMessage(Lang.getComponent("request-timed-out"));
                               pendingRequests.remove(thisRequest);
                               this.cancel();
                             }
                           }
                         }, Config.get().getInt("duels.max-duration")
    );
  }

  public void accept(Player p) {
    pendingRequests.remove(this);
    requester.sendMessage(Lang.getComponent("request-accepted"));
    p.sendMessage(Lang.getComponent("request-accepted"));
  }

  public void deny() {
    pendingRequests.remove(this);
    requester.sendMessage(Lang.getComponent("request-denied"));
    requested[0].sendMessage(Lang.getComponent("request-denied"));
  }
}
