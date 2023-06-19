package club.aurorapvp.modules.requests;

import club.aurorapvp.configs.Lang;
import club.aurorapvp.modules.Kit;
import club.aurorapvp.modules.Queue;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.entity.Player;

public abstract class Request {
  protected static final Set<Request> pendingRequests = new HashSet<>();
  protected final Player requester;
  protected final Set<Player> requested;
  protected final Queue queue;
  protected final Kit kit;

  public Request(Player requester, Set<Player> requested, String queueName, String kitName) {
    this.requester = requester;
    this.requested = requested;
    this.queue = Queue.getQueue(queueName);
    this.kit = Kit.getKit(kitName);
  }

  public abstract void accept(Player p);

  public abstract void deny();

  public static Request getRequest(Player p) {
    for (Request request : pendingRequests) {
      if (request.requester == p || request.requested.contains(p)) {
        return request;
      }
    }
    return null;
  }

  public static boolean hasPendingRequest(Player p) {
    for (Request request : pendingRequests) {
      if (request.requester == p || request.requested.contains(p)) {
        return true;
      }
    }
    return false;
  }

  public static void denyDuelRequest(Player p) {
    if (hasPendingRequest(p)) {
      Objects.requireNonNull(getRequest(p)).deny();
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
