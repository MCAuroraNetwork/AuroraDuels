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
    Player p1 = null;
    Player p2 = null;

    for (Arena arena : Duels.arenas) {
      if (!arena.isOccupied()) {
        for (Player key : duelQueue.keySet()) {
          p1 = key;
          p2 = duelQueue.get(key);
          Duels.startDuel(p1, p2, arena);
          return;
        }
      }
    }
  };

  public static void enterDuelQueue(Player p, Player opponent) {
    duelQueue.put(p, opponent);
    p.sendMessage(Lang.getComponent("entered-queue"));
    opponent.sendMessage(Lang.getComponent("entered-queue"));
    executor.execute(checkQueue);
  }
}
