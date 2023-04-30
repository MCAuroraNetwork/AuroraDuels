package club.aurorapvp.modules;

import club.aurorapvp.configs.Lang;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;

public abstract class Request {
  protected static final Set<Request> pendingRequests = new HashSet<>();
  protected final Player requester;
  protected final Player[] requested;
  protected final Queue queue;

  public Request(String queueName, Player requester, Player[] requested) {
    this.requester = requester;
    this.requested = requested;
    this.queue = Queue.getQueue(queueName);
    pendingRequests.add(this);
  }

  public abstract void accept();

  public abstract void deny();

  public static Request getRequest(Player p) {
    for (Request request : pendingRequests) {
      if (request.requester == p || Arrays.asList(request.requested).contains(p)) {
        return request;
      }
    }
    return null;
  }

  public static boolean hasPendingRequest(Player p) {
    for (Request request : pendingRequests) {
      if (request.requester == p || Arrays.asList(request.requested).contains(p)) {
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

      assert request != null;
      request.accept();

      if (request.requested.length > 1) {
        Player [] players = Arrays.copyOf (request.requested, request.requested.length + 1);
        players [players.length - 1] = request.requester;

        request.queue.enterDuelQueue(players, false);
      } else {
        request.queue.enterDuelQueue(request.requester, request.requested[0], false);
      }
    } else {
      p.sendMessage(Lang.getComponent("no-requests"));
    }
  }
}
