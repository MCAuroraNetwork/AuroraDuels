package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public abstract class Duel {
  private static final NamespacedKey duelKitKey = new NamespacedKey(AuroraDuels.INSTANCE, "duelkit");
  private static final HashMap<Player, Duel> LAST_DUEL = new HashMap<>();
  protected static Set<Duel> duels = new HashSet<>();
  protected final Set<Player> players = new HashSet<>();
  protected final Set<Player> spectators = new HashSet<>();
  protected final Location respawnLocation;
  private final boolean ranked;
  private static final NamespacedKey winStreakKey = new NamespacedKey(AuroraDuels.INSTANCE, "duel-winstreak");
  protected Long timeStarted;
  protected Kit kit;
  protected Arena arena;
  protected int rounds = 1;
  protected int currentRound = 1;
  private Queue queue;
  private boolean active = false;
  protected int taskId;
  protected final Runnable checkBounds = () -> {
    for (Player p : this.getPlayers()) {
      Arena arena = this.getArena();
      if (!Arena.isInBounds(p.getLocation(), arena) && !this.isSpectator(p)) {
        p.setHealth(0);
        p.sendMessage(Lang.getComponent("no-leaving-arena"));
      }
    }

    for (Player p : this.getSpectators()) {
      if (!Arena.isInBounds(p.getLocation(), arena)) {
        for (Player player : this.getPlayers()) {
          if (!this.isSpectator(player)) {
            p.teleportAsync(player.getLocation());
          }
        }
        p.sendMessage(Lang.getComponent("no-leaving-arena"));
      }
    }
  };

  public Duel(Set<Player> players, Location respawnLocation, boolean ranked) {
    this.respawnLocation = respawnLocation;
    this.players.addAll(players);
    this.ranked = ranked;
    duels.add(this);
  }

  public Duel(Set<Player> players, Location respawnLocation, boolean ranked, int rounds) {
    this.respawnLocation = respawnLocation;
    this.players.addAll(players);
    this.ranked = ranked;
    this.rounds = rounds;
    duels.add(this);
  }

  public Duel(Set<Player> players, Location respawnLocation, Queue queue, Kit kit, boolean ranked) {
    this.respawnLocation = respawnLocation;
    this.players.addAll(players);
    this.ranked = ranked;
    this.queue = queue;
    this.kit = kit;
    duels.add(this);
  }

  public Duel(Set<Player> players, Location respawnLocation, Queue queue, Kit kit, boolean ranked,
              int rounds) {
    this.respawnLocation = respawnLocation;
    this.players.addAll(players);
    this.ranked = ranked;
    this.queue = queue;
    this.kit = kit;
    this.rounds = rounds;
    duels.add(this);
  }

  public static void addWin(Player p) {
    PersistentDataContainer container = p.getPersistentDataContainer();

    int wins = container.getOrDefault(winStreakKey, PersistentDataType.INTEGER, 0);

    container.set(winStreakKey, PersistentDataType.INTEGER, wins + 1);
  }

  public static void resetStreak(Player p) {
    PersistentDataContainer container = p.getPersistentDataContainer();

    container.set(winStreakKey, PersistentDataType.INTEGER, 0);
  }

  public static int getWins(Player p) {
    PersistentDataContainer container = p.getPersistentDataContainer();

    return container.getOrDefault(winStreakKey, PersistentDataType.INTEGER, 0);
  }

  public static Kit getDuelKit(Player p) {
    PersistentDataContainer container = p.getPersistentDataContainer();

    return Kit.getKit(p, container.get(duelKitKey, PersistentDataType.STRING));
  }

  public static void setLastDuel(Player p, Duel duel) {
    LAST_DUEL.put(p, duel);
  }

  public static Duel getLastDuel(Player p) {
    if (LAST_DUEL.containsKey(p)) {
      return LAST_DUEL.get(p);
    }

    return null;
  }

  public static void setDuelKit(Player p, String kitName) {
    PersistentDataContainer container = p.getPersistentDataContainer();

    container.set(duelKitKey, PersistentDataType.STRING, kitName);

    p.sendMessage(Lang.getComponent("duel-kit"));
  }

  public static int inDuels() {
    int inDuels = 0;

    for (Duel duel : duels) {
      inDuels = inDuels + duel.inDuel();
    }

    return inDuels;
  }

  public static Set<Duel> getDuels() {
    return duels;
  }

  public static int inDuel(String queueName) {
    int inDuel = 0;

    for (Duel duel : Duel.getDuels()) {
      // TODO solve placeholder NPE more gracefully
      Queue queue = duel.getQueue();

      if (queue != null && Objects.equals(duel.getQueue().getName(), queueName)) {
        inDuel = inDuel + duel.inDuel();
      }
    }
    return inDuel;
  }

  public static boolean inDuel(Player p) {
    for (Duel duel : duels) {
      if ((duel.contains(p) || duel.isSpectator(p)) && duel.isActive()) {
        return true;
      }
    }
    return false;
  }

  public static Duel getDuel(Player p) {
    for (Duel duel : duels) {
      if (duel.contains(p)) {
        return duel;
      }
    }
    return null;
  }

  public abstract void startDuel(Arena arena);

  public abstract void endDuel(Player[] winners, Player[] losers);

  public abstract void draw();

  public Queue getQueue() {
    return queue;
  }

  public void setQueue(Queue queue) {
    this.queue = queue;
  }

  public Kit getKit() {
    return kit;
  }

  public void setKit(Kit kit) {
    this.kit = kit;
  }

  public Long getElapsed() {
    return System.currentTimeMillis() - timeStarted;
  }

  public Arena getArena() {
    return arena;
  }

  public Set<Player> getPlayers() {
    return players;
  }

  public Set<Player> getSpectators() {
    return spectators;
  }

  public Location getRespawnLocation() {
    return respawnLocation;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isRanked() {
    return ranked;
  }

  public int getRounds() {
    return rounds;
  }

  public int inDuel() {
    return this.getPlayers().size();
  }

  public void setSpectator(Player p, boolean spectator) {
    if (spectator) {
      BlockVector3 midpoint = arena.getFirstSpawn().add(arena.getSecondSpawn()).divide(2);
      Location loc =
          new Location(arena.getWorld(), midpoint.getX(), midpoint.getY(), midpoint.getZ());

      p.teleportAsync(loc);
      p.setGameMode(GameMode.SPECTATOR);
      p.sendMessage(Lang.getComponent("start-spectating"));

      spectators.add(p);
    } else {
      p.teleportAsync(Bukkit.getWorlds().get(0).getSpawnLocation());
      p.setGameMode(Bukkit.getDefaultGameMode());
      p.sendMessage(Lang.getComponent("stop-spectating"));

      spectators.remove(p);
    }
  }

  public boolean contains(Player p) {
    return players.contains(p);
  }

  public boolean isSpectator(Player p) {
    return spectators.contains(p);
  }
}