package club.aurorapvp.modules;

import static club.aurorapvp.AuroraDuels.auroraKitsInstalled;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.api.KitAPI;
import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.datahandlers.QueueData;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Queue implements Listener {
  private final LinkedList<Duel> duelQueue = new LinkedList<>();
  private static boolean arenasSet = true;
  private static Player creator;
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private static Player lookingForMatch = null;
  private static final List<Queue> queues = new ArrayList<>();
  @Getter
  private final String name;
  @Getter
  private Kit kit;
  private final List<Arena> arenas = new ArrayList<>();
  @Getter
  private final QueueData data;

  public Queue(String queueName) {
    this.name = queueName;

    this.data = new QueueData(this);

    if (this.exists()) {
      reload();
    }
  }

  public Queue() {
    this.name = null;
    this.data = null;
  }

  public boolean exists() {
    return data.getFile().exists();
  }

  public Arena[] getArenas() {
    Arena[] arenaArray = new Arena[arenas.size()];

    for (int i = 0; i < arenas.size(); i++) {
      arenaArray[i] = arenas.get(i);
    }

    return arenaArray;
  }

  public Player[] getPlayers() {
    List<Player> playerList = new ArrayList<>();

    for (Duel duel : duelQueue) {
      playerList.add(duel.getP1());
      playerList.add(duel.getP2());
    }

    Player[] players = new Player[playerList.size()];

    for (int i = 0; i < playerList.size(); i++) {
      players[i] = playerList.get(i);
    }

    return players;
  }

  public void reload() {
    this.kit = this.getData().getKit();
    this.arenas.addAll(List.of(this.getData().getArenas()));
  }

  public void create(Player p, String kitName) {
    if (!this.exists() && arenasSet) {
      if (kitName != null && auroraKitsInstalled) {
        this.kit = KitAPI.getKit(kitName);
      }

      create(p);
    } else if (this.exists()) {
      p.sendMessage(Lang.getComponent("queue-already-exists"));
    } else {
      p.sendMessage(Lang.getComponent("queue-being-created"));
    }
  }

  public void create(Player p) {
    if (!this.exists() && arenasSet) {
      arenasSet = false;
      creator = p;

      p.sendMessage(AuroraDuels.COMPONENT_DESERIALIZER.deserialize(
          String.format(Lang.getString("add-arena"), arenas.size())));
    } else if (this.exists()) {
      p.sendMessage(Lang.getComponent("queue-already-exists"));
    } else {
      p.sendMessage(Lang.getComponent("queue-being-created"));
    }
  }

  public void createArenaList(Player p, String str) {
    if (str.equals("stop")) {
      arenasSet = true;
      data.create();
      p.sendMessage(Lang.getComponent("queue-created"));
    } else if (Arena.getArena(str) != null) {
      arenas.add(Arena.getArena(str));
      p.sendMessage(AuroraDuels.COMPONENT_DESERIALIZER.deserialize(
          String.format(Lang.getString("add-arena"), arenas.size())));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  @EventHandler
  public void onPlayerChat(AsyncChatEvent event) {
    if (isCreator(event.getPlayer()) && !arenasSet) {
      event.setCancelled(true);
      createArenaList(event.getPlayer(),
          AuroraDuels.COMPONENT_SERIALIZER.serialize(event.originalMessage()));
    }
  }

  public void create(Arena[] arenas) {
    if (!this.exists()) {
      queues.add(this);
      executor.scheduleAtFixedRate(checkQueue, 0, 10, TimeUnit.SECONDS);
      this.arenas.addAll(Arrays.asList(arenas));
      data.create();
    }
  }

  public void enterDuelQueue(Player p, Player opponent, boolean isPublic) {
    duelQueue.add(new Duel(p, opponent, isPublic));
    p.sendMessage(Lang.getComponent("entered-queue"));
    opponent.sendMessage(Lang.getComponent("entered-queue"));
    executor.execute(checkQueue);
  }

  public void exitDuelQueue(Player p) {
    duelQueue.remove(Duel.getDuel(p));
  }

  private final Runnable checkQueue = () -> {
    if (duelQueue.size() > 0) {
      for (Arena arena : this.getArenas()) {
        if (!arena.isOccupied()) {
          duelQueue.getLast().startDuel(kit, arena);
        }
      }
    }
  };

  public static boolean isCreator(Player p) {
    return p == creator;
  }

  public static Queue[] getQueues() {
    Queue[] queueArray = new Queue[queues.size()];

    for (int i = 0; i < queues.size(); i++) {
      queueArray[i] = queues.get(i);
    }

    return queueArray;
  }

  public static Queue getQueue(String queueName) {
    for (Queue queue : queues) {
      if (Objects.equals(queue.getName(), queueName)) {
        return queue;
      }
    }
    return null;
  }

  public static Queue getQueue(Player p) {
    for (Queue queue : queues) {
      for (Player player : queue.getPlayers()) {
        if (player == p) {
          return queue;
        }
      }
    }
    return null;
  }

  public static void setup() {
    File dir = new File(AuroraDuels.DATA_FOLDER, "/queues/");

    if (Config.get().getBoolean("queues.enable-default-queue")) {
      new Queue("Default").create(Arena.getArenas());
    }

    if (dir.exists()) {
      for (File file : dir.listFiles()) {
        Queue queue = new Queue(file.getName().replace(".yml", ""));
        if (queue.exists()) {
          queues.add(queue);
        }
      }
    }

    AuroraDuels.INSTANCE.getLogger().info("All queues loaded!");
  }

  public static void findMatch(Player p, String queueName) {
    p.sendMessage(Lang.getComponent("looking-match"));
    if (lookingForMatch == null) {
      lookingForMatch = p;
    } else {
      Queue.getQueue(queueName).enterDuelQueue(p, lookingForMatch, true);
      p.sendMessage(Lang.getComponent("match-found"));
      lookingForMatch.sendMessage(Lang.getComponent("match-found"));
      lookingForMatch = null;
    }
  }
}
