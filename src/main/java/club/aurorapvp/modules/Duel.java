package club.aurorapvp.modules;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.events.custom.DuelEndEvent;
import club.aurorapvp.events.custom.DuelStartEvent;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class Duel implements Listener {
  private static final NamespacedKey key = new NamespacedKey(AuroraDuels.INSTANCE, "duelkit");
  private static Set<Duel> duels = new HashSet<>();
  private final Set<Player> spectators = new HashSet<>();
  private Long timeStarted;
  private final Timer t = new Timer();
  private final Player playerOne;
  private final Player playerTwo;
  private final boolean ranked;
  private boolean active = false;
  private Arena arena;

  public Duel(Player p1, Player p2, boolean ranked) {
    this.playerOne = p1;
    this.playerTwo = p2;
    this.ranked = ranked;
  }

  public static void init() {
    if (AuroraDuels.isAuroraCombatInstalled()) {
      Rating.setupRating("duels", false);
    }
  }

  public Long getElapsed() {
    return System.currentTimeMillis() - timeStarted;
  }

  public Player getPlayerOne() {
    return playerOne;
  }

  public Player getPlayerTwo() {
    return playerOne;
  }

  public Player getOpponent(Player p) {
    if (p == playerOne) {
      return playerTwo;
    } else if (p == playerTwo) {
      return playerOne;
    } else {
      return null;
    }
  }

  public boolean isRanked() {
    return ranked;
  }

  public Arena getArena() {
    return arena;
  }

  public void startDuel(Kit kit, Arena arena) {
    if (kit != null) {
      Kit.giveKit(kit, playerOne);
      Kit.giveKit(kit, playerTwo);
    } else {
      Kit kit1 = Duel.getDuelKit(playerOne);
      Kit kit2 = Duel.getDuelKit(playerTwo);

      if (kit1 != null) {
        Kit.giveKit(kit1, playerOne);
      }

      if (kit2 != null) {
        Kit.giveKit(kit2, playerTwo);
      }
    }

    this.arena = arena;

    Location spawn1 =
        new Location(arena.getWorld(),
            arena.getFirstSpawn().getX(), arena.getFirstSpawn().getY(),
            arena.getFirstSpawn().getZ());
    Location spawn2 =
        new Location(arena.getWorld(),
            arena.getSecondSpawn().getX(), arena.getSecondSpawn().getY(),
            arena.getSecondSpawn().getZ());

    playerOne.teleportAsync(spawn1);
    playerTwo.teleportAsync(spawn2);

    final Title title1 =
        Title.title(Lang.getComponent("duel-started"),
            Component.text("Opponent: " + playerTwo.getName()));
    final Title title2 =
        Title.title(Lang.getComponent("duel-started"),
            Component.text("Opponent: " + playerOne.getName()));

    playerOne.showTitle(title1);
    playerTwo.showTitle(title2);

    Duel thisDuel = this;

    t.schedule(new TimerTask() {
      @Override
      public void run() {
        if (thisDuel.isActive()) {
          playerOne.sendMessage(Lang.getComponent("duel-time-expired"));
          playerTwo.sendMessage(Lang.getComponent("duel-time-expired"));
          thisDuel.endDuel();
        }
        this.cancel();
      }
    }, Config.get().getInt("duels.max-duration"));

    Queue.getQueue(playerOne).exitDuelQueue(playerOne);
    arena.setOccupied(true);
    duels.add(this);
    timeStarted = System.currentTimeMillis();
    active = true;

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelStartEvent(thisDuel));
      }
    }.runTask(AuroraDuels.INSTANCE);
  }

  public void endDuel() {
    if (playerOne.isDead() || !playerOne.isOnline()) {
      playerTwo.showTitle(
          Title.title(Lang.getComponent("you-won"),
              Component.text("Opponent: " + playerOne.getName())));
      playerOne.sendMessage(Lang.getComponent("you-lost"));
    } else if (playerTwo.isDead() || !playerTwo.isOnline()) {
      playerOne.showTitle(
          Title.title(Lang.getComponent("you-won"),
              Component.text("Opponent: " + playerTwo.getName())));
      playerTwo.sendMessage(Lang.getComponent("you-lost"));
    } else {
      playerOne.sendMessage(Lang.getComponent("draw"));
      playerTwo.sendMessage(Lang.getComponent("draw"));
    }

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        playerOne.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
        playerTwo.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
        arena.reset();
        arena.setOccupied(false);
        this.cancel();
      }
    }, 3000);

    this.active = false;
    Duel thisDuel = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelEndEvent(thisDuel));
      }
    }.runTask(AuroraDuels.INSTANCE);

    duels.remove(this);
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

  public Player[] getSpectators() {
    return spectators.toArray(new Player[0]);
  }

  public boolean isActive() {
    return active;
  }

  public static Kit getDuelKit(Player p) {
    PersistentDataContainer container = p.getPersistentDataContainer();

    return Kit.getKit(p, container.get(key, PersistentDataType.STRING));
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
      if ((duel.playerOne.equals(p) || duel.playerTwo.equals(p)) && duel.isActive()) {
        return true;
      }
    }
    return false;
  }

  public static Duel getDuel(Player p) {
    for (Duel duel : duels) {
      if (duel.playerOne.equals(p) || duel.playerTwo.equals(p)) {
        return duel;
      }

    }
    return null;
  }
}
