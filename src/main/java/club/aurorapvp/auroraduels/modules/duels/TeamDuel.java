package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.custom.DuelDrawEvent;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import io.papermc.paper.entity.LookAnchor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

public class TeamDuel extends Duel {

  public static final Team team1 = Bukkit.getScoreboardManager().getMainScoreboard()
      .registerNewTeam("team1");
  public static final Team team2 = Bukkit.getScoreboardManager().getMainScoreboard()
      .registerNewTeam("team2");
  private final Map<Player, Boolean> team1Dead = new HashMap<>();
  private final Map<Player, Boolean> team2Dead = new HashMap<>();
  private final Player[] team1Members;
  private final Player[] team2Members;
  private final BossBar[] team1Bar = new BossBar[1];
  private final BossBar[] team2Bar = new BossBar[1];

  public TeamDuel(Player[] team1Members, Player[] team2Members, Location respawnLocation,
      boolean ranked) {
    super(Stream.concat(Arrays.stream(team1Members), Arrays.stream(team2Members))
            .collect(Collectors.toSet()),
        respawnLocation, ranked);

    team1.color(NamedTextColor.RED);
    team2.color(NamedTextColor.BLUE);

    this.team1Members = team1Members;
    this.team2Members = team2Members;
  }

  public TeamDuel(Player[] team1Members, Player[] team2Members, Queue queue, Kit kit,
      Location respawnLocation,
      boolean ranked) {
    super(Stream.concat(Arrays.stream(team1Members), Arrays.stream(team2Members))
            .collect(Collectors.toSet()),
        respawnLocation, queue, kit, ranked);

    team1.color(NamedTextColor.RED);
    team2.color(NamedTextColor.BLUE);

    this.team1Members = team1Members;
    this.team2Members = team2Members;
  }

  public Player[] getTeam1Members() {
    return team1Members;
  }

  public Player[] getTeam2Members() {
    return team2Members;
  }

  @SuppressWarnings("unused")
  public Player[] getOpponents(Player player) {
    for (Player teamMember : team1Members) {
      if (teamMember == player) {
        return team2Members;
      }
    }
    return team1Members;
  }

  @SuppressWarnings("unused")
  public Player[] getTeammates(Player player) {
    for (Player teamMember : team1Members) {
      if (teamMember == player) {
        return team1Members;
      }
    }
    return team2Members;
  }

  public void setDead(Player player) {
    if (team1Dead.containsKey(player)) {
      team1Dead.put(player, true);
    } else {
      team2Dead.put(player, true);
    }
  }

