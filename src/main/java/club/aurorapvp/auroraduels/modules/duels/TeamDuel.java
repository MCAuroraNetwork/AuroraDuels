package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
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
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

public class TeamDuel extends Duel {

  public static Team team1;
  public static Team team2;
  private final Map<Player, Boolean> teamOneDead = new HashMap<>();
  private final Map<Player, Boolean> teamTwoDead = new HashMap<>();
  private final Player[] teamOneMembers;
  private final Player[] teamTwoMembers;
  private final BossBar[] teamOneBar = new BossBar[1];
  private final BossBar[] teamTwoBar = new BossBar[1];
  private final double team1MaxHealth;
  private final double team2MaxHealth;

  public TeamDuel(Player[] teamOneMembers, Player[] teamTwoMembers, Location respawnLocation,
      boolean ranked) {
    super(Stream.concat(Arrays.stream(teamOneMembers), Arrays.stream(teamTwoMembers))
            .collect(Collectors.toSet()),
        respawnLocation, ranked);

    this.teamOneMembers = teamOneMembers;
    this.teamTwoMembers = teamTwoMembers;

    team1MaxHealth = teamOneMembers.length * 20;
    team2MaxHealth = teamTwoMembers.length * 20;
  }

  public TeamDuel(Player[] teamOneMembers, Player[] teamTwoMembers, Queue queue, Kit kit,
      Location respawnLocation,
      boolean ranked) {
    super(Stream.concat(Arrays.stream(teamOneMembers), Arrays.stream(teamTwoMembers))
            .collect(Collectors.toSet()),
        respawnLocation, queue, kit, ranked);

    this.teamOneMembers = teamOneMembers;
    this.teamTwoMembers = teamTwoMembers;

    team1MaxHealth = teamOneMembers.length * 20;
    team2MaxHealth = teamTwoMembers.length * 20;
  }

  public static void init() {
    team1 = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("team1");

    if (team1 == null) {
      team1 = Bukkit.getScoreboardManager().getMainScoreboard()
          .registerNewTeam("team1");
    }

    team2 = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("team2");

    if (team2 == null) {
      team2 = Bukkit.getScoreboardManager().getMainScoreboard()
          .registerNewTeam("team2");
    }

    team1.color(NamedTextColor.BLUE);
    team2.color(NamedTextColor.RED);
  }

  public Player[] getTeamOneMembers() {
    return teamOneMembers;
  }

  public Player[] getTeamTwoMembers() {
    return teamTwoMembers;
  }

  public Player[] getOpponents(Player player) {
    for (Player teamMember : teamOneMembers) {
      if (teamMember == player) {
        return teamTwoMembers;
      }
    }
    return teamOneMembers;
  }

  public Player[] getTeammates(Player player) {
    for (Player teamMember : teamOneMembers) {
      if (teamMember == player) {
        return teamOneMembers;
      }
    }
    return teamTwoMembers;
  }

  public boolean isOnTeamOne(Player player) {
    for (Player teamOneMember : teamOneMembers) {
      if (teamOneMember == player) {
        return true;
      }
    }

    return false;
  }

  @SuppressWarnings("unused")
  public boolean isOnTeamTwo(Player player) {
    for (Player teamOneMember : teamOneMembers) {
      if (teamOneMember == player) {
        return true;
      }
    }

    return false;
  }

  public void setDead(Player player) {
    if (teamOneDead.containsKey(player)) {
      teamOneDead.put(player, true);
    } else {
      teamTwoDead.put(player, true);
    }
  }

