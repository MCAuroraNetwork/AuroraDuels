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
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FFADuel extends Duel {
  private final Map<Player, Boolean> deadPlayers = new HashMap<>();
  private final Random random = new Random();

  public FFADuel(Set<Player> players, boolean ranked) {
    super(players, ranked);
  }

  public FFADuel(Set<Player> players, Queue queue, Kit kit, boolean ranked) {
    super(players, queue, kit, ranked);
  }

  @SuppressWarnings("unused")
  public Player[] getOpponents(Player p) {
    Set<Player> opponents = new HashSet<>(this.getPlayers());
    opponents.remove(p);

    return opponents.toArray(new Player[0]);
  }

  public void setDead(Player p) {
    deadPlayers.put(p, true);
  }

  public boolean onePlayerAlive() {
    int aliveCount = 0;

    for (Boolean isDead : deadPlayers.values()) {
      if (!isDead) {
        aliveCount++;
      }
    }

    return aliveCount == 1;
  }

  // TODO janky method
  public Player getFirstAlive() {
    for (Player p : deadPlayers.keySet()) {
      if (!deadPlayers.get(p)) {
        return p;
      }
    }
    return null;
  }

  public void startDuel(Arena arena) {
    this.arena = arena;

    if (kit != null) {
      for (Player p : this.getPlayers()) {
        Kit.giveKit(kit, p);
      }
    } else {
      for (Player p : this.getPlayers()) {
        Kit playerKit = Duel.getDuelKit(p);

        if (playerKit != null) {
          Kit.giveKit(playerKit, p);
        }
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

    for (Player p : this.getPlayers()) {
      p.teleportAsync(Objects.requireNonNull(findSafeSpawnLocation(spawn1, spawn2)));
    }

    this.setActive(true);
    arena.setOccupied(true);
    taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(AuroraDuels.INSTANCE, checkBounds, 200L, 200L);

    StringBuilder sb;
    String names;
    Title title;

    Set<Player> players = this.getPlayers();

    for (Player currentPlayer : players) {
      deadPlayers.put(currentPlayer, false);

      sb = new StringBuilder();

      for (Player opponent : players) {
        if (currentPlayer != opponent) {
          sb.append(opponent.getName());
          sb.append(", ");
        }
      }

      names = sb.toString();
      if (names.length() > 2) {
        names = names.substring(0, names.length() - 2);
      }

      title = Title.title(Lang.getComponent("duel-started"),
          Component.text("Opponents: " + names));

      currentPlayer.showTitle(title);
    }

    FFADuel thisDuel = this;

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        if (thisDuel.isActive()) {
          for (Player p : thisDuel.getPlayers()) {
            p.sendMessage(Lang.getComponent("duel-time-expired"));
            p.sendMessage(Lang.getComponent("duel-time-expired"));
          }
          thisDuel.draw();
        }
        this.cancel();
      }
    }, Config.get().getInt("duels.max-duration"));

    for (Player p : this.getPlayers()) {
      Objects.requireNonNull(Queue.getQueue(p)).exitDuelQueue(p);
      break;
    }

    timeStarted = System.currentTimeMillis();

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelStartEvent(thisDuel));
      }
    }.runTask(AuroraDuels.INSTANCE);
  }

  public void endDuel(Player[] player, Player[] losers) {
    Bukkit.getScheduler().cancelTask(taskId);

    Player winner = player[0];

    String winnerName = winner.getName();

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < losers.length; i++) {
      sb.append(losers[i].getName());
      if (i < losers.length - 1) {
        sb.append(", ");
      }
    }

    String loserNames = sb.toString();

    winner.showTitle(
        Title.title(Lang.getComponent("you-won"), Component.text("Opponents: " + loserNames)));

    for (Player p : losers) {
      p.showTitle(
          Title.title(Lang.getComponent("you-lost"), Component.text("Winner: " + winnerName)));
    }

    Duel thisDuel = this;

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        for (Player p : thisDuel.getPlayers()) {
          p.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
        }
        arena.reset();
        arena.setOccupied(false);
        this.cancel();
      }
    }, 3000);

    this.setActive(false);

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager()
            .callEvent(new DuelEndEvent(thisDuel, new Player[] {winner}, losers));
      }
    }.runTask(AuroraDuels.INSTANCE);

    duels.remove(this);
  }

  private Location findSafeSpawnLocation(Location spawn1, Location spawn2) {
    double x = (spawn1.getX() + spawn2.getX()) / 2;
    double y = (spawn1.getY() + spawn2.getY()) / 2;
    double z = (spawn1.getZ() + spawn2.getZ()) / 2;

    Location center = new Location(spawn1.getWorld(), x, y, z);
    int radius = (int) Math.floor(spawn1.distance(spawn2));

    World world = center.getWorld();

    int maxAttempts = 100;
    int attempts = 0;

    while (attempts < maxAttempts) {
      double angle = random.nextDouble() * Math.PI * 2;
      double distance = random.nextDouble() * radius;
      x = center.getX() + distance * Math.cos(angle);
      z = center.getZ() + distance * Math.sin(angle);

      Location spawnLocation = new Location(world, x, center.getY(), z);

      if (isSafeSpawnLocation(spawnLocation)) {
        return spawnLocation;
      }

      attempts++;
    }

    return null;
  }

  private boolean isSafeSpawnLocation(Location location) {
    Material blockType = location.getBlock().getType();
    Material belowBlockType = location.clone().add(0, -1, 0).getBlock().getType();
    Material aboveBlockType = location.clone().add(0, 1, 0).getBlock().getType();

    boolean isAir = blockType == Material.AIR;
    boolean isNotDangerous =
        !blockType.isOccluding() && !blockType.isSolid() && blockType != Material.LAVA &&
            blockType != Material.MAGMA_BLOCK;
    boolean isOnSolidGround = belowBlockType.isSolid() && belowBlockType != Material.LAVA &&
        belowBlockType != Material.MAGMA_BLOCK;
    boolean hasEnoughHeadroom = aboveBlockType == Material.AIR;

    if (!isAir && !(isNotDangerous && isOnSolidGround && hasEnoughHeadroom)) {
      int maxAdjustments = 5;
      int adjustments = 0;

      while (adjustments < maxAdjustments) {
        location.setY(location.getY() + 1);
        blockType = location.getBlock().getType();
        belowBlockType = location.clone().add(0, -1, 0).getBlock().getType();
        aboveBlockType = location.clone().add(0, 1, 0).getBlock().getType();

        isAir = blockType == Material.AIR;
        isNotDangerous =
            !blockType.isOccluding() && !blockType.isSolid() && blockType != Material.LAVA &&
                blockType != Material.MAGMA_BLOCK;
        isOnSolidGround = belowBlockType.isSolid() && belowBlockType != Material.LAVA &&
            belowBlockType != Material.MAGMA_BLOCK;
        hasEnoughHeadroom = aboveBlockType == Material.AIR;

        if (isAir || (isNotDangerous && isOnSolidGround && hasEnoughHeadroom)) {
          return true;
        }

        adjustments++;
      }
    }

    return isAir || (isNotDangerous && isOnSolidGround && hasEnoughHeadroom);
  }

  @Override
  public void draw() {
    Bukkit.getScheduler().cancelTask(taskId);

    for (Player p : this.getPlayers()) {
      p.sendMessage(Lang.getComponent("draw"));
    }

    Duel thisDuel = this;

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        for (Player p : thisDuel.getPlayers()) {
          p.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
        }
        arena.reset();
        arena.setOccupied(false);
        this.cancel();
      }
    }, 3000);

    this.setActive(false);

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelEndEvent(thisDuel, null, null));
      }
    }.runTask(AuroraDuels.INSTANCE);

    duels.remove(this);
  }
}