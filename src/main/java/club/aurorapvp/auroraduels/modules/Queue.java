package club.aurorapvp.auroraduels.modules;

import static club.aurorapvp.auroraduels.AuroraDuels.auroraKitsInstalled;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.configs.Config;
import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.datahandlers.QueueData;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.PartyDuel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class Queue {
  private final LinkedList<Duel> duelQueue = new LinkedList<>();
  private static boolean arenasSet = true;
  private static Player currentCreator;
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private static Map<Boolean, Player> lookingForMatch = new HashMap<>();
  private static final Set<Queue> queues = new HashSet<>();
  private static Queue unFinishedQueue;
  private final String name;
  private Kit kit;
  private final Set<Arena> arenas = new HashSet<>();
  private final QueueData data;

  public Queue(String queueName) {
    this.name = queueName;

    this.data = new QueueData(this);

    if (this.exists()) {
      reload();
    }

    if (AuroraDuels.isAuroraCombatInstalled()) {
      Rating.setupRating(name + "_duels", false);
    }
  }

  public static void init() {
    File dir = new File(AuroraDuels.INSTANCE.getDataFolder(), "/queues/");

    if (Config.get().getBoolean("queues.enable-default-queue")) {
      Queue queue = new Queue("default");
      queue.addArenas(Arena.getArenas());
      queues.add(queue);
    }

    if (dir.exists()) {
      for (File file : Objects.requireNonNull(dir.listFiles())) {
        Queue queue = new Queue(file.getName().replace(".yml", ""));
        if (queue.exists()) {
          queues.add(queue);
        }
      }
    }

    AuroraDuels.INSTANCE.getLogger().info("All queues loaded!");
  }

  public Kit getKit() {
    return kit;
  }

  public String getName() {
    return name;
  }

  public boolean exists() {
    return data.getFile().exists();
  }

  public void addArenas(Set<Arena> arenas) {
    this.arenas.addAll(arenas);
  }

  public Set<Arena> getArenas() {
    return arenas;
  }

  public Set<Player> getPlayers() {
    Set<Player> playerSet = new HashSet<>();

    for (Duel duel : duelQueue) {
      playerSet.addAll(duel.getPlayers());
    }

    return playerSet;
  }

  public void reload() {
    this.arenas.addAll(this.data.getArenas());

    if (AuroraDuels.isAuroraKitsInstalled()) {
      this.kit = this.data.getKit();
    }
  }

  public void create(Player p, String kitName) {
    if (!this.exists() && arenasSet) {
      if (kitName != null && auroraKitsInstalled) {
        this.kit = Kit.getKit(kitName);
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
      currentCreator = p;
      unFinishedQueue = this;

      p.sendMessage(MiniMessage.miniMessage().deserialize(
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
      queues.add(this);
      data.create();
      p.sendMessage(Lang.getComponent("queue-created"));
    } else if (Arena.getArena(str) != null) {
      arenas.add(Arena.getArena(str));
      p.sendMessage(MiniMessage.miniMessage().deserialize(
          String.format(Lang.getString("add-arena"), arenas.size())));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  public int inQueue() {
    return duelQueue.size() * 2;
  }

  public void enterDuelQueue(Player p, Player opponent, Kit kit,  boolean ranked) {
    duelQueue.add(new StandardDuel(p, opponent, this, Objects.requireNonNullElse(this.kit, kit), ranked));
    p.sendMessage(Lang.getComponent("entered-queue"));
    opponent.sendMessage(Lang.getComponent("entered-queue"));
    executor.execute(checkQueue);
  }

  public void enterDuelQueue(Set<Player> players, Kit kit, boolean ranked) {
    duelQueue.add(new PartyDuel(players, this, Objects.requireNonNullElse(this.kit, kit), ranked));

    for (Player p : players) {
      p.sendMessage(Lang.getComponent("entered-queue"));
    }

    executor.execute(checkQueue);
  }

  public void enterDuelQueue(Player[] team1, Player[] team2, Kit kit, boolean ranked) {
    duelQueue.add(new TeamDuel(team1, team2, this, Objects.requireNonNullElse(this.kit, kit), ranked));

    for (Player p : team1) {
      p.sendMessage(Lang.getComponent("entered-queue"));
    }

    for (Player p : team2) {
      p.sendMessage(Lang.getComponent("entered-queue"));
    }

    executor.execute(checkQueue);
  }

  public void exitDuelQueue(Player p) {
    duelQueue.remove(Duel.getDuel(p));
  }

  private final Runnable checkQueue = () -> {
    if (duelQueue.size() > 0) {
      for (Arena arena : this.arenas) {
        if (!arena.isOccupied()) {
          duelQueue.getLast().startDuel(arena);
        }
      }
    }
  };

  public static boolean isArenasSet() {
    return arenasSet;
  }

  public static Queue isUnFinishedQueue() {
    return unFinishedQueue;
  }

  public static void reloadAll() {
    for (Queue queue : getQueues()) {
      queue.reload();
    }

    AuroraDuels.INSTANCE.getLogger().info("ALl arenas reloaded!");
  }

  public static boolean isCreator(Player p) {
    return p == currentCreator;
  }

  public static Set<Queue> getQueues() {
    return queues;
  }

  public static Queue getQueue(String queueName) {
    for (Queue queue : queues) {
      if (Objects.equals(queue.name, queueName)) {
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

  public static void findMatch(Player p, String queueName, boolean ranked) {
    p.sendMessage(Lang.getComponent("looking-match"));
    if (lookingForMatch.get(ranked) == null) {
      lookingForMatch.put(true, p);
    } else {
      // For some reason Intellij wouldn't stop whining about a possible NPE
      // unless I wrote it like this, fuck you Intellij
      Queue queue = Queue.getQueue(queueName);
      assert queue != null;
      queue.enterDuelQueue(Set.of(p, lookingForMatch.get(ranked)), null, ranked);
      p.sendMessage(Lang.getComponent("match-found"));
      lookingForMatch.get(ranked).sendMessage(Lang.getComponent("match-found"));
      lookingForMatch = null;
    }
  }
}