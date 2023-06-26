package club.aurorapvp.auroraduels.modules.requests;

import club.aurorapvp.auroraduels.configs.Config;
import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.auroraduels.modules.queues.QueueGUI;
import club.aurorapvp.aurorakits.modules.Kit;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.entity.Player;

public class StandardRequest extends Request {
  private final Player requested;
  private int rounds = 1;

  public StandardRequest(Player requester, Player requested) {
    super(requester, Set.of(requested));

    this.requested = requested;

    if (hasPendingRequest(requester, requested)) {
      requester.sendMessage(Lang.getComponent("already-has-request"));
      return;
    } else if (Duel.inDuel(requested)) {
      requester.sendMessage(Lang.getComponent("player-in-duel"));
      return;
    } else if (Request.hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("you-have-request"));
      return;
    }

    QueueGUI.open(requester);
  }

  public StandardRequest(Player requester, Player requested, int rounds) {
    super(requester, Set.of(requested));

    this.requested = requested;
    this.rounds = rounds;

    if (hasPendingRequest(requester, requested)) {
      requester.sendMessage(Lang.getComponent("already-has-request"));
      return;
    } else if (Duel.inDuel(requested)) {
      requester.sendMessage(Lang.getComponent("player-in-duel"));
      return;
    } else if (Request.hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("you-have-request"));
      return;
    }

    QueueGUI.open(requester);
  }

  public StandardRequest(Player requester, Player requested, Queue queue, Kit kit) {
    super(requester, Set.of(requested));

    this.kit = kit;
    this.queue = queue;

    this.requested = requested;

    if (queue == null) {
      requester.sendMessage(Lang.getComponent("queue-not-found"));
      return;
    } else if (hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("already-has-request"));
      return;
    } else if (hasPendingRequest(requested)) {
      requester.sendMessage(Lang.getComponent("requested-player-has-request"));
      return;
    } else if (Duel.inDuel(requested)) {
      requester.sendMessage(Lang.getComponent("player-in-duel"));
      return;
    }

    requester.sendMessage(Lang.getComponent("sent-request"));
    requested.sendMessage(Lang.formatComponent("received-request", requester.getName()));

    Request thisRequest = this;

    new Timer().schedule(new TimerTask() {
                           @Override
                           public void run() {
                             if (PENDING_REQUESTS.contains(thisRequest)) {
                               requester.sendMessage(Lang.getComponent("request-timed-out"));
                               requested.sendMessage(Lang.getComponent("request-timed-out"));
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
    requested.sendMessage(Lang.formatComponent("received-request", requester.getName()));

    Request thisRequest = this;

    new Timer().schedule(new TimerTask() {
                           @Override
                           public void run() {
                             if (PENDING_REQUESTS.contains(thisRequest)) {
                               requester.sendMessage(Lang.getComponent("request-timed-out"));
                               requested.sendMessage(Lang.getComponent("request-timed-out"));
                               PENDING_REQUESTS.remove(thisRequest);
                               this.cancel();
                             }
                           }
                         }, Config.get().getInt("duels.max-duration")
    );

    acceptable = true;
  }

  public void accept(Player p) {
    PENDING_REQUESTS.remove(this);
    requester.sendMessage(Lang.getComponent("request-accepted"));
    p.sendMessage(Lang.getComponent("request-accepted"));

    if (kit == null) {
      queue.enterDuelQueue(new StandardDuel(requester, requested, queue, queue.getKit(), false, rounds));
    } else {
      queue.enterDuelQueue(new StandardDuel(requester, requested, queue, kit, false, rounds));
    }
  }

  public void deny() {
    PENDING_REQUESTS.remove(this);
    requester.sendMessage(Lang.getComponent("request-denied"));
    requested.sendMessage(Lang.getComponent("request-denied"));
  }
}
