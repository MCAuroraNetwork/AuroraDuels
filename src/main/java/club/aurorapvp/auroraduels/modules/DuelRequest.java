package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.DuelRequestAcceptEvent;
import club.aurorapvp.auroraduels.events.custom.DuelRequestSendEvent;
import club.aurorapvp.auroraduels.events.custom.QueueSelectEvent;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.auroraduels.modules.queues.DuelQueueGUI;
import club.aurorapvp.aurorakits.events.custom.KitSelectEvent;
import club.aurorapvp.aurorakits.modules.kits.KitsGUI;
import java.util.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class DuelRequest {

  protected static final LinkedList<DuelRequest> PENDING_DUEL_REQUESTS = new LinkedList<>();
  private final HashMap<UUID, Boolean> playersAccepted = new HashMap<>();
  private final Player requester;
  private final Set<Player> requested = new HashSet<>();
  private final Duel duel;

  public DuelRequest(Player requester, Duel duel) {
    this.requester = requester;
    this.duel = duel;

    if (DuelRequest.hasPendingRequest(requester)) {
      requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("you-have-request"));
      return;
    } else if (DuelQueue.inQueue(requester)) {
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
      } else if (DuelQueue.inQueue(player)) {
        requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("player-in-duel"));
        return;
      }
    }

    if(!(new DuelRequestSendEvent(requester, this).callEvent())) {
      return;
    }

    if (DuelQueue.getQueues().size() > 1 && duel.getQueue() == null) {
      new DuelQueueGUI("duel-request").open(requester);
    } else if (AuroraDuels.getInstance().isAuroraKitsInstalled() && duel.getKit() == null) {
      requester.openInventory(KitsGUI.getGUI(requester).getCurrentPage());
    } else {
      this.sendRequest();
    }

    PENDING_DUEL_REQUESTS.add(this);
  }

  public void onInventoryClose(InventoryCloseEvent event) {
    if (requester != event.getPlayer()) {
      return;
    }

    if (!(event.getInventory().getHolder() instanceof DuelQueueGUI duelQueueGUI)) {
      return;
    }

    if (!Objects.equals(duelQueueGUI.getName(), "duel-request")) {
      return;
    }

    if (duel.getQueue() == null) {
      PENDING_DUEL_REQUESTS.remove(this);
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

  public static LinkedList<DuelRequest> getPendingRequests() {
    return PENDING_DUEL_REQUESTS;
  }

  public static DuelRequest getRequest(Player player) {
    for (DuelRequest duelRequest : PENDING_DUEL_REQUESTS) {
      if (duelRequest.requester.equals(player) || duelRequest.requested.contains(player)) {
        return duelRequest;
      }
    }
    return null;
  }

  public static DuelRequest getRequest(Player requester, Player requested) {
    for (DuelRequest duelRequest : PENDING_DUEL_REQUESTS) {
      if (duelRequest.requester.equals(requester) && duelRequest.requested.contains(requested)) {
        return duelRequest;
      }
    }
    return null;
  }

  public static Set<DuelRequest> getRequests(Player player) {
    Set<DuelRequest> matchingDuelRequests = new HashSet<>();

    for (DuelRequest duelRequest : PENDING_DUEL_REQUESTS) {
      if (duelRequest.requester.equals(player) || duelRequest.requested.contains(player)) {
        matchingDuelRequests.add(duelRequest);
      }
    }

    return matchingDuelRequests;
  }

  public static boolean hasPendingRequest(Player requester, Player requested) {
    for (DuelRequest duelRequest : PENDING_DUEL_REQUESTS) {
      if (duelRequest.requester.equals(requester) && duelRequest.requested.contains(requested)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasPendingRequest(Player player) {
    for (DuelRequest duelRequest : PENDING_DUEL_REQUESTS) {
      if (duelRequest.requester.equals(player) || duelRequest.requested.contains(player)) {
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
      PENDING_DUEL_REQUESTS.remove(this);
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

    DuelRequest thisDuelRequest = this;

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                if (PENDING_DUEL_REQUESTS.contains(thisDuelRequest)) {
                  requester.sendMessage(
                      AuroraDuels.getInstance().getLang().getComponent("request-timed-out"));

                  for (Player player : requested) {
                    player.sendMessage(
                        AuroraDuels.getInstance().getLang().getComponent("request-timed-out"));
                  }

                  PENDING_DUEL_REQUESTS.remove(thisDuelRequest);
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
      PENDING_DUEL_REQUESTS.remove(this);
      requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("request-accepted"));

      duel.getQueue().enterDuelQueue(duel);
    }

    new DuelRequestAcceptEvent(player, duel).callEvent();
  }

  public void deny() {
    PENDING_DUEL_REQUESTS.remove(this);
    Duel.getDuels().remove(duel);
    requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("request-denied"));

    for (Player player : requested) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("request-denied"));
    }
  }
}
