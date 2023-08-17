package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.custom.DuelKitSelectEvent;
import club.aurorapvp.auroraduels.events.custom.DuelRequestAcceptEvent;
import club.aurorapvp.auroraduels.events.custom.QueueSelectEvent;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.DuelKitGui;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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

    for (Player p : duel.getPlayers()) {
      if (hasPendingRequest(requester, p)) {
        requester.sendMessage(Lang.getComponent("already-has-request"));
        return;
      } else if (Duel.inDuel(p)) {
        requester.sendMessage(Lang.getComponent("player-in-duel"));
        return;
      } else if (Queue.inQueue(p)) {
        requester.sendMessage(Lang.getComponent("player-in-duel"));
        return;
      }
    }

    AuroraDuels.getQueueGUI().open(requester);

    PENDING_REQUESTS.add(this);

    Bukkit.getPluginManager().registerEvents(this, AuroraDuels.INSTANCE);
  }

  @EventHandler
  public void onQueueSelect(QueueSelectEvent event) {
    if (requester != event.getPlayer()) {
      return;
    }
    
    duel.setQueue(event.getQueue());

    if (AuroraDuels.isAuroraKitsInstalled()) {
      DuelKitGui.getGui(event.getPlayer()).open();
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

    new Timer()
        .schedule(
            new TimerTask() {
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
            },
            Config.get().getInt("requests.expiry-time"));
  }

  public void accept(Player p) {
    p.sendMessage(Lang.getComponent("request-accepted"));
    playersAccepted.put(p, true);

    if (!playersAccepted.containsValue(false)) {
      PENDING_REQUESTS.remove(this);
      requester.sendMessage(Lang.getComponent("request-accepted"));

      duel.getQueue().enterDuelQueue(duel);
    }

    new DuelRequestAcceptEvent(p, duel).callEvent();
  }

  public void deny() {
    PENDING_REQUESTS.remove(this);
    Duel.getDuels().remove(duel);
    requester.sendMessage(Lang.getComponent("request-denied"));

    for (Player p : requested) {
      p.sendMessage(Lang.getComponent("request-denied"));
    }
  }
}
