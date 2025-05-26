package club.aurorapvp.auroraduels.modules.queues;

import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.data.QueueDataHandler;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.QueueJoinEvent;
import club.aurorapvp.auroraduels.events.custom.QueueSelectEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.DuelRequest;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.util.ItemStackUtil;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import club.aurorapvp.aurorawarps.modules.WorldSpawn;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DuelQueue {

  private static final Set<DuelQueue> DUEL_QUEUES = new HashSet<>();
  private static boolean arenasSet = true;
  private static Player currentCreator;
  private static DuelQueue unFinishedDuelQueue;
  private final Map<Boolean, Player> lookingForMatch = new HashMap<>();
  private final ObservableQueue<Duel> queue = new ObservableQueue<>(this);
  private final String name;
  private final Set<Arena> arenas = new HashSet<>();
  private final QueueDataHandler data;
  private ItemStack displayItem;
  private Kit kit;

  public DuelQueue(String queueName) {
    this.name = queueName;

    this.data = new QueueDataHandler(this);

    if (this.exists()) {
      reload();
    }

    if (AuroraDuels.getInstance().isAuroraCombatInstalled()) {
      new Rating(name + "_duels", Rating.RatingType.CUSTOM, false);
    }
  }

  public DuelQueue(String queueName, String ratingName) {
    this.name = queueName;

    this.data = new QueueDataHandler(this);

    if (this.exists()) {
      reload();
    }

    if (AuroraDuels.getInstance().isAuroraCombatInstalled()) {
      new Rating(ratingName + "_duels", Rating.RatingType.CUSTOM, false);
    }
  }

  public static void init() {
    File dir = new File(AuroraDuels.getInstance().getDataFolder(), "/queues/");

    if (AuroraDuels.getInstance().getConfig().getBoolean("queues.enable-random-queue")) {
      DuelQueue duelQueue = new DuelQueue("random", "overall");
      duelQueue.addArenas(Arena.getArenas());

      duelQueue.displayItem =
          ItemStackUtil.createDisplayItem(Material.DIAMOND_SWORD, duelQueue.getName());

      DUEL_QUEUES.add(duelQueue);
    }

    if (dir.exists()) {
      for (File file : Objects.requireNonNull(dir.listFiles())) {
        DuelQueue duelQueue = new DuelQueue(file.getName().replace(".yml", ""));
        if (duelQueue.exists()) {
          DUEL_QUEUES.add(duelQueue);
        }
      }
    }

    AuroraDuels.getInstance().getLogger().info("All queues loaded!");
  }

  public static boolean isArenasSet() {
    return arenasSet;
  }

  public static DuelQueue getUnFinishedQueue() {
    return unFinishedDuelQueue;
  }

  public static void reloadAll() {
    DUEL_QUEUES.clear();

    if (AuroraDuels.getInstance().getConfig().getBoolean("queues.enable-random-queue")) {
      DuelQueue duelQueue = new DuelQueue("random", "overall");
      duelQueue.addArenas(Arena.getArenas());

      duelQueue.displayItem =
          ItemStackUtil.createDisplayItem(Material.DIAMOND_SWORD, duelQueue.getName());

      DUEL_QUEUES.add(duelQueue);
    }

    for (DuelQueue duelQueue : getQueues()) {
      if (!Objects.equals(duelQueue.getName(), "random")) {
        duelQueue.reload();
      }
    }

    AuroraDuels.getInstance().getLogger().info("ALl queues reloaded!");
  }

  public static boolean isCreator(Player player) {
    return player.equals(currentCreator);
  }

  public static Set<DuelQueue> getQueues() {
    return DUEL_QUEUES;
  }

  public static DuelQueue getQueue(String queueName) {
    for (DuelQueue duelQueue : DUEL_QUEUES) {
      if (Objects.equals(duelQueue.name, queueName)) {
        return duelQueue;
      }
    }
    return null;
  }

  public void findMatch(Player player, boolean ranked) {
    if (DuelRequest.hasPendingRequest(player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("you-have-request"));
      return;
    }

    if (lookingForMatch.containsValue(player)) {
      lookingForMatch.clear();
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("not-looking-match"));
      return;
    } else if (DuelQueue.inQueue(player)) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("in-queue"));
      return;
    }

    player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("looking-match"));
    if (lookingForMatch.get(ranked) == null) {
      lookingForMatch.put(ranked, player);

      for (DuelRequest pendingDuelRequest : DuelRequest.getRequests(player)) {
        pendingDuelRequest.deny();
      }

      ItemMeta meta = displayItem.getItemMeta();

      meta.lore(
          List.of(
              Component.text("In Queue:" + this.inQueue()),
              Component.text("In Duel: " + Duel.inDuel(this.getName()))));

      displayItem.setItemMeta(meta);
    } else {
      Location loc = findRespawnLocation(player);

      this.enterDuelQueue(
          new StandardDuel(
              player, lookingForMatch.get(ranked), this, this.getKit(), loc, ranked, 1));
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("match-found"));
      lookingForMatch
          .get(ranked)
          .sendMessage(AuroraDuels.getInstance().getLang().getComponent("match-found"));
      lookingForMatch.clear();
    }
  }

  // TODO move this
  public static Location findRespawnLocation(Player player) {
    Location loc = Duel.getDuelLobby();

    if (loc == null && AuroraDuels.getInstance().isAuroraWarpsInstalled()) {
      WorldSpawn worldSpawn = WorldSpawn.getWorldSpawn(player.getWorld());

      if (worldSpawn != null) {
        loc = worldSpawn.getLocation();
      }

      if (loc == null) {
        loc = WorldSpawn.getGlobalSpawn();
      }
    }

    if (loc == null) {
      loc = Bukkit.getWorlds().getFirst().getSpawnLocation();
    }

    return loc;
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

    for (Duel duel : queue) {
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
      DUEL_QUEUES.remove(this);
      return;
    }

    if (!this.exists() && arenasSet) {
      arenasSet = false;
      currentCreator = player;
      unFinishedDuelQueue = this;
      displayItem =
          ItemStackUtil.createDisplayItem(
              player.getInventory().getItemInMainHand().getType(), this.getName());

      player.sendMessage(
          MiniMessage.miniMessage()
              .deserialize(
                  String.format(
                      AuroraDuels.getInstance().getLang().getString("add-arena"), arenas.size())));
    } else if (this.exists()) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("queue-already-exists"));
    } else {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("queue-being-created"));
    }
  }

  public static void onQueueSelect(QueueSelectEvent event) {
    if (event.getName().equals("ranked-match")) {
      event.getQueue().findMatch(event.getPlayer(), true);

      event.getPlayer().closeInventory();
    } else if (event.getName().equals("unranked-match")) {
      event.getQueue().findMatch(event.getPlayer(), false);

      event.getPlayer().closeInventory();
    }
  }

  public void createArenaList(Player player, String str) {
    if (str.equals("stop")) {
      arenasSet = true;
      DUEL_QUEUES.add(this);
      data.create();

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
    return queue.size() * 2 + lookingForMatch.values().size();
  }

  public void enterDuelQueue(Duel duel) {
    queue.add(duel);

    for (Player player : duel.getPlayers()) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("entered-queue"));
    }

    ItemMeta meta = displayItem.getItemMeta();

    meta.lore(
        List.of(
            Component.text("In Queue:" + this.inQueue()),
            Component.text("In Duel: " + Duel.inDuel(this.getName()))));

    displayItem.setItemMeta(meta);

    new QueueJoinEvent(duel).callEvent();
  }

  public static void removeFromQueue(Player player) {
    for (DuelQueue duelQueue : DUEL_QUEUES) {
      for (Duel duel : duelQueue.queue) {
        if (duel.contains(player)) {
          duelQueue.queue.remove(duel);

          duel.draw();
        }
      }

      for (Player matchMaker : duelQueue.lookingForMatch.values()) {
        if (matchMaker.equals(player)) {
          duelQueue.lookingForMatch.clear();
        }
      }
    }
  }

  public static int inQueues() {
    int inQueues = 0;

    for (DuelQueue duelQueue : DuelQueue.getQueues()) {
      inQueues = inQueues + duelQueue.inQueue();
    }

    return inQueues;
  }

  public static boolean inQueue(Player player) {
    for (DuelQueue duelQueue : DuelQueue.getQueues()) {
      if (duelQueue.lookingForMatch.containsValue(player)) {
        return true;
      }

      for (Player queuedPlayer : duelQueue.getPlayers()) {
        if (player.equals(queuedPlayer)) {
          return true;
        }
      }
    }
    return false;
  }

  public static class ObservableQueue<T> extends AbstractSequentialList<T> implements List<T>, Deque<T>, Listener {
    private final LinkedList<T> queue;
    private final DuelQueue duelQueue;

    public ObservableQueue(DuelQueue duelQueue) {
      this.queue = new LinkedList<>();

      this.duelQueue = duelQueue;
    }

    public boolean add(T element) {
      boolean added = queue.add(element);
      if (added) {
        checkIfAvailable();
      }
      return added;
    }

    @EventHandler
    private void checkIfAvailable(DuelEndEvent event) {
      if (checkIfAvailable()) {
        event.getHandlers().unregister(this);
      }
    }

    private boolean checkIfAvailable() {
      if (queue.isEmpty()) {
        return true;
      }
      
      Set<Arena> arenasSet = duelQueue.getArenas();
      List<Arena> arenasList = new ArrayList<>(arenasSet);
      Collections.shuffle(arenasList);
      Iterator<Arena> arenaIterator = arenasList.iterator();

      while (arenaIterator.hasNext() && !queue.isEmpty()) {
        Arena arena = arenaIterator.next();

        if (!arena.isOccupied()) {
          ((Duel) queue.getLast()).startDuel(arena);
          queue.removeLast();

          ItemMeta meta = duelQueue.getDisplayItem().getItemMeta();

          meta.lore(
              List.of(
                  Component.text("In Queue:" + duelQueue.inQueue()),
                  Component.text("In Duel: " + Duel.inDuel(duelQueue.getName()))));

          duelQueue.getDisplayItem().setItemMeta(meta);
        }
      }

      if (!queue.isEmpty()) {
        Bukkit.getPluginManager().registerEvents(this, AuroraDuels.getInstance());

        return false;
      }

      return true;
    }

    @Override
    public void push(T t) {
      queue.addFirst(t);
    }

    @Override
    public T pop() {
      return queue.removeFirst();
    }

    @Override
    public int size() {
      return queue.size();
    }

    @Override
    public boolean isEmpty() {
      return queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      return queue.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
      return queue.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
      queue.forEach(action);
    }

    @Override
    public Iterator<T> descendingIterator() {
      return queue.descendingIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
      return queue.listIterator(index);
    }

    @Override
    public Spliterator<T> spliterator() {
      return queue.spliterator();
    }

    @Override
    public Stream<T> stream() {
      return queue.stream();
    }

    @Override
    public Stream<T> parallelStream() {
      return queue.parallelStream();
    }

    @Override
    public void addFirst(T t) {
      queue.addFirst(t);
    }

    @Override
    public void addLast(T t) {
      queue.addLast(t);
    }

    @Override
    public T getFirst() {
      return queue.getFirst();
    }

    @Override
    public T getLast() {
      return queue.getLast();
    }

    @Override
    public T removeFirst() {
      return queue.removeFirst();
    }

    @Override
    public T removeLast() {
      return queue.removeLast();
    }

    @Override
    public LinkedList<T> reversed() {
      LinkedList<T> reversedList = new LinkedList<>(queue);
      Collections.reverse(reversedList);
      return reversedList;
    }

    @Override
    public Object[] toArray() {
      return queue.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
      return queue.toArray(a);
    }

    @Override
    public <T1> T1[] toArray(IntFunction<T1[]> generator) {
      return queue.toArray(generator);
    }

    @Override
    public boolean remove(Object o) {
      return queue.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return queue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
      return queue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      return queue.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
      return queue.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return queue.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
      queue.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super T> c) {
      queue.sort(c);
    }

    @Override
    public void clear() {
      queue.clear();
    }

    @Override
    public boolean offer(T t) {
      return queue.offer(t);
    }

    @Override
    public T remove() {
      return queue.remove();
    }

    @Override
    public T poll() {
      return queue.poll();
    }

    @Override
    public T element() {
      return queue.element();
    }

    @Override
    public T peek() {
      return queue.peek();
    }

    @Override
    public T get(int index) {
      return queue.get(index);
    }

    @Override
    public T set(int index, T element) {
      return queue.set(index, element);
    }

    @Override
    public void add(int index, T element) {
      queue.add(index, element);
    }

    @Override
    public T remove(int index) {
      return queue.remove(index);
    }

    @Override
    public int indexOf(Object o) {
      return queue.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
      return queue.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
      return queue.listIterator();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
      return queue.subList(fromIndex, toIndex);
    }

    @Override
    public boolean offerFirst(T e) {
      return queue.offerFirst(e);
    }

    @Override
    public boolean offerLast(T e) {
      return queue.offerLast(e);
    }

    @Override
    public T pollFirst() {
      return queue.pollFirst();
    }

    @Override
    public T pollLast() {
      return queue.pollLast();
    }

    @Override
    public T peekFirst() {
      return queue.peekFirst();
    }

    @Override
    public T peekLast() {
      return queue.peekLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
      return queue.removeFirstOccurrence(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
      return queue.removeLastOccurrence(o);
    }
  }
}