  public boolean teamDead(Player player) {
    if (team1Dead.containsKey(player)) {
      for (boolean dead : team1Dead.values()) {
        if (!dead) {
          return false;
        }
      }
    } else {
      for (boolean dead : team2Dead.values()) {
        if (!dead) {
          return false;
        }
      }
    }
    return true;
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

    for (Player player : team1Members) {
      team1Dead.put(player, false);
      team1.addPlayer(player);
    }

    for (Player player : team2Members) {
      team2Dead.put(player, false);
      team2.addPlayer(player);
    }

    Duel thisDuel = this;

    Set<CompletableFuture<Boolean>> teleportFutures = new HashSet<>();

    for (Player player : team1Members) {
      CompletableFuture<Boolean> teleportFuture = player.teleportAsync(spawn1);
      teleportFutures.add(teleportFuture);
    }

    for (Player player : team2Members) {
      CompletableFuture<Boolean> teleportFuture = player.teleportAsync(spawn1);
      teleportFutures.add(teleportFuture);
    }

    CompletableFuture.allOf(teleportFutures.toArray(new CompletableFuture[0])).thenRun(() -> {
      if (!teleportFutures.stream()
          .allMatch(CompletableFuture::join)) {
        thisDuel.draw();
        return;
      }
      new BukkitRunnable() {
        int seconds = 4;

        @Override
        public void run() {
          for (Player player : team1Members) {
            player.teleportAsync(spawn1);
            player.lookAt(spawn2, LookAnchor.EYES);

            player.showTitle(Title.title(Component.text(seconds), Component.text("")));

            if ((seconds -= 1) == 0) {
              StringBuilder sb = new StringBuilder();
              for (int i = 0; i < team1Members.length; i++) {
                sb.append(team2Members[i].getName());
                if (i < team2Members.length - 1) {
                  sb.append(", ");
                }
              }
              String names = sb.toString();

              final Title title =
                  Title.title(
                      Lang.getComponent("duel-started"), Component.text("Opponents: " + names));

              player.showTitle(title);

              this.cancel();
            }
          }
        }
      }.runTaskTimer(AuroraDuels.INSTANCE, 0L, 20L);

      new BukkitRunnable() {
        int seconds = 4;

        @Override
        public void run() {
          for (Player player : team2Members) {
            player.teleportAsync(spawn2);
            player.lookAt(spawn1, LookAnchor.EYES);

            player.showTitle(Title.title(Component.text(seconds), Component.text("")));

            if ((seconds -= 1) == 0) {
              StringBuilder sb = new StringBuilder();
              for (int i = 0; i < team1Members.length; i++) {
                sb.append(team1Members[i].getName());
                if (i < team1Members.length - 1) {
                  sb.append(", ");
                }
              }
              String names = sb.toString();

              final Title title =
                  Title.title(
                      Lang.getComponent("duel-started"), Component.text("Opponents: " + names));

              player.showTitle(title);

              this.cancel();
            }
          }
        }
      }.runTaskTimer(AuroraDuels.INSTANCE, 0L, 20L);

      double team1MaxHealth = team1Members.length * 20;
      double team2MaxHealth = team2Members.length * 20;

      bossbarTask =
          new BukkitRunnable() {
            @Override
            public void run() {
              for (Player player : team1Members) {
                if (team1Bar[0] != null) {
                  player.hideBossBar(team1Bar[0]);
                }

                double team2Health = 0;

                for (Player team2Member : team2Members) {
                  if (team2Dead.get(team2Member) || thisDuel.isSpectator(team2Member)) {
                    continue;
                  }

                  team2Health =
                      team2Health + team2Member.getHealth() + team2Member.getAbsorptionAmount();
                }

                team1Bar[0] =
                    BossBar.bossBar(
                        Lang.getComponent("team2-bossbar"),
                        (float) Math.min(team2Health / team2MaxHealth, 1.0f),
                        Color.BLUE,
                        Overlay.PROGRESS);

                player.showBossBar(team1Bar[0]);
              }

              for (Player player : team2Members) {
                if (team2Bar[0] != null) {
                  player.hideBossBar(team2Bar[0]);
                }

                double team1Health = 0;

                for (Player team1Member : team1Members) {
                  if (team1Dead.get(team1Member)) {
                    continue;
                  }

                  team1Health =
                      team1Health + team1Member.getHealth() + team1Member.getAbsorptionAmount();
                }

                team2Bar[0] =
                    BossBar.bossBar(
                        Lang.getComponent("team1-bossbar"),
                        (float) Math.min(team1Health / team1MaxHealth, 1.0f),
                        Color.RED,
                        Overlay.PROGRESS);

                player.showBossBar(team2Bar[0]);
              }
            }
          };
      bossbarTask.runTaskTimer(AuroraDuels.INSTANCE, 0L, 1L);

      timeStarted = System.currentTimeMillis();
    });

    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelStartEvent(thisDuel).callEvent();
      }
    }.runTask(AuroraDuels.INSTANCE);

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
    bossbarTask.cancel();

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < winners.length; i++) {
      sb.append(winners[i].getName());
      if (i < winners.length - 1) {
        sb.append(", ");
      }
    }
    String winnerNames = sb.toString();

    sb = new StringBuilder();
    for (int i = 0; i < losers.length; i++) {
      sb.append(losers[i].getName());
      if (i < losers.length - 1) {
        sb.append(", ");
      }
    }
    String loserNames = sb.toString();

    for (Player player : winners) {
      player.showTitle(
          Title.title(Lang.getComponent("you-won"), Component.text("Opponents: " + loserNames)));
    }

    for (Player player : losers) {
      player.showTitle(
          Title.title(Lang.getComponent("you-lost"), Component.text("Opponents: " + winnerNames)));
    }

    for (Player player : team1Members) {
      player.hideBossBar(team1Bar[0]);
    }

    for (Player player : team2Members) {
      player.hideBossBar(team2Bar[0]);
    }

    Duel thisDuel = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelEndEvent(thisDuel, winners, losers).callEvent();
      }
    }.runTask(AuroraDuels.INSTANCE);

    duels.remove(this);
  }

  @Override
  public void draw() {
    bossbarTask.cancel();

    for (Player player : this.getPlayers()) {
      player.sendMessage(Lang.getComponent("draw"));
    }

    if (team1Bar[0] != null) {
      for (Player player : team1Members) {
        player.hideBossBar(team1Bar[0]);
      }
    }

    if (team2Bar[0] != null) {
      for (Player player : team2Members) {
        player.hideBossBar(team2Bar[0]);
      }
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

    duels.remove(this);
  }
}
