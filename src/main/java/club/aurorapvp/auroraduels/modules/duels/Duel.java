package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.DuelStats;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
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
  private static final Map<UUID, Duel> LAST_DUEL = new HashMap<>();
  protected static final Set<Duel> duels = new HashSet<>();
  private final Map<UUID, DuelStats> stats = new HashMap<>();
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
  private DuelQueue duelQueue;
  private boolean active = false;
  private boolean starting = false;
  private final Duel thisDuel = this;
  private final BukkitRunnable checkBounds =
      new BukkitRunnable() {
        @Override
        public void run() {
          if (!thisDuel.isActive()) {
            this.cancel();
            return;
          }

          try {
            for (Player player : thisDuel.getPlayers()) {
              if (!thisDuel.isSpectating(player)
                  && !thisDuel.getArena().isInBounds(player.getLocation())) {
                player.setHealth(0);
                player.sendMessage(
                    AuroraDuels.getInstance().getLang().getComponent("no-leaving-arena"));
              }
            }

            for (Player player : thisDuel.getSpectators()) {
              if (!thisDuel.getArena().isInBounds(player.getLocation())) {
                for (Player duelingPlayer : thisDuel.getPlayers()) {
                  if (!thisDuel.isSpectating(duelingPlayer)) {
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
    this(players, respawnLocation, null, null, ranked, 0);
  }

  public Duel(Set<Player> players, Location respawnLocation, boolean ranked, int rounds) {
    this(players, respawnLocation, null, null, ranked, rounds);
  }

  public Duel(
      Set<Player> players, Location respawnLocation, DuelQueue duelQueue, Kit kit, boolean ranked) {
    this(players, respawnLocation, duelQueue, kit, ranked, 0);
  }

  public Duel(
      Set<Player> players,
      Location respawnLocation,
      DuelQueue duelQueue,
      Kit kit,
      boolean ranked,
      int rounds) {
    this.respawnLocation = respawnLocation;
    this.players.addAll(players);
    this.ranked = ranked;
    this.duelQueue = duelQueue;
    this.kit = kit;
    this.rounds = rounds;
    duels.add(this);
    checkBounds.runTaskTimer(AuroraDuels.getInstance(), 200L, 200L);

    for (Player player : this.getPlayers()) {
      stats.put(player.getUniqueId(), new DuelStats(
          player,
          this.getArena(),
          Rating.getRating("overall_duels").getScore(player).getPoints()));
    }
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
      AuroraDuels.getInstance()
          .getConfig()
          .save(new File(AuroraDuels.getInstance().getDataFolder(), "config.yml"));
    } catch (IOException e) {
      AuroraDuels.getInstance().getLogger().log(Level.SEVERE, "Failed to save config file", e);
    }
  }

  public static int inDuels(boolean ranked) {
    int inDuels = 0;

    for (Duel duel : duels) {
      if (duel.isActive() && duel.isRanked() == ranked) {
        inDuels = inDuels + duel.inDuel();
      }
    }

    return inDuels;
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
        .filter(
            duel ->
                Optional.ofNullable(duel.getQueue())
                    .map(DuelQueue::getName)
                    .filter(queueName::equals)
                    .isPresent())
        .mapToInt(Duel::inDuel)
        .sum();
  }

  public static boolean inDuel(Player player) {
    for (Duel duel : duels) {
      if ((duel.contains(player) || duel.isSpectating(player)) && duel.isActive()) {
        return true;
      }
    }
    return false;
  }

  public static boolean inDuelCountdown(Player player) {
    for (Duel duel : duels) {
      if ((duel.contains(player) || duel.isSpectating(player))
          && duel.isActive()
          && duel.isStarting()) {
        return true;
      }
    }
    return false;
  }

  public static boolean isSpectator(Player player) {
    for (Duel duel : duels) {
      if (duel.isSpectating(player)) {
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

  public DuelQueue getQueue() {
    return duelQueue;
  }

  public void setQueue(DuelQueue duelQueue) {
    this.duelQueue = duelQueue;
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

  public boolean isStarting() {
    return starting;
  }

  public void setStarting(boolean starting) {
    this.starting = starting;
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

  public void addTotemPop(Player player) {
    stats.get(player.getUniqueId()).addTotemPop();
  }

  public void addDamagedReceived(Player player, double damage) {
    stats.get(player.getUniqueId()).addDamageReceived((int) damage);
  }

  public void addDamagedDealt(Player player, double damage) {
    stats.get(player.getUniqueId()).addDamageDealt((int) damage);
  }

  public void setHealthRemaining(Player player, double healthRemaining) {
    stats.get(player.getUniqueId()).setHealthRemaining((int) healthRemaining);
  }

  public void setSpectator(Player player, boolean spectator) {
    if (spectator) {
      for (Player duelingPlayer : thisDuel.getPlayers()) {
        if (!thisDuel.isSpectating(duelingPlayer)) {
          player.teleportAsync(duelingPlayer.getLocation());
          break;
        }
      }

      player.setGameMode(GameMode.SPECTATOR);
      player.sendMessage(
          AuroraDuels.getInstance()
              .getLang()
              .getComponentCommand(
                  "start-spectating", "Click to stop Spectating", "/duel spectate"));

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

  public boolean isSpectating(Player player) {
    return spectators.contains(player);
  }
}
