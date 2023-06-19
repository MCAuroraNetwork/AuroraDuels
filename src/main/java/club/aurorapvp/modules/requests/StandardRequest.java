package club.aurorapvp.modules.requests;

import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.modules.duels.Duel;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.entity.Player;

public class StandardRequest extends Request {

  private final Player requested;

  public StandardRequest(Player requester, Player requested, String queueName, String kitName) {
    super(requester, Set.of(requested), queueName, kitName);

    this.requested = requested;

    if (queue == null) {
      requester.sendMessage(Lang.getComponent("queue-not-found"));
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

    queue.enterDuelQueue(requester, requested, kit, false);
  }

  public void deny() {
    pendingRequests.remove(this);
    requester.sendMessage(Lang.getComponent("request-denied"));
    requested.sendMessage(Lang.getComponent("request-denied"));
  }
}