  public boolean isTeamDead(Player player) {
    if (teamOneDead.containsKey(player)) {
      for (boolean dead : teamOneDead.values()) {
        if (!dead) {
          return false;
        }
      }
    } else {
      for (boolean dead : teamTwoDead.values()) {
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

    for (Player player : teamOneMembers) {
      teamOneDead.put(player, false);
      team1.addPlayer(player);
    }

    for (Player player : teamTwoMembers) {
      teamTwoDead.put(player, false);
      team2.addPlayer(player);
    }

    Duel thisDuel = this;

    Set<CompletableFuture<Boolean>> teleportFutures = new HashSet<>();

    for (Player player : teamOneMembers) {
      CompletableFuture<Boolean> teleportFuture = player.teleportAsync(spawn1);
      teleportFutures.add(teleportFuture);
    }

    for (Player player : teamTwoMembers) {
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
        int seconds = 3;

        @Override
        public void run() {
          for (Player player : teamOneMembers) {
            CompletableFuture<Boolean> teleport = player.teleportAsync(spawn1);

            teleport.thenRun(() -> player.lookAt(spawn2, LookAnchor.EYES));

            player.showTitle(
                Title.title(MiniMessage.miniMessage().deserialize("<yellow><bold>" + seconds),
                    Component.text("")));

          }

          if ((seconds -= 1) < 0) {
            for (Player player : teamOneMembers) {

              StringBuilder sb = new StringBuilder();
              for (int i = 0; i < teamTwoMembers.length; i++) {
                sb.append(teamTwoMembers[i].getName());
                if (i < teamTwoMembers.length - 1) {
                  sb.append(", ");
                }
              }
              String names = sb.toString();

              final Title title =
                  Title.title(
                      Lang.getComponent("duel-started"), Component.text("Opponents: " + names));

              player.showTitle(title);
            }

            this.cancel();
          }
        }
      }.runTaskTimer(AuroraDuels.INSTANCE, 0L, 20L);

      new BukkitRunnable() {
        int seconds = 3;

        @Override
        public void run() {
          for (Player player : teamTwoMembers) {
            CompletableFuture<Boolean> teleport = player.teleportAsync(spawn2);

            teleport.thenRun(() -> player.lookAt(spawn1, LookAnchor.EYES));

            player.showTitle(
                Title.title(MiniMessage.miniMessage().deserialize("<yellow><bold>" + seconds),
                    Component.text("")));
          }

          if ((seconds -= 1) < 0) {
            for (Player player : teamTwoMembers) {
              StringBuilder sb = new StringBuilder();
              for (int i = 0; i < teamOneMembers.length; i++) {
                sb.append(teamOneMembers[i].getName());
                if (i < teamOneMembers.length - 1) {
                  sb.append(", ");
                }
              }
              String names = sb.toString();

              final Title title =
                  Title.title(
                      Lang.getComponent("duel-started"), Component.text("Opponents: " + names));

              player.showTitle(title);
            }

            this.cancel();
          }
        }
      }.runTaskTimer(AuroraDuels.INSTANCE, 0L, 20L);

      teamOneBar[0] =
          BossBar.bossBar(
              Component.text("Blue Team").decorate(TextDecoration.BOLD).color(NamedTextColor.BLUE),
              1.0f,
              Color.BLUE,
              Overlay.PROGRESS);

      for (Player team1Member : teamOneMembers) {
        team1Member.showBossBar(teamOneBar[0]);
      }

      teamTwoBar[0] =
          BossBar.bossBar(
              Component.text("Red Team").decorate(TextDecoration.BOLD).color(NamedTextColor.RED),
              1.0f,
              Color.RED,
              Overlay.PROGRESS);

      for (Player team2Member : teamTwoMembers) {
        team2Member.showBossBar(teamTwoBar[0]);
      }

      spectatorBars.add(teamOneBar[0]);
      spectatorBars.add(teamTwoBar[0]);

      bossbarTask = new BukkitRunnable() {
        @Override
        public void run() {
          double team1Health = 0;

          for (Player team1Member : teamOneMembers) {
            if (teamOneDead.get(team1Member) || thisDuel.isSpectator(team1Member)) {
              continue;
            }

            team1Health =
                team1Health + team1Member.getHealth() + team1Member.getAbsorptionAmount();
          }

          teamTwoBar[0]
              .progress((float) Math.min(team1Health / team1MaxHealth, 1.0f));

          double team2Health = 0;

          for (Player team2Member : teamTwoMembers) {
            if (teamTwoDead.get(team2Member) || thisDuel.isSpectator(team2Member)) {
              continue;
            }

            team2Health =
                team2Health + team2Member.getHealth() + team2Member.getAbsorptionAmount();
          }

          teamOneBar[0]
              .progress((float) Math.min(team2Health / team2MaxHealth, 1.0f));
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

    for (Player player : teamOneMembers) {
      TeamDuel.team1.removePlayer(player);
      player.hideBossBar(teamOneBar[0]);
    }

    for (Player player : teamTwoMembers) {
      TeamDuel.team2.removePlayer(player);
      player.hideBossBar(teamTwoBar[0]);
    }

    Duel thisDuel = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelEndEvent(thisDuel, winners, losers).callEvent();
      }
    }.runTask(AuroraDuels.INSTANCE);
  }

  @Override
  public void draw() {
    for (Player player : this.getPlayers()) {
      player.sendMessage(Lang.getComponent("draw"));
    }

    for (Player player : teamOneMembers) {
      team1.removePlayer(player);
      player.hideBossBar(teamOneBar[0]);
    }

    for (Player player : teamTwoMembers) {
      team2.removePlayer(player);
      player.hideBossBar(teamTwoBar[0]);
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
