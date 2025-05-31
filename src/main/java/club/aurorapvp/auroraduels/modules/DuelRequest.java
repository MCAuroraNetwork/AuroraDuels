package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.DuelRequestAcceptEvent;
import club.aurorapvp.auroraduels.events.custom.DuelRequestSendEvent;
import club.aurorapvp.auroraduels.events.custom.QueueSelectionEvent;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.teamduels.FriendlyFireGUI;
import club.aurorapvp.auroraduels.modules.duels.teamduels.TeamDuel;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.auroraduels.modules.queues.QueueSelectionGUI;
import club.aurorapvp.aurorakits.events.custom.KitSelectEvent;
import club.aurorapvp.aurorakits.modules.kits.KitsGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.*;

public class DuelRequest implements Listener {
  protected static final LinkedList<DuelRequest> PENDING_DUEL_REQUESTS = new LinkedList<>();
  private final HashMap<UUID, Boolean> playersAccepted = new HashMap<>();
  private final Player requester;
  private final Set<Player> requested = new HashSet<>();
  private final Duel duel;
  private Set<DuelQueue> selectedQueues = new HashSet<>();
  private boolean confirmed = false;
  private boolean requestSent = false;
  private boolean kitSelectionInProgress = false;

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
        requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("already-has-request"));
        return;
      } else if (Duel.inDuel(player)) {
        requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("player-in-duel"));
        return;
      } else if (DuelQueue.inQueue(player)) {
        requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("player-in-duel"));
        return;
      }
    }

    if (!(new DuelRequestSendEvent(requester, this).callEvent())) {
      return;
    }

    if (duel instanceof TeamDuel teamDuel) {
      new FriendlyFireGUI(requester, teamDuel).open();
    } else {
      new QueueSelectionGUI(requester, false, "duel-request").open();
    }

    PENDING_DUEL_REQUESTS.add(this);
    Bukkit.getPluginManager().registerEvents(this, AuroraDuels.getInstance());
  }

  public Duel getDuel() {
    return duel;
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (requester != event.getPlayer()) {
      return;
    }

    if (event.getInventory().getHolder() instanceof FriendlyFireGUI) {
      if (duel instanceof TeamDuel) {
        new QueueSelectionGUI(requester, false, "duel-request").open();
      }
      return;
    }

    if (!(event.getInventory().getHolder() instanceof QueueSelectionGUI queueGUI)) {
      return;
    }

    if (!queueGUI.getContext().equals("duel-request")) {
      return;
    }

    if (!confirmed) {
      PENDING_DUEL_REQUESTS.remove(this);
    }
  }

  @EventHandler
  public void onQueueSelect(QueueSelectionEvent event) {
    if (event.isCancelled() || requester != event.getPlayer() || !event.getContext().equals("duel-request")) {
      return;
    }

    this.selectedQueues = event.getSelectedQueues();
    confirmed = true;

    if (selectedQueues.isEmpty()) {
      requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("select-at-least-one"));
      PENDING_DUEL_REQUESTS.remove(this);
      return;
    }

    if (AuroraDuels.getInstance().isAuroraKitsInstalled() && duel.getKit() == null) {
      KitsGUI kitsGUI = KitsGUI.getGUI(requester);
      if (kitsGUI == null) {
        requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("error-opening-kits"));
        PENDING_DUEL_REQUESTS.remove(this);
        return;
      }
      kitSelectionInProgress = true;
      Bukkit.getScheduler().runTaskLater(AuroraDuels.getInstance(), () -> requester.openInventory(kitsGUI.getChoiceGUI(true, false)), 1L);
    } else {
      sendRequest();
    }
  }

  @EventHandler
  public void onKitSelect(KitSelectEvent event) {
    if (requester != event.getPlayer() || !kitSelectionInProgress) {
      return;
    }

    event.setCancelled(true);
    duel.setKit(event.getKit());
    event.getPlayer().closeInventory();
    kitSelectionInProgress = false;
    sendRequest();
  }

  public static LinkedList<DuelRequest> getPendingRequests() {
    return PENDING_DUEL_REQUESTS;
  }

  public static LinkedList<DuelRequest> getPendingRequests(Player player) {
    LinkedList<DuelRequest> pendingRequests = new LinkedList<>();
    for (DuelRequest duelRequest : PENDING_DUEL_REQUESTS) {
      if (duelRequest.requester.equals(player) || duelRequest.requested.contains(player)) {
        pendingRequests.add(duelRequest);
      }
    }
    return pendingRequests;
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
    if (requestSent) {
      return;
    }

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
                              requester.getName()
                      )
      );
    }

    for (Player player : requested) {
      playersAccepted.put(player.getUniqueId(), false);
    }

    requestSent = true;

    DuelRequest thisDuelRequest = this;
    new Timer().schedule(
            new TimerTask() {
              @Override
              public void run() {
                if (PENDING_DUEL_REQUESTS.contains(thisDuelRequest)) {
                  requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("request-timed-out"));
                  for (Player player : requested) {
                    player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("request-timed-out"));
                  }
                  PENDING_DUEL_REQUESTS.remove(thisDuelRequest);
                  this.cancel();
                }
              }
            },
            AuroraDuels.getInstance().getConfig().getInt("requests.expiry-time")
    );
  }

  public void accept(Player player) {
    player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("request-accepted"));
    playersAccepted.put(player.getUniqueId(), true);

    if (!playersAccepted.containsValue(false)) {
      PENDING_DUEL_REQUESTS.remove(this);
      requester.sendMessage(AuroraDuels.getInstance().getLang().getComponent("request-accepted"));
      for (DuelRequest duelRequest : DuelRequest.getPendingRequests(player)) {
        if (duelRequest != this) {
          duelRequest.deny();
        }
      }
      DuelQueue selectedQueue = selectedQueues.iterator().next();
      duel.setQueue(selectedQueue);
      selectedQueue.enterDuelQueue(duel);
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