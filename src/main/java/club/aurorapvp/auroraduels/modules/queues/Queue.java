package club.aurorapvp.auroraduels.modules.queues;

import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.data.QueueDataHandler;
import club.aurorapvp.auroraduels.events.custom.QueueJoinEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.util.ItemStackUtil;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import club.aurorapvp.aurorawarps.modules.WorldSpawn;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Queue {
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private static final Set<Queue> queues = new HashSet<>();
  private static boolean arenasSet = true;
  private static Player currentCreator;
  private static Queue unFinishedQueue;
  private final Map<Boolean, Player> lookingForMatch = new HashMap<>();
  private final LinkedList<Duel> duelQueue = new LinkedList<>();
  private final String name;
  private final Set<Arena> arenas = new HashSet<>();
  private final QueueDataHandler data;
  private ItemStack displayItem;
  private Kit kit;
  private final Runnable checkQueue =
      () -> {
        Set<Arena> arenasSet = this.getArenas();
        List<Arena> arenasList = new ArrayList<>(arenasSet);
        Collections.shuffle(arenasList);
        Iterator<Arena> arenaIterator = arenasList.iterator();

        while (arenaIterator.hasNext() && duelQueue.size() > 0) {
          Arena arena = arenaIterator.next();
          if (!arena.isOccupied()) {
            duelQueue.getLast().startDuel(arena);
            duelQueue.remove(duelQueue.getLast());
          }
        }
      };

  public Queue(String queueName) {
    this.name = queueName;

    this.data = new QueueDataHandler(this);

    if (this.exists()) {
      reload();
    }

    if (AuroraDuels.isAuroraCombatInstalled()) {
      new Rating(name + "_duels", Rating.RatingType.CUSTOM, true);
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

  public void findMatch(Player p, boolean ranked) {
    if (lookingForMatch.containsValue(p)) {
      lookingForMatch.clear();
      p.sendMessage(Lang.getComponent("not-looking-match"));
      return;
    }

    p.sendMessage(Lang.getComponent("looking-match"));
    if (lookingForMatch.get(ranked) == null) {
      lookingForMatch.put(ranked, p);
    } else {
      WorldSpawn worldSpawn = WorldSpawn.getWorldSpawn(p.getWorld());
      Location loc = null;

      if (worldSpawn != null) {
        loc = worldSpawn.getSpawn();
      }

      if (loc == null) {
        loc = WorldSpawn.getGlobalSpawn();
      }

      this.enterDuelQueue(
          new StandardDuel(p, lookingForMatch.get(ranked), this, this.getKit(), loc, ranked, 1));
      p.sendMessage(Lang.getComponent("match-found"));
      lookingForMatch.get(ranked).sendMessage(Lang.getComponent("match-found"));
      lookingForMatch.clear();
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
    this.displayItem = data.getDisplayItem();

    if (AuroraDuels.isAuroraKitsInstalled()) {
      this.kit = this.data.getKit();
    }
  }

  public void create(Player p, String kitName) {
    if (!this.exists() && arenasSet) {
      if (kitName != null && AuroraDuels.isAuroraKitsInstalled()) {
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
    if (p.getInventory().getItemInMainHand().getItemMeta() == null) {
      p.sendMessage(Lang.getComponent("queue-invalid-item"));
      queues.remove(this);
      return;
    }

    if (!this.exists() && arenasSet) {
      arenasSet = false;
      currentCreator = p;
      unFinishedQueue = this;
      displayItem =
          ItemStackUtil.createDisplayItem(
              p.getInventory().getItemInMainHand().getType(), this.getName());

      p.sendMessage(
          MiniMessage.miniMessage()
              .deserialize(String.format(Lang.getString("add-arena"), arenas.size())));
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
      QueueGUI.addItem(this.getDisplayItem());

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

    for (Player p : duel.getPlayers()) {
      p.sendMessage(Lang.getComponent("entered-queue"));
    }

    executor.scheduleAtFixedRate(checkQueue, 0L, 1L, TimeUnit.SECONDS);

    new QueueJoinEvent(duel).callEvent();
  }

  public static boolean inQueue(Player p) {
    for (Queue queue : Queue.getQueues()) {
      if (queue.lookingForMatch.containsValue(p)) {
        return true;
      }

      for (Player player : queue.getPlayers()) {
        if (p == player) {
          return true;
        }
      }
    }
    return false;
  }
}
