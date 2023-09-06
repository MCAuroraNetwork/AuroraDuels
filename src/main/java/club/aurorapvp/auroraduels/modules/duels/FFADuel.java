package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
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
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FFADuel extends Duel {

  private final Set<Player> deadPlayers = new HashSet<>();
  private final Map<Player, BossBar> bossbars = new HashMap<>();

  public FFADuel(Set<Player> players, Location respawnLocation, boolean ranked) {
    super(players, respawnLocation, ranked);
  }

  public FFADuel(Set<Player> players, Queue queue, Kit kit, Location respawnLocation,
      boolean ranked) {
    super(players, respawnLocation, queue, kit, ranked);
  }

  public Set<Player> getOpponents(Player player) {
    Set<Player> opponents = new HashSet<>(this.getPlayers());
    opponents.remove(player);

    return opponents;
  }

  public void setDead(Player player) {
    deadPlayers.add(player);
  }

  public boolean isDead(Player player) {
    return deadPlayers.contains(player);
  }

  public boolean onePlayerAlive() {
    int aliveCount = 0;

    for (Player player : this.getPlayers()) {
      if (!deadPlayers.contains(player)) {
        aliveCount++;
      }
    }

    return aliveCount == 1;
  }

  public Player getFirstAlive() {
    for (Player player : this.getPlayers()) {
      if (!deadPlayers.contains(player)) {
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

    final Map<Player, Location> safeLocations = new HashMap<>();

    Set<CompletableFuture<Boolean>> teleportFutures = new HashSet<>();
    for (Player player : thisDuel.getPlayers()) {
      safeLocations.put(player, LocationUtil.findSafeSpawnLocation(spawn1, spawn2, center));

      CompletableFuture<Boolean> teleportFuture = player.teleportAsync(safeLocations.get(player));
      teleportFutures.add(teleportFuture);
    }

    CompletableFuture.allOf(teleportFutures.toArray(new CompletableFuture[0])).thenRun(() -> {
      if (!teleportFutures.stream()
          .allMatch(CompletableFuture::join)) {
        thisDuel.draw();
        return;
      }

      new BukkitRunnable() {
        int seconds = 3;

        @Override
        public void run() {
          for (Player player : thisDuel.getPlayers()) {
            CompletableFuture<Boolean> teleport = player.teleportAsync(
                Objects.requireNonNull(safeLocations.get(player)));

            teleport.thenRun(() -> player.lookAt(center, LookAnchor.EYES));

            player.showTitle(
                Title.title(MiniMessage.miniMessage().deserialize("<yellow><bold>" + seconds),
                    Component.text("")));
          }

          if ((seconds -= 1) < 0) {
            StringBuilder sb;
            String names;
            Title title;

            for (Player player : thisDuel.getPlayers()) {
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
            }

            this.cancel();
          }
        }
      }.runTaskTimer(AuroraDuels.INSTANCE, 0L, 20L);

      for (Player player : this.getPlayers()) {
        BossBar bar = BossBar.bossBar(
            MiniMessage.miniMessage().deserialize("<red>Opponents"),
            1.0f,
            Color.RED,
            Overlay.PROGRESS);

        player.showBossBar(bar);

        bossbars.put(player, bar);
      }

      final double playersMaxHealth = (players.size() - 1) * 20;

      bossbarTask = new BukkitRunnable() {
        @Override
        public void run() {
          double playersHealth = 0;

          for (Player player : thisDuel.getPlayers()) {
            if (!thisDuel.isDead(player) && !thisDuel.isSpectator(player)) {
              playersHealth += player.getHealth() + player.getAbsorptionAmount();
            }
          }

          for (Player player : thisDuel.getPlayers()) {
            double playerHealth = thisDuel.isSpectator(player) ? 0 : player.getHealth() + player.getAbsorptionAmount();
            bossbars.get(player)
                .name(Component.text("Opponents").decorate(TextDecoration.BOLD)
                    .color(NamedTextColor.RED))
                .progress((float) Math.min(
                    ((playersHealth - playerHealth) / playersMaxHealth), 1.0f));
          }
        }
      };

      timeStarted = System.currentTimeMillis();

      new BukkitRunnable() {
        @Override
        public void run() {
          new DuelStartEvent(thisDuel).callEvent();
        }
      }.runTask(AuroraDuels.INSTANCE);
    });
  }

  public void endDuel(Player[] winners, Player[] losers) {
    for (Player player : this.getPlayers()) {
      player.hideBossBar(bossbars.get(player));
    }

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
        new DuelEndEvent(thisDuel, new Player[]{winner}, losers).callEvent();
      }
    }.runTask(AuroraDuels.INSTANCE);
  }

  @Override
  public void draw() {
    for (Player player : this.getPlayers()) {
      player.hideBossBar(bossbars.get(player));
    }

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
                arena.reset();
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
  }
}
