package club.aurorapvp.auroraduels.modules.requests;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.duels.DuelKitGui;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.aurorakits.modules.Kit;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import org.bukkit.entity.Player;

public abstract class Request {
  protected static final LinkedList<Request> PENDING_REQUESTS = new LinkedList<>();
  protected final Player requester;
  protected final Set<Player> requested;
  protected Queue queue;
  protected Kit kit;
  protected boolean acceptable;

  public Request(Player requester, Set<Player> requested) {
    this.requester = requester;
    this.requested = requested;

    acceptable = false;

    PENDING_REQUESTS.add(this);
  }

  public boolean isAcceptable() {
    return acceptable;
  }

  public abstract void accept(Player p);

  public abstract void deny();

  public abstract void sendRequest();

  public void setQueue(Queue queue) {
    this.queue = queue;

    if (queue.isKitSet()) {
      this.sendRequest();
    }

    if (!AuroraDuels.isAuroraKitsInstalled()) {
      this.setKit(null);

      this.sendRequest();
    }

    new DuelKitGui(requester);
  }

  public void setKit(Kit kit) {
    this.kit = kit;

    this.sendRequest();
  }

  public static Request getRequest(Player p) {
    for (Request request : PENDING_REQUESTS) {
      if (request.requester == p || request.requested.contains(p)) {
        return request;
      }
    }
    return null;
  }

  public static Request getRequest(Player requester, Player requested) {
    for (Request request : PENDING_REQUESTS) {
      if (request.requester == requester && request.requested.contains(requested)) {
        return request;
      }
    }
    return null;
  }

  public static Set<Request> getRequests(Player p) {
    Set<Request> matchingRequests = new HashSet<>();

    for (Request request : PENDING_REQUESTS) {
      if (request.requester == p || request.requested.contains(p)) {
        matchingRequests.add(request);
      }
    }

    return matchingRequests;
  }

  public static boolean hasPendingRequest(Player requester, Player requested) {
    for (Request request : PENDING_REQUESTS) {
      if (request.isAcceptable() && request.requester == requester &&
          request.requested.contains(requested)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasPendingRequest(Player p) {
    for (Request request : PENDING_REQUESTS) {
      if (request.isAcceptable() && (request.requester == p || request.requested.contains(p))) {
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

  public static void acceptDuelRequest(Player p, Request request) {
    for (Request pendingRequest : Request.getRequests(p)) {
      if (pendingRequest != request) {
        pendingRequest.deny();
      }
    }

    request.accept(p);
  }

  public static void acceptDuelRequest(Player p) {
    if (hasPendingRequest(p)) {
      Request request = getRequest(p);
      assert request != null;

      for (Request pendingRequest : Request.getRequests(p)) {
        if (pendingRequest != request) {
          pendingRequest.deny();
        }
      }

      request.accept(p);
    } else {
      p.sendMessage(Lang.getComponent("no-requests"));
    }
  }
}
