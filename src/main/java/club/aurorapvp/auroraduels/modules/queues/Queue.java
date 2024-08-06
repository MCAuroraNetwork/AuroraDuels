package club.aurorapvp.auroraduels.modules.queues;

import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.data.QueueDataHandler;
import club.aurorapvp.auroraduels.events.custom.QueueJoinEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.Request;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Queue {

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

  public Queue(String queueName) {
    this.name = queueName;

    this.data = new QueueDataHandler(this);

    if (this.exists()) {
      reload();
    }

    if (AuroraDuels.getInstance().isAuroraCombatInstalled()) {
      new Rating(name + "_duels", Rating.RatingType.CUSTOM, false);
    }
  }

  public static void init() {
    File dir = new File(AuroraDuels.getInstance().getDataFolder(), "/queues/");

    if (AuroraDuels.getInstance().getConfig().getBoolean("queues.enable-random-queue")) {
      Queue queue = new Queue("random");
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

    AuroraDuels.getInstance().getLogger().info("All queues loaded!");
  }

  public static boolean isArenasSet() {
    return arenasSet;
  }

  public static Queue getUnFinishedQueue() {
    return unFinishedQueue;
  }

  public static void reloadAll() {
    queues.clear();

    if (AuroraDuels.getInstance().getConfig().getBoolean("queues.enable-random-queue")) {
      Queue queue = new Queue("random");
      queue.addArenas(Arena.getArenas());

      queue.displayItem = ItemStackUtil.createDisplayItem(Material.DIAMOND_SWORD, queue.getName());

      queues.add(queue);
    }

    for (Queue queue : getQueues()) {
      if (!Objects.equals(queue.getName(), "random")) {
        queue.reload();
      }
    }

    AuroraDuels.getInstance().getLogger().info("ALl queues reloaded!");
  }

  public static boolean isCreator(Player player) {
    return player == currentCreator;
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

  public void findMatch(Player player, boolean ranked) {
    if (Request.hasPendingRequest(player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("you-have-request"));
      return;
    }

    if (lookingForMatch.containsValue(player)) {
      lookingForMatch.clear();
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("not-looking-match"));
      return;
    } else if (Queue.inQueue(player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("in-queue"));
      return;
    }

    player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("looking-match"));
    if (lookingForMatch.get(ranked) == null) {
      lookingForMatch.put(ranked, player);

      for (Request pendingRequest : Request.getRequests(player)) {
        pendingRequest.deny();
      }

      ItemMeta meta = displayItem.getItemMeta();

      meta.lore(List.of(Component.text("In Queue:" + this.inQueue()),
          Component.text("In Duel: " + Duel.inDuel(this.getName()))));

      displayItem.setItemMeta(meta);
    } else {
      WorldSpawn worldSpawn = WorldSpawn.getWorldSpawn(player.getWorld());
      Location loc = null;

      if (worldSpawn != null) {
        loc = worldSpawn.getLocation();
      }

      if (loc == null) {
        loc = WorldSpawn.getGlobalSpawn();
      }

      this.enterDuelQueue(
          new StandardDuel(player, lookingForMatch.get(ranked), this, this.getKit(), loc, ranked,
              1));
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("match-found"));
      lookingForMatch.get(ranked)
          .sendMessage(AuroraDuels.getInstance().getLang().getComponent("match-found"));
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

    if (AuroraDuels.getInstance().isAuroraKitsInstalled()) {
      this.kit = this.data.getKit();
    }
  }

  public void create(Player player, String kitName) {
    if (!this.exists() && arenasSet) {
      if (kitName != null && AuroraDuels.getInstance().isAuroraKitsInstalled()) {
        this.kit = Kit.getKit(kitName);
      }

      create(player);
    } else if (this.exists()) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("queue-already-exists"));
    } else {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("queue-being-created"));
    }
  }

  public void create(Player player) {
    if (player.getInventory().getItemInMainHand().getItemMeta() == null) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("queue-invalid-item"));
      queues.remove(this);
      return;
    }

    if (!this.exists() && arenasSet) {
      arenasSet = false;
      currentCreator = player;
      unFinishedQueue = this;
      displayItem =
          ItemStackUtil.createDisplayItem(
              player.getInventory().getItemInMainHand().getType(), this.getName());

      player.sendMessage(
          MiniMessage.miniMessage()
              .deserialize(String.format(AuroraDuels.getInstance().getLang().getString("add-arena"),
                  arenas.size())));
    } else if (this.exists()) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("queue-already-exists"));
    } else {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("queue-being-created"));
    }
  }

  public void createArenaList(Player player, String str) {
    if (str.equals("stop")) {
      arenasSet = true;
      queues.add(this);
      data.create();
      AuroraDuels.getQueueGUI().addItem(this.getDisplayItem());

      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("queue-created"));
    } else if (Arena.getArena(str) != null) {
      arenas.add(Arena.getArena(str));
      player.sendMessage(
          AuroraDuels.getInstance().getLang().formatComponent("add-arena", arenas.size()));
    } else {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("arena-does-not-exist"));
    }
  }

  public int inQueue() {
    return duelQueue.size() * 2 + lookingForMatch.values().size();
  }

  public void enterDuelQueue(Duel duel) {
    duelQueue.add(duel);

    for (Player player : duel.getPlayers()) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("entered-queue"));
    }

    Queue thisQueue = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        Set<Arena> arenasSet = thisQueue.getArenas();
        List<Arena> arenasList = new ArrayList<>(arenasSet);
        Collections.shuffle(arenasList);
        Iterator<Arena> arenaIterator = arenasList.iterator();

        while (arenaIterator.hasNext() && !duelQueue.isEmpty()) {
          Arena arena = arenaIterator.next();
          if (!arena.isOccupied()) {
            duelQueue.getLast().startDuel(arena);
            duelQueue.remove(duelQueue.getLast());

            ItemMeta meta = displayItem.getItemMeta();

            meta.lore(List.of(Component.text("In Queue:" + thisQueue.inQueue()),
                Component.text("In Duel: " + Duel.inDuel(thisQueue.getName()))));

            displayItem.setItemMeta(meta);
          }
        }
      }
    }.runTaskTimer(AuroraDuels.getInstance(), 0L, 20L);

    ItemMeta meta = displayItem.getItemMeta();

    meta.lore(List.of(Component.text("In Queue:" + this.inQueue()),
        Component.text("In Duel: " + Duel.inDuel(this.getName()))));

    displayItem.setItemMeta(meta);

    new QueueJoinEvent(duel).callEvent();
  }

  public static void removeFromQueue(Player player) {
    for (Queue queue : queues) {
      for (Duel duel : queue.duelQueue) {
        if (duel.contains(player)) {
          queue.duelQueue.remove(duel);

          duel.draw();
        }
      }

      for (Player matchMaker : queue.lookingForMatch.values()) {
        if (matchMaker == player) {
          queue.lookingForMatch.clear();
        }
      }
    }
  }

  public static int inQueues() {
    int inQueues = 0;

    for (Queue queue : Queue.getQueues()) {
      inQueues = inQueues + queue.inQueue();
    }

    return inQueues;
  }

  public static boolean inQueue(Player player) {
    for (Queue queue : Queue.getQueues()) {
      if (queue.lookingForMatch.containsValue(player)) {
        return true;
      }

      for (Player queuedPlayer : queue.getPlayers()) {
        if (player == queuedPlayer) {
          return true;
        }
      }
    }
    return false;
  }
}
