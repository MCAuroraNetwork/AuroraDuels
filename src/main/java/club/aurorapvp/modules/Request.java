package club.aurorapvp.modules;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.bukkit.entity.Player;

public class Request {
  private static final Set<Request> pendingRequests = new HashSet<>();
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private final Player requester;
  private final Player requested;
  private final Queue queue;

  public Request(String queueName, Player requester, Player requested) {
    this.requested = requested;
    this.requester = requester;
    this.queue = Queue.getQueue(queueName);

    if (queue == null) {
      requester.sendMessage(Lang.getComponent("queue-not-found"));
      return;
    } else if (requested == null) {
      requester.sendMessage(Lang.getComponent("unknown-opponent"));
      return;
    } else if (hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("already-has-pending-request"));
      return;
    } else if (hasPendingRequest(requested)) {
      requester.sendMessage(Lang.getComponent("requested-player-has-request"));
      return;
    } else if (Duel.inDuel(requested)) {
      requester.sendMessage("");
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

  public void accept() {
    pendingRequests.remove(this);
    requester.sendMessage(Lang.getComponent("request-accepted"));
    requested.sendMessage(Lang.getComponent("request-accepted"));
  }

  public Player getRequester() {
    return requester;
  }

  public Player getRequested() {
    return requested;
  }

  public Queue getQueue() {
    return queue;
  }

  public void deny() {
    pendingRequests.remove(this);
    requester.sendMessage(Lang.getComponent("request-denied"));
    requested.sendMessage(Lang.getComponent("request-denied"));
  }

  public static Request getRequest(Player p) {
    for (Request request : pendingRequests) {
      if (request.getRequester() == p || request.getRequested() == p) {
        return request;
      }
    }
    return null;
  }

  public static boolean hasPendingRequest(Player p) {
    for (Request request : pendingRequests) {
      if (request.getRequester() == p || request.getRequested() == p) {
        return true;
      }
    }
    return false;
  }

  public static void denyDuelRequest(Player p) {
    if (hasPendingRequest(p)) {
      getRequest(p).deny();
    } else {
      p.sendMessage(Lang.getComponent("no-requests"));
    }
  }

  public static void acceptDuelRequest(Player p) {
    if (hasPendingRequest(p)) {
      Request request = getRequest(p);

      request.accept();

      request.getQueue().enterDuelQueue(request.getRequester(), request.getRequested(), false);
    } else {
      p.sendMessage(Lang.getComponent("no-requests"));
    }
  }
}
