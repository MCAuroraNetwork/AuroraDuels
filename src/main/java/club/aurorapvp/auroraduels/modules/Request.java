package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.DuelRequestAcceptEvent;
import club.aurorapvp.auroraduels.events.custom.QueueSelectEvent;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.auroraduels.modules.queues.QueueGUI;
import club.aurorapvp.aurorakits.events.custom.KitSelectEvent;
import club.aurorapvp.aurorakits.modules.kits.KitsGUI;
import java.util.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class Request {

  protected static final LinkedList<Request> PENDING_REQUESTS = new LinkedList<>();
  private final HashMap<UUID, Boolean> playersAccepted = new HashMap<>();
  private final Player requester;
  private final Set<Player> requested = new HashSet<>();
  private final Duel duel;

  public Request(Player requester, Duel duel) {
    this.requester = requester;
    this.duel = duel;

    if (Request.hasPendingRequest(requester)) {
      requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("you-have-request"));
      return;
    } else if (Queue.inQueue(requester)) {
      requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("in-queue"));
      return;
    }

    for (Player player : duel.getPlayers()) {
      if (hasPendingRequest(requester, player)) {
        requester.sendMessage(
            AuroraDuels.getInstance().getLang().getComponent("already-has-request"));
        return;
      } else if (Duel.inDuel(player)) {
        requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("player-in-duel"));
        return;
      } else if (Queue.inQueue(player)) {
        requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("player-in-duel"));
        return;
      }
    }

    if (Queue.getQueues().size() > 1) {
      new QueueGUI("duel-request").open(requester);
    } else if (AuroraDuels.getInstance().isAuroraKitsInstalled()) {
      requester.openInventory(KitsGUI.getGUI(requester).getCurrentPage());
    } else {
      this.sendRequest();
    }

    PENDING_REQUESTS.add(this);
  }

  public void onInventoryClose(InventoryCloseEvent event) {
    if (requester != event.getPlayer()) {
      return;
    }

    if (!(event.getInventory().getHolder() instanceof QueueGUI queueGUI)) {
      return;
    }
    
    if (!Objects.equals(queueGUI.getName(), "duel-request")) {
      return;
    }

    if (duel.getQueue() == null) {
      PENDING_REQUESTS.remove(this);
    }
  }

  public void onQueueSelect(QueueSelectEvent event) {
    if (requester != event.getPlayer()) {
      return;
    }

    if (!event.getName().equals("duel-request")) {
      return;
    }

    duel.setQueue(event.getQueue());

    if (AuroraDuels.getInstance().isAuroraKitsInstalled()) {
      event.getPlayer().openInventory(KitsGUI.getGUI(event.getPlayer()).getCurrentPage());
      return;
    }

    event.getPlayer().closeInventory();
    this.sendRequest();
  }

  public void onKitSelect(KitSelectEvent event) {
    if (requester != event.getPlayer()) {
      return;
    }

    event.setCancelled(true);

    duel.setKit(event.getKit());

    event.getPlayer().closeInventory();
    this.sendRequest();
  }

  public static LinkedList<Request> getPendingRequests() {
    return PENDING_REQUESTS;
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
      requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("unknown-opponent"));
      PENDING_REQUESTS.remove(this);
      return;
    }

    requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("sent-request"));

    for (Player player : requested) {
      player.sendMessage(
          AuroraDuels.getInstance()
              .getLang()
              .getformattedComponentCommand(
                  "received-request",
                  "Click to Accept Request",
                  "/duel accept " + requester.getName(),
                  requester.getName()));
    }

    for (Player player : requested) {
      playersAccepted.put(player.getUniqueId(), false);
    }

    Request thisRequest = this;

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                if (PENDING_REQUESTS.contains(thisRequest)) {
                  requester.sendMessage(
                      AuroraDuels.getInstance().getLang().getComponent("request-timed-out"));

                  for (Player player : requested) {
                    player.sendMessage(
                        AuroraDuels.getInstance().getLang().getComponent("request-timed-out"));
                  }

                  PENDING_REQUESTS.remove(thisRequest);
                  this.cancel();
                }
              }
            },
            AuroraDuels.getInstance().getConfig().getInt("requests.expiry-time"));
  }

  public void accept(Player player) {
    player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("request-accepted"));
    playersAccepted.put(player.getUniqueId(), true);

    if (!playersAccepted.containsValue(false)) {
      PENDING_REQUESTS.remove(this);
      requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("request-accepted"));

      duel.getQueue().enterDuelQueue(duel);
    }

    new DuelRequestAcceptEvent(player, duel).callEvent();
  }

  public void deny() {
    PENDING_REQUESTS.remove(this);
    Duel.getDuels().remove(duel);
    requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("request-denied"));

    for (Player player : requested) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("request-denied"));
    }
  }
}
