package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class StandardDuel extends Duel implements Listener {
  private final Map<Player, Integer> wins = new HashMap<>();
  private final Player playerOne;
  private final Player playerTwo;

  public StandardDuel(Player p1, Player p2, boolean ranked, int rounds) {
    super(Set.of(p1, p2), ranked);
    this.playerOne = p1;
    this.playerTwo = p2;
    this.rounds = rounds;
  }

  public StandardDuel(Player p1, Player p2, Queue queue, Kit kit, boolean ranked, int rounds) {
    super(Set.of(p1, p2), queue, kit, ranked);
    this.playerOne = p1;
    this.playerTwo = p2;
    this.rounds = rounds;
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

  public void startDuel(Arena arena) {
    this.arena = arena;

    if (rounds % 2 == 0) {
      for (Player p : this.players) {
        p.sendMessage(Lang.getComponent("even-rounds"));
      }
    }

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

    playerOne.spigot().respawn();
    playerTwo.spigot().respawn();

    playerOne.teleportAsync(spawn1);
    playerTwo.teleportAsync(spawn2);

    this.setActive(true);
    arena.setOccupied(true);
    executor.scheduleAtFixedRate(checkBounds, 60L, 1L, TimeUnit.SECONDS);

    final Title title1 =
        Title.title(Lang.getComponent("duel-started"),
            Component.text("Opponent: " + playerTwo.getName()));
    final Title title2 =
        Title.title(Lang.getComponent("duel-started"),
            Component.text("Opponent: " + playerOne.getName()));

    if (rounds > 1) {
      playerOne.sendActionBar(Component.text("Round " + currentRound + " of " + rounds));
      playerTwo.sendActionBar(Component.text("Round " + currentRound + " of " + rounds));
    }

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
    timeStarted = System.currentTimeMillis();

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelStartEvent(thisDuel));
      }
    }.runTask(AuroraDuels.INSTANCE);
  }

  public void endDuel(Player[] winner, Player[] loser) {
    if (rounds > currentRound) {
      if (wins.containsKey(winner[0])) {
        int currentWins = wins.get(winner[0]);

        wins.put(winner[0], currentWins + 1);
      } else {
        wins.put(winner[0], 1);
        return;
      }

      if (!(wins.get(winner[0]) >= (rounds / 2 + 1))) {
        this.startDuel(arena);
        return;
      }
    }

    winner[0].showTitle(
        Title.title(Lang.getComponent("you-won"),
            Component.text("Opponent: " + loser[0].getName())));
    loser[0].showTitle(
        Title.title(Lang.getComponent("you-lost"),
            Component.text("Opponent: " + winner[0].getName())));

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

    this.setActive(false);

    Duel thisDuel = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelEndEvent(thisDuel, winner, loser));
      }
    }.runTask(AuroraDuels.INSTANCE);

    duels.remove(this);
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

    this.setActive(false);
    Duel thisDuel = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelEndEvent(thisDuel, null, null));
      }
    }.runTask(AuroraDuels.INSTANCE);

    duels.remove(this);
  }
}