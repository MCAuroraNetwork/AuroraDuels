package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.DuelKitGui;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.auroraduels.modules.queues.QueueGUI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class Request {
  protected static final LinkedList<Request>
      PENDING_REQUESTS = new LinkedList<>();
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
    }

    for (Player p : duel.getPlayers()) {
      if (hasPendingRequest(requester, p)) {
        requester.sendMessage(Lang.getComponent("already-has-request"));
        PENDING_REQUESTS.remove(this);
        return;
      } else if (Duel.inDuel(p)) {
        requester.sendMessage(Lang.getComponent("player-in-duel"));
        PENDING_REQUESTS.remove(this);
        return;
      }
    }

    QueueGUI.open(requester);

    PENDING_REQUESTS.add(this);
  }

  public void sendRequest() {
    this.requested.addAll(duel.getPlayers());

    requested.remove(requester);

    if (requested.size() == 0) {
      requester.sendMessage(Lang.getComponent("unknown-opponent"));
      PENDING_REQUESTS.remove(this);
      return;
    }

    requester.sendMessage(Lang.getComponent("sent-request"));

    for (Player p : requested) {
      p.sendMessage(Lang.formatComponent("received-request", requester.getName()));
    }

    for (Player p : requested) {
      playersAccepted.put(p, false);
    }

    Request thisRequest = this;

    new Timer().schedule(new TimerTask() {
                           @Override
                           public void run() {
                             if (PENDING_REQUESTS.contains(thisRequest)) {
                               requester.sendMessage(Lang.getComponent("request-timed-out"));

                               for (Player p : requested) {
                                 p.sendMessage(Lang.getComponent("request-timed-out"));
                               }

                               PENDING_REQUESTS.remove(thisRequest);
                               this.cancel();
                             }
                           }
                         }, Config.get().getInt("requests.expiry-time")
    );
  }

  public void accept(Player p) {
    p.sendMessage(Lang.getComponent("request-accepted"));
    playersAccepted.put(p, true);

    if (!playersAccepted.containsValue(false)) {
      PENDING_REQUESTS.remove(this);
      requester.sendMessage(Lang.getComponent("request-accepted"));

      duel.getQueue().enterDuelQueue(duel);
    }
  }

  public void deny() {
    PENDING_REQUESTS.remove(this);
    requester.sendMessage(Lang.getComponent("request-denied"));

    for (Player p : requested) {
      p.sendMessage(Lang.getComponent("request-denied"));
    }
  }

  public static void onInventoryClicked(InventoryClickEvent event) {
    if (!QueueGUI.isQueueGUI(event.getClickedInventory())) {
      return;
    }

    final ItemStack clickedItem = event.getCurrentItem();

    if (clickedItem == null || clickedItem.getType().isAir()) {
      return;
    }

    Player p = (Player) event.getWhoClicked();

    event.setCancelled(true);

    if (event.getCurrentItem().getItemMeta().displayName() == null) {
      p.sendMessage(Lang.getComponent("queue-not-found"));
    } else {
      String itemName =
          PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName());
      String queueName = itemName.substring(1, itemName.length() - 1);

      Queue queue = Queue.getQueue(queueName.toLowerCase());

      if (queue == null) {
        p.sendMessage(Lang.getComponent("queue-not-found"));
        return;
      }

      Objects.requireNonNull(Duel.getDuel(p)).setQueue(queue);

      if (AuroraDuels.isAuroraKitsInstalled()) {
        new DuelKitGui(p);
        return;
      }

      Objects.requireNonNull(Request.getRequest(p)).sendRequest();
    }
  }

  public static void onInventoryClose(InventoryCloseEvent event) {
    if (!Request.hasPendingRequest((Player) event.getPlayer())) {
      return;
    }

    if (Objects.requireNonNull(DuelKitGui.getGui((Player) event.getPlayer()))
        .isGuiInventory(event.getInventory())) {
      Objects.requireNonNull(Request.getRequest((Player) event.getPlayer())).sendRequest();
    }
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
      if (request.requester == requester && request.requested.contains(requested)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasPendingRequest(Player p) {
    for (Request request : PENDING_REQUESTS) {
      if (request.requester == p || request.requested.contains(p)) {
        return true;
      }
    }
    return false;
  }
}
