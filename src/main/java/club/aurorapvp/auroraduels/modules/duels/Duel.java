package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Duel {

  private static final NamespacedKey duelKitKey =
      new NamespacedKey(AuroraDuels.getInstance(), "duelkit");
  private static final NamespacedKey winStreakKey =
      new NamespacedKey(AuroraDuels.getInstance(), "duel-winstreak");
  private static final Map<UUID, Duel> LAST_DUEL = new HashMap<>();
  protected static Set<Duel> duels = new HashSet<>();
  protected final Set<Player> players = new HashSet<>();
  protected final Set<Player> spectators = new HashSet<>();
  protected final Set<BossBar> spectatorBars = new HashSet<>();
  protected final Location respawnLocation;
  private final boolean ranked;
  protected Long timeStarted;
  protected Kit kit;
  protected Arena arena;
  protected int rounds = 1;
  protected int currentRound = 1;
  private Queue queue;
  private boolean active = false;
  private final Duel thisDuel = this;
  public BukkitRunnable bossbarTask;
  private final BukkitRunnable checkBounds = new BukkitRunnable() {
    @Override
    public void run() {
      if (!thisDuel.isActive()) {
        this.cancel();
        return;
      }

      try {
        for (Player player : thisDuel.getPlayers()) {
          if (!thisDuel.isSpectator(player) && !thisDuel.getArena()
              .isInBounds(player.getLocation())) {
            player.setHealth(0);
            player.sendMessage(
                AuroraDuels.getInstance().getLang().getComponent("no-leaving-arena"));
          }
        }

        for (Player player : thisDuel.getSpectators()) {
          if (!thisDuel.getArena().isInBounds(player.getLocation())) {
            for (Player duelingPlayer : thisDuel.getPlayers()) {
              if (!thisDuel.isSpectator(duelingPlayer)) {
                player.teleportAsync(duelingPlayer.getLocation());
                break;
              }
            }
            player.sendMessage(
                AuroraDuels.getInstance().getLang().getComponent("no-leaving-arena"));
          }
        }
      } catch (IllegalArgumentException e) {
        this.cancel();
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

  public static void addWin(Player player) {
    PersistentDataContainer container = player.getPersistentDataContainer();

    int wins = container.getOrDefault(winStreakKey, PersistentDataType.INTEGER, 0);

    container.set(winStreakKey, PersistentDataType.INTEGER, wins + 1);

    player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("new-win"));
  }

  public static void resetStreak(Player player) {
    PersistentDataContainer container = player.getPersistentDataContainer();

    container.set(winStreakKey, PersistentDataType.INTEGER, 0);
  }

  public static int getWins(Player player) {
    PersistentDataContainer container = player.getPersistentDataContainer();

    return container.getOrDefault(winStreakKey, PersistentDataType.INTEGER, 0);
  }

  public static Kit getDuelKit(Player player) {
    PersistentDataContainer container = player.getPersistentDataContainer();

    return Kit.getKit(player, container.get(duelKitKey, PersistentDataType.STRING));
  }

  public static void setLastDuel(Player player, Duel duel) {
    LAST_DUEL.put(player.getUniqueId(), duel);
  }

  public static Duel getLastDuel(Player player) {
    return LAST_DUEL.get(player.getUniqueId());
  }

  public static void setDuelKit(Player player, String kitName) {
    PersistentDataContainer container = player.getPersistentDataContainer();

    container.set(duelKitKey, PersistentDataType.STRING, kitName);

    player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("duel-kit"));
  }

  public static void unSetDuelKit(Player player) {
    PersistentDataContainer container = player.getPersistentDataContainer();

    container.remove(duelKitKey);

    player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("duel-kit"));
  }

  public static Location getDuelLobby() {
    return AuroraDuels.getInstance().getConfig().getLocation("lobby");
  }

  // TODO do this better
  public static void setDuelLobby(Location loc) {
    AuroraDuels.getInstance().getConfig().set("lobby", loc);

    try {
      AuroraDuels.getInstance().getConfig()
          .save(new File(AuroraDuels.getInstance().getDataFolder(), "config.yml"));
    } catch (IOException e) {
      AuroraDuels.getInstance().getLogger().log(Level.SEVERE, "Failed to save config file", e);
    }
  }

  public static int inDuels() {
    int inDuels = 0;

    for (Duel duel : duels) {
      if (duel.isActive()) {
        inDuels = inDuels + duel.inDuel();
      }
    }

    return inDuels;
  }

  public static Set<Duel> getDuels() {
    return duels;
  }

  public static int inDuel(String queueName) {
    return Duel.getDuels().stream()
        .filter(Duel::isActive)
        .filter(duel -> Optional.ofNullable(duel.getQueue())
            .map(Queue::getName)
            .filter(queueName::equals)
            .isPresent())
        .mapToInt(Duel::inDuel)
        .sum();
  }

  public static boolean inDuel(Player player) {
    for (Duel duel : duels) {
      if ((duel.contains(player) || duel.isSpectator(player)) && duel.isActive()) {
        return true;
      }
    }
    return false;
  }

  public static Duel getDuel(Player player) {
    for (Duel duel : duels) {
      if (duel.contains(player)) {
        return duel;
      }
    }
    return null;
  }

  public abstract void startDuel(Arena arena);

  public abstract void endDuel(Collection<Player> winners, Collection<Player> losers);

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

  public void setSpectator(Player player, boolean spectator) {
    if (spectator) {
      for (Player duelingPlayer : thisDuel.getPlayers()) {
        if (!thisDuel.isSpectator(duelingPlayer)) {
          player.teleportAsync(duelingPlayer.getLocation());
          break;
        }
      }

      player.setGameMode(GameMode.SPECTATOR);
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("start-spectating"));

      for (BossBar bar : spectatorBars) {
        player.showBossBar(bar);
      }

      spectators.add(player);
    } else {
      player.setGameMode(Bukkit.getDefaultGameMode());
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("stop-spectating"));

      spectators.remove(player);

      for (BossBar bar : spectatorBars) {
        if (bar != null) {
          player.hideBossBar(bar);
        }
      }

      player.teleportAsync(this.getRespawnLocation());
    }
  }

  public boolean contains(Player player) {
    return players.contains(player);
  }

  public boolean isSpectator(Player player) {
    return spectators.contains(player);
  }
}
