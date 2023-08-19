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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamDuel extends Duel {
  private final Map<Player, Boolean> team1Dead = new HashMap<>();
  private final Map<Player, Boolean> team2Dead = new HashMap<>();
  private final Player[] team1;
  private final Player[] team2;

  public TeamDuel(Player[] team1, Player[] team2, Location respawnLocation, boolean ranked) {
    super(
        Stream.concat(Arrays.stream(team1), Arrays.stream(team2)).collect(Collectors.toSet()),
        respawnLocation,
        ranked);

    this.team1 = team1;
    this.team2 = team2;
  }

  public TeamDuel(
      Player[] team1,
      Player[] team2,
      Queue queue,
      Kit kit,
      Location respawnLocation,
      boolean ranked) {
    super(
        Stream.concat(Arrays.stream(team1), Arrays.stream(team2)).collect(Collectors.toSet()),
        respawnLocation,
        queue,
        kit,
        ranked);

    this.team1 = team1;
    this.team2 = team2;
  }

  public Player[] getTeam1() {
    return team1;
  }

  public Player[] getTeam2() {
    return team2;
  }

  @SuppressWarnings("unused")
  public Player[] getOpponents(Player player) {
    for (Player teamMember : team1) {
      if (teamMember == player) {
        return team2;
      }
    }
    return team1;
  }

  @SuppressWarnings("unused")
  public Player[] getTeammates(Player player) {
    for (Player teamMember : team1) {
      if (teamMember == player) {
        return team1;
      }
    }
    return team2;
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

    for (Player player : team1) {
      team1Dead.put(player, false);
    }

    for (Player player : team2) {
      team2Dead.put(player, false);
    }

    new BukkitRunnable() {
      int seconds = 3;

      @Override
      public void run() {
        for (Player player : team1) {
          player.teleportAsync(spawn1);
          player.lookAt(spawn2, LookAnchor.EYES);

          player.showTitle(Title.title(Component.text(seconds), Component.text("")));

          if ((seconds -= 1) == 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < team1.length; i++) {
              sb.append(team2[i].getName());
              if (i < team2.length - 1) {
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
    };

    new BukkitRunnable() {
      int seconds = 3;

      @Override
      public void run() {
        for (Player player : team2) {
          player.teleportAsync(spawn2);
          player.lookAt(spawn1, LookAnchor.EYES);

          player.showTitle(Title.title(Component.text(seconds), Component.text("")));

          if ((seconds -= 1) == 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < team1.length; i++) {
              sb.append(team1[i].getName());
              if (i < team1.length - 1) {
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
    };

    TeamDuel thisDuel = this;

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
