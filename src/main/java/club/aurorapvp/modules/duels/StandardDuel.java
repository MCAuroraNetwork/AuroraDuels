package club.aurorapvp.modules.duels;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.events.custom.DuelEndEvent;
import club.aurorapvp.events.custom.DuelStartEvent;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Kit;
import club.aurorapvp.modules.Queue;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class StandardDuel extends Duel implements Listener {
  private static final Set<StandardDuel> STANDARD_DUELS = new HashSet<>();
  private final Player playerOne;
  private final Player playerTwo;

  public StandardDuel(Player p1, Player p2, Queue queue, boolean ranked) {
    super(Set.of(p1, p2), queue, ranked);
    this.playerOne = p1;
    this.playerTwo = p2;
    STANDARD_DUELS.add(this);
  }

  public static StandardDuel getStandardDuel(Player p) {
    for (StandardDuel duel : STANDARD_DUELS) {
      if (duel.contains(p)) {
        return duel;
      }
    }
    return null;
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

  public void startDuel(Arena arena, Kit kit) {
    this.arena = arena;

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

    StandardDuel thisDuel = this;

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        if (thisDuel.isActive()) {
          playerOne.sendMessage(Lang.getComponent("duel-time-expired"));
          playerTwo.sendMessage(Lang.getComponent("duel-time-expired"));
          thisDuel.draw();
        }
        this.cancel();
      }
    }, Config.get().getInt("duels.max-duration"));

    Objects.requireNonNull(Queue.getQueue(playerOne)).exitDuelQueue(playerOne);
    arena.setOccupied(true);
    timeStarted = System.currentTimeMillis();
    active = true;

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelStartEvent(thisDuel));
      }
    }.runTask(AuroraDuels.INSTANCE);
  }

  public void endDuel(Player[] winner, Player[] loser) {
    winner[0].showTitle(
        Title.title(Lang.getComponent("you-won"),
            Component.text("Opponent: " + playerOne.getName())));
    loser[0].sendMessage(Lang.getComponent("you-lost"));

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
        Bukkit.getServer().getPluginManager().callEvent(new DuelEndEvent(thisDuel, winner, loser));
      }
    }.runTask(AuroraDuels.INSTANCE);

    STANDARD_DUELS.remove(this);
  }

  public void draw() {
    for (Player p : this.getPlayers()) {
      p.sendMessage(Lang.getComponent("draw"));
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
        Bukkit.getServer().getPluginManager().callEvent(new DuelEndEvent(thisDuel, null, null));
      }
    }.runTask(AuroraDuels.INSTANCE);

    STANDARD_DUELS.remove(this);
  }

  @SuppressWarnings("unused")
  public static Set<StandardDuel> getStandardDuels() {
    return STANDARD_DUELS;
  }
}