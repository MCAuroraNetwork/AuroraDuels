package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.custom.DuelDrawEvent;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.auroraduels.util.LocationUtil;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import io.papermc.paper.entity.LookAnchor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FFADuel extends Duel {
  private final Map<Player, Boolean> deadPlayers = new HashMap<>();

  public FFADuel(Set<Player> players, Location respawnLocation, boolean ranked) {
    super(players, respawnLocation, ranked);
  }

  public FFADuel(
      Set<Player> players, Queue queue, Kit kit, Location respawnLocation, boolean ranked) {
    super(players, respawnLocation, queue, kit, ranked);
  }

  @SuppressWarnings("unused")
  public Player[] getOpponents(Player player) {
    Set<Player> opponents = new HashSet<>(this.getPlayers());
    opponents.remove(player);

    return opponents.toArray(new Player[0]);
  }

  public void setDead(Player player) {
    deadPlayers.put(player, true);
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
    for (Player player : deadPlayers.keySet()) {
      if (!deadPlayers.get(player)) {
        return player;
      }
    }
    return null;
  }

  public void startDuel(Arena arena) {
    this.arena = arena;

    Location spawn1 =
        new Location(
            arena.getWorld(),
            arena.getFirstSpawn().getX(),
            arena.getFirstSpawn().getY(),
            arena.getFirstSpawn().getZ());
    Location spawn2 =
        new Location(
            arena.getWorld(),
            arena.getSecondSpawn().getX(),
            arena.getSecondSpawn().getY(),
            arena.getSecondSpawn().getZ());

    double x = (spawn1.getX() + spawn2.getX()) / 2;
    double y = (spawn1.getY() + spawn2.getY()) / 2;
    double z = (spawn1.getZ() + spawn2.getZ()) / 2;

    Location center = new Location(spawn1.getWorld(), x, y, z);

    FFADuel thisDuel = this;
    new BukkitRunnable() {
      int seconds = 3;

      @Override
      public void run() {
        for (Player player : thisDuel.getPlayers()) {
          player.teleportAsync(
              Objects.requireNonNull(LocationUtil.findSafeSpawnLocation(spawn1, spawn2, center)));
          player.lookAt(center, LookAnchor.EYES);

          player.showTitle(Title.title(Component.text(seconds), Component.text("")));

          if ((seconds -= 1) == 0) {
            StringBuilder sb;
            String names;
            Title title;

            sb = new StringBuilder();

            for (Player opponent : thisDuel.getPlayers()) {
              if (player != opponent) {
                sb.append(opponent.getName());
                sb.append(", ");
              }
            }

            names = sb.toString();
            if (names.length() > 2) {
              names = names.substring(0, names.length() - 2);
            }

            title =
                Title.title(
                    Lang.getComponent("duel-started"), Component.text("Opponents: " + names));

            player.showTitle(title);

            this.cancel();
          }
        }
      }
    }.runTaskTimer(AuroraDuels.INSTANCE, 1L, 1L);

    checkBoundsId =
        Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(AuroraDuels.INSTANCE, checkBounds, 200L, 200L);

    Set<Player> players = this.getPlayers();

    for (Player player : players) {
      deadPlayers.put(player, false);
    }

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                if (thisDuel.isActive()) {
                  for (Player player : thisDuel.getPlayers()) {
                    player.sendMessage(Lang.getComponent("duel-time-expired"));
                    player.sendMessage(Lang.getComponent("duel-time-expired"));
                  }
                  thisDuel.draw();
                }
                this.cancel();
              }
            },
            Config.get().getInt("duels.max-duration"));

    timeStarted = System.currentTimeMillis();

    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelStartEvent(thisDuel).callEvent();
      }
    }.runTask(AuroraDuels.INSTANCE);
  }

  public void endDuel(Player[] winners, Player[] losers) {
    Bukkit.getScheduler().cancelTask(checkBoundsId);

    Player winner = winners[0];

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

    for (Player player : losers) {
      player.showTitle(
          Title.title(Lang.getComponent("you-lost"), Component.text("Winner: " + winnerName)));
    }

    Duel thisDuel = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelEndEvent(thisDuel, new Player[] {winner}, losers).callEvent();
      }
    }.runTask(AuroraDuels.INSTANCE);

    duels.remove(this);
  }

  @Override
  public void draw() {
    Bukkit.getScheduler().cancelTask(checkBoundsId);

    for (Player player : this.getPlayers()) {
      player.sendMessage(Lang.getComponent("draw"));
    }

    Duel thisDuel = this;

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                for (Player player : thisDuel.getPlayers()) {
                  player.teleportAsync(thisDuel.getRespawnLocation());
                }
                arena.resetAsync();
                arena.setOccupied(false);
                this.cancel();
              }
            },
            3000);

    this.setActive(false);

    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelDrawEvent(thisDuel).callEvent();
      }
    }.runTask(AuroraDuels.INSTANCE);

    duels.remove(this);
  }
}
