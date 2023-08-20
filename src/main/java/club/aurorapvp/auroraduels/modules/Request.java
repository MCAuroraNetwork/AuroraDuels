package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.custom.DuelKitSelectEvent;
import club.aurorapvp.auroraduels.events.custom.DuelRequestAcceptEvent;
import club.aurorapvp.auroraduels.events.custom.QueueSelectEvent;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.DuelKitGUI;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Request implements Listener {
  protected static final LinkedList<Request> PENDING_REQUESTS = new LinkedList<>();
  private final HashMap<Player, Boolean> playersAccepted = new HashMap<>();
  private final Player requester;
  private final Set<Player> requested = new HashSet<>();
  private final Duel duel;

  public Request(Player requester, Duel duel) {
    this.requester = requester;
    this.duel = duel;

    if (Request.hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("you-have-request"));
      return;
    } else if (Queue.inQueue(requester)) {
      requester.sendMessage(Lang.getComponent("in-queue"));
    }

    for (Player player : duel.getPlayers()) {
      if (hasPendingRequest(requester, player)) {
        requester.sendMessage(Lang.getComponent("already-has-request"));
        return;
      } else if (Duel.inDuel(player)) {
        requester.sendMessage(Lang.getComponent("player-in-duel"));
        return;
      } else if (Queue.inQueue(player)) {
        requester.sendMessage(Lang.getComponent("player-in-duel"));
        return;
      }
    }

    Bukkit.getPluginManager().registerEvents(this, AuroraDuels.INSTANCE);

    if (Queue.getQueues().size() > 1) {
      AuroraDuels.getQueueGUI().open(requester);
    } else if (AuroraDuels.isAuroraKitsInstalled()) {
      DuelKitGUI.getGUI(requester).open();
    } else {
      this.sendRequest();
      return;
    }

    PENDING_REQUESTS.add(this);
  }

  @EventHandler
  public void onQueueSelect(QueueSelectEvent event) {
    if (requester != event.getPlayer()) {
      return;
    }
    
    duel.setQueue(event.getQueue());

    if (AuroraDuels.isAuroraKitsInstalled()) {
      DuelKitGUI.getGUI(event.getPlayer()).open();
      return;
    }

    this.sendRequest();
  }

  @EventHandler
  public void onKitSelect(DuelKitSelectEvent event) {
    if (requester != event.getPlayer()) {
      return;
    }

    duel.setKit(event.getKit());

    this.sendRequest();
  }

  public static Request getRequest(Player player) {
    for (Request request : PENDING_REQUESTS) {
      if (request.requester == player || request.requested.contains(player)) {
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

  public static Set<Request> getRequests(Player player) {
    Set<Request> matchingRequests = new HashSet<>();

    for (Request request : PENDING_REQUESTS) {
      if (request.requester == player || request.requested.contains(player)) {
        matchingRequests.add(request);
      }
    }

    return matchingRequests;
  }

  public static boolean hasPendingRequest(Player requester, Player requested) {
    for (Request request : PENDING_REQUESTS) {
      if (request.requester == requester && request.requested.contains(requested)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasPendingRequest(Player player) {
    for (Request request : PENDING_REQUESTS) {
      if (request.requester == player || request.requested.contains(player)) {
        return true;
      }
    }
    return false;
  }

  public void sendRequest() {
    this.requested.addAll(duel.getPlayers());

    requested.remove(requester);

    if (requested.isEmpty()) {
      requester.sendMessage(Lang.getComponent("unknown-opponent"));
      PENDING_REQUESTS.remove(this);
      return;
    }

    requester.sendMessage(Lang.getComponent("sent-request"));

    for (Player player : requested) {
      player.sendMessage(Lang.formatComponent("received-request", requester.getName()));
    }

    for (Player player : requested) {
      playersAccepted.put(player, false);
    }

    Request thisRequest = this;

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                if (PENDING_REQUESTS.contains(thisRequest)) {
                  requester.sendMessage(Lang.getComponent("request-timed-out"));

                  for (Player player : requested) {
                    player.sendMessage(Lang.getComponent("request-timed-out"));
                  }

                  PENDING_REQUESTS.remove(thisRequest);
                  this.cancel();
                }
              }
            },
            Config.get().getInt("requests.expiry-time"));
  }

  public void accept(Player player) {
    player.sendMessage(Lang.getComponent("request-accepted"));
    playersAccepted.put(player, true);

    if (!playersAccepted.containsValue(false)) {
      PENDING_REQUESTS.remove(this);
      requester.sendMessage(Lang.getComponent("request-accepted"));

      duel.getQueue().enterDuelQueue(duel);
    }

    new DuelRequestAcceptEvent(player, duel).callEvent();
  }

  public void deny() {
    PENDING_REQUESTS.remove(this);
    Duel.getDuels().remove(duel);
    requester.sendMessage(Lang.getComponent("request-denied"));

    for (Player player : requested) {
      player.sendMessage(Lang.getComponent("request-denied"));
    }
  }
}
