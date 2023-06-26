package club.aurorapvp.auroraduels.modules.requests;

import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.FFADuel;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.auroraduels.modules.queues.QueueGUI;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.entity.Player;

public class FFARequest extends Request {
  private final HashMap<Player, Boolean> playersAccepted = new HashMap<>();

  public FFARequest(Player requester, Set<Player> requested) {
    super(requester, requested);

    if (requested == null || requested.size() == 0) {
      requester.sendMessage(Lang.getComponent("unknown-opponent"));
      return;
    } else if (Request.hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("you-have-request"));
      return;
    }

    for (Player p : requested) {
      if (hasPendingRequest(requester, p)) {
        requester.sendMessage(Lang.getComponent("already-has-request"));
        return;
      } else if (Duel.inDuel(p)) {
        requester.sendMessage(Lang.getComponent("player-in-duel"));
        return;
      }
    }

    QueueGUI.open(requester);
  }

  public FFARequest(Player requester, Set<Player> requested, Queue queue, Kit kit) {
    super(requester, requested);

    this.queue = queue;
    this.kit = kit;

    if (queue == null) {
      requester.sendMessage(Lang.getComponent("queue-not-found"));
      return;
    } else if (requested == null || requested.size() == 0) {
      requester.sendMessage(Lang.getComponent("unknown-opponent"));
      return;
    } else if (hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("already-has-request"));
      return;
    }

    for (Player p : requested) {
      if (hasPendingRequest(p)) {
        requester.sendMessage(Lang.getComponent("requested-player-has-request"));
        return;
      } else if (Duel.inDuel(p)) {
        requester.sendMessage(Lang.getComponent("player-in-duel"));
        return;
      }
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
                         }, Config.get().getInt("duels.max-duration")
    );

    acceptable = true;
  }

  public void sendRequest() {
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
                         }, Config.get().getInt("duels.max-duration")
    );

    acceptable = true;
  }

  public void accept(Player acceptor) {
    acceptor.sendMessage(Lang.getComponent("request-accepted"));
    playersAccepted.put(acceptor, true);

    if (!playersAccepted.containsValue(false)) {
      PENDING_REQUESTS.remove(this);
      requester.sendMessage(Lang.getComponent("request-accepted"));

      Set<Player> players = new HashSet<>(requested);
      players.add(requester);

      if (kit == null) {
        queue.enterDuelQueue(new FFADuel(players, queue, queue.getKit(), false));
      } else {
        queue.enterDuelQueue(new FFADuel(players, queue, kit, false));
      }
    }
  }

  public void deny() {
    PENDING_REQUESTS.remove(this);
    requester.sendMessage(Lang.getComponent("request-denied"));

    for (Player p : requested) {
      p.sendMessage(Lang.getComponent("request-denied"));
    }
  }
}