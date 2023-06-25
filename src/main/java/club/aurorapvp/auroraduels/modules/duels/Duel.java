package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.aurorakits.modules.Kit;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public abstract class Duel {
  public static Set<Duel> duels = new HashSet<>();
  private static final NamespacedKey key = new NamespacedKey(AuroraDuels.INSTANCE, "duelkit");
  private static final HashMap<Player, Duel> LAST_DUEL = new HashMap<>();
  protected final Set<Player> players = new HashSet<>();
  protected final Set<Player> spectators = new HashSet<>();
  protected Long timeStarted;
  private final boolean ranked;
  private final Queue queue;
  protected final Kit kit;
  protected boolean active = false;
  protected Arena arena;

  public Duel(Set<Player> players, Queue queue, Kit kit, boolean ranked) {
    this.players.addAll(players);
    this.ranked = ranked;
    this.queue = queue;
    this.kit = kit;
    duels.add(this);
  }

  public abstract void startDuel(Arena arena);

  public abstract void endDuel(Player[] winners, Player[] losers);

  public abstract void draw();

  public Queue getQueue() {
    return queue;
  }

  public Kit getKit() {
    return kit;
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

  public boolean isActive() {
    return active;
  }

  public boolean isRanked() {
    return ranked;
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

  public static Kit getDuelKit(Player p) {
    PersistentDataContainer container = p.getPersistentDataContainer();

    return Kit.getKit(p, container.get(key, PersistentDataType.STRING));
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

    container.set(key, PersistentDataType.STRING, kitName);

    p.sendMessage(Lang.getComponent("duel-kit"));
  }

  public static int inDuels() {
    return duels.size();
  }

  public static boolean inDuel(Player p) {
    for (Duel duel : duels) {
      if ((duel.contains(p) || duel.contains(p)) && duel.isActive()) {
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
}