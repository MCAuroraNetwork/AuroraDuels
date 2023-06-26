package club.aurorapvp.auroraduels.modules.requests;

import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.auroraduels.modules.queues.QueueGUI;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.entity.Player;

public class TeamRequest extends Request {
  private final HashMap<Player, Boolean> playersAccepted = new HashMap<>();
  private final Player[] team1;
  private final Player[] team2;

  public TeamRequest(Player requester, Player[] team1, Player[] team2) {
    super(requester,
        Stream.concat(Arrays.stream(team1), Arrays.stream(team2)).collect(Collectors.toSet()));

    ArrayList<Player> team1List = new ArrayList<>(Arrays.asList(team1));
    ArrayList<Player> team2List = new ArrayList<>(Arrays.asList(team2));

    if (!team1List.contains(requester) && !team2List.contains(requester)) {
      team1List.add(requester);
      team1 = team1List.toArray(new Player[0]);
    }

    this.team1 = team1;
    this.team2 = team2;

    if (requested == null || requested.size() == 0) {
      requester.sendMessage(Lang.getComponent("unknown-opponent"));
      return;
    } else if (hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("you-have-request"));
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

    QueueGUI.open(requester);
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

  public TeamRequest(Player requester, Player[] team1, Player[] team2, Queue queue, Kit kit) {
    super(requester,
        Stream.concat(Arrays.stream(team1), Arrays.stream(team2)).collect(Collectors.toSet()));

    this.kit = kit;
    this.queue = queue;

    ArrayList<Player> team1List = new ArrayList<>(Arrays.asList(team1));
    ArrayList<Player> team2List = new ArrayList<>(Arrays.asList(team2));

    if (!team1List.contains(requester) && !team2List.contains(requester)) {
      team1List.add(requester);
      team1 = team1List.toArray(new Player[0]);
    }

    this.team1 = team1;
    this.team2 = team2;

    if (requested == null || requested.size() == 0) {
      requester.sendMessage(Lang.getComponent("unknown-opponent"));
      return;
    } else if (hasPendingRequest(requester)) {
      requester.sendMessage(Lang.getComponent("you-have-request"));
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

  public void accept(Player acceptor) {
    acceptor.sendMessage(Lang.getComponent("request-accepted"));
    playersAccepted.put(acceptor, true);

    if (!playersAccepted.containsValue(false)) {
      PENDING_REQUESTS.remove(this);
      requester.sendMessage(Lang.getComponent("request-accepted"));

      if (kit == null) {
        queue.enterDuelQueue(new TeamDuel(team1, team2, queue, queue.getKit(), false));
      } else {
        queue.enterDuelQueue(new TeamDuel(team1, team2, queue, kit, false));
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
