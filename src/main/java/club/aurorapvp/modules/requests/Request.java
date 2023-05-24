package club.aurorapvp.modules.requests;

import club.aurorapvp.configs.Lang;
import club.aurorapvp.modules.Queue;
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
  }

  public abstract void accept(Player p);

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
      request.accept(p);
    } else {
      p.sendMessage(Lang.getComponent("no-requests"));
    }
  }
}
