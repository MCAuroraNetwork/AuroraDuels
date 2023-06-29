package club.aurorapvp.auroraduels.modules.queues;

import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.data.QueueDataHandler;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.util.ItemStackUtil;
import club.aurorapvp.aurorakits.modules.kits.Kit;
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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Queue {
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private static final Set<Queue> queues = new HashSet<>();
  private static boolean arenasSet = true;
  private static Player currentCreator;
  private static Map<Boolean, Player> lookingForMatch = new HashMap<>();
  private static Queue unFinishedQueue;
  private final LinkedList<Duel> duelQueue = new LinkedList<>();
  private final String name;
  private final Set<Arena> arenas = new HashSet<>();
  private final QueueDataHandler data;
  private final Runnable checkQueue = () -> {
    if (duelQueue.size() > 0) {
      for (Arena arena : this.arenas) {
        if (!arena.isOccupied()) {
          duelQueue.getLast().startDuel(arena);
        }
      }
    }
  };
  private ItemStack displayItem;
  private Kit kit;
  private boolean kitSet = false;

  public Queue(String queueName) {
    this.name = queueName;

    this.data = new QueueDataHandler(this);

    if (this.exists()) {
      reload();
    }

    if (AuroraDuels.isAuroraCombatInstalled()) {
      Rating.setupRating(name + "_duels");
    }
  }

  public static void init() {
    File dir = new File(AuroraDuels.INSTANCE.getDataFolder(), "/queues/");

    if (Config.get().getBoolean("queues.enable-default-queue")) {
      Queue queue = new Queue("default");
      queue.addArenas(Arena.getArenas());

      queue.displayItem = ItemStackUtil.createDisplayItem(Material.DIAMOND_SWORD, queue.getName());

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

  public static boolean isArenasSet() {
    return arenasSet;
  }

  public static Queue getUnFinishedQueue() {
    return unFinishedQueue;
  }

  public static void reloadAll() {
    queues.clear();

    if (Config.get().getBoolean("queues.enable-default-queue")) {
      Queue queue = new Queue("default");
      queue.addArenas(Arena.getArenas());

      queue.displayItem = ItemStackUtil.createDisplayItem(Material.DIAMOND_SWORD, queue.getName());

      queues.add(queue);
    }

    for (Queue queue : getQueues()) {
      if (!Objects.equals(queue.getName(), "default")) {
        queue.reload();
      }
    }

    AuroraDuels.INSTANCE.getLogger().info("ALl queues reloaded!");
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
      Queue queue = Queue.getQueue(queueName);
      assert queue != null;

      queue.enterDuelQueue(
          new StandardDuel(p, lookingForMatch.get(ranked), queue, queue.getKit(), ranked, 1));
      p.sendMessage(Lang.getComponent("match-found"));
      lookingForMatch.get(ranked).sendMessage(Lang.getComponent("match-found"));
      lookingForMatch = null;
    }
  }

  public Kit getKit() {
    return kit;
  }

  public String getName() {
    return name;
  }

  public ItemStack getDisplayItem() {
    return displayItem;
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
      if (kitName != null && AuroraDuels.isAuroraKitsInstalled()) {
        this.kit = Kit.getKit(kitName);
        kitSet = true;
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
      displayItem = ItemStackUtil.createDisplayItem(p.getInventory().getItemInMainHand().getType(),
          this.getName());

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
      QueueGUI.updateGUI(this.getDisplayItem());

      p.sendMessage(Lang.getComponent("queue-created"));
    } else if (Arena.getArena(str) != null) {
      arenas.add(Arena.getArena(str));
      p.sendMessage(Lang.formatComponent("add-arena", arenas.size()));
    } else {
      p.sendMessage(Lang.getComponent("arena-does-not-exist"));
    }
  }

  public int inQueue() {
    return duelQueue.size() * 2 + lookingForMatch.values().size();
  }

  public void enterDuelQueue(Duel duel) {
    duelQueue.add(duel);
    duel.setActive(true);

    for (Player p : duel.getPlayers()) {
      p.sendMessage(Lang.getComponent("entered-queue"));
    }

    executor.execute(checkQueue);
  }

  public void exitDuelQueue(Player p) {
    duelQueue.remove(Duel.getDuel(p));
  }
}