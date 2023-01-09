package club.aurorapvp.modules;

import club.aurorapvp.configs.Lang;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;

public class Queue {
  public static LinkedHashMap<Player, Player> duelQueue = new LinkedHashMap<>();
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  public static void setup() {
    executor.scheduleAtFixedRate(checkQueue, 0, 10, TimeUnit.SECONDS);
  }

  private static final Runnable checkQueue = () -> {
    for (Arena arena : Duels.arenas) {
      if (!arena.isOccupied()) {
        for (Player p1 : duelQueue.keySet()) {
          Player p2 = duelQueue.get(p1);
          if (!Duels.inDuel(p1) && Duels.inDuel(p2)) {
            Duels.startDuel(p1, p2, arena);
          }
          return;
        }
      }
    }
  };

  public static void exitDuelQueue(Player p1, Player p2) {
    if (duelQueue.containsKey(p1)) {
      duelQueue.remove(p1);
    } else if (duelQueue.containsKey(p2)) {
      duelQueue.remove(p2);
    }
  }

  public static void enterDuelQueue(Player p, Player opponent) {
    duelQueue.put(p, opponent);
    p.sendMessage(Lang.getComponent("entered-queue"));
    opponent.sendMessage(Lang.getComponent("entered-queue"));
    executor.execute(checkQueue);
  }
}
