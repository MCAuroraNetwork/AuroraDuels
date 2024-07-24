package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.DuelDrawEvent;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import io.papermc.paper.entity.LookAnchor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
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
  private final Map<UUID, Boolean> teamOneDead = new HashMap<>();
  private final Map<UUID, Boolean> teamTwoDead = new HashMap<>();
  private final Collection<Player> teamOneMembers;
  private final Collection<Player> teamTwoMembers;
  private final BossBar[] teamOneBar = new BossBar[1];
  private final BossBar[] teamTwoBar = new BossBar[1];
  private final double team1MaxHealth;
  private final double team2MaxHealth;

  public TeamDuel(
      Collection<Player> teamOneMembers,
      Collection<Player> teamTwoMembers,
      Location respawnLocation,
      boolean ranked) {
    super(
        new HashSet<>() {
          {
            addAll(teamOneMembers);
            addAll(teamTwoMembers);
          }
        },
        respawnLocation,
        ranked);

    this.teamOneMembers = teamOneMembers;
    this.teamTwoMembers = teamTwoMembers;

    team1MaxHealth = teamOneMembers.size() * 20;
    team2MaxHealth = teamTwoMembers.size() * 20;
  }

  public TeamDuel(
      Collection<Player> teamOneMembers,
      Collection<Player> teamTwoMembers,
      Queue queue,
      Kit kit,
      Location respawnLocation,
      boolean ranked) {
    super(
        new HashSet<>() {
          {
            addAll(teamOneMembers);
            addAll(teamTwoMembers);
          }
        },
        respawnLocation,
        queue,
        kit,
        ranked);

    this.teamOneMembers = teamOneMembers;
    this.teamTwoMembers = teamTwoMembers;

    team1MaxHealth = teamOneMembers.size() * 20;
    team2MaxHealth = teamTwoMembers.size() * 20;
  }

  public static void init() {
    team1 = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("team1");

    if (team1 == null) {
      team1 = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("team1");
    }

    team2 = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("team2");

    if (team2 == null) {
      team2 = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("team2");
    }

    team1.color(NamedTextColor.BLUE);
    team2.color(NamedTextColor.RED);
  }

  public Collection<Player> getTeamOneMembers() {
    return teamOneMembers;
  }

  public Collection<Player> getTeamTwoMembers() {
    return teamTwoMembers;
  }

  public Collection<Player> getOpponents(Player player) {
    for (Player teamMember : teamOneMembers) {
      if (teamMember == player) {
        return teamTwoMembers;
      }
    }
    return teamOneMembers;
  }

  public Collection<Player> getTeammates(Player player) {
    for (Player teamMember : teamOneMembers) {
      if (teamMember == player) {
        return teamOneMembers;
      }
    }
    return teamTwoMembers;
  }

  @SuppressWarnings("unused")
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
    if (teamOneDead.containsKey(player.getUniqueId())) {
      teamOneDead.put(player.getUniqueId(), true);
    } else {
      teamTwoDead.put(player.getUniqueId(), true);
    }
  }

  public boolean isTeamDead(Player player) {
    if (teamOneDead.containsKey(player.getUniqueId())) {
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
            arena.getFirstSpawn().x(),
            arena.getFirstSpawn().y(),
            arena.getFirstSpawn().z());
    Location spawn2 =
        new Location(
            arena.getWorld(),
            arena.getSecondSpawn().x(),
            arena.getSecondSpawn().y(),
            arena.getSecondSpawn().z());

    for (Player player : teamOneMembers) {
      teamOneDead.put(player.getUniqueId(), false);
      team1.addPlayer(player);
    }

    for (Player player : teamTwoMembers) {
      teamTwoDead.put(player.getUniqueId(), false);
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

    CompletableFuture.allOf(teleportFutures.toArray(new CompletableFuture[0]))
        .thenRun(
            () -> {
              new BukkitRunnable() {
                int seconds = 3;

                @Override
                public void run() {
                  for (Player player : teamOneMembers) {
                    CompletableFuture<Boolean> teleport = player.teleportAsync(spawn1);

                    teleport.thenRun(() -> player.lookAt(spawn2, LookAnchor.EYES));

                    player.showTitle(
                        Title.title(
                            MiniMessage.miniMessage().deserialize("<yellow><bold>" + seconds),
                            Component.text("")));
                  }

                  if ((seconds -= 1) < 0) {
                    for (Player player : teamOneMembers) {
                      StringBuilder sb = new StringBuilder();
                      for (Player teamTwoMember : teamTwoMembers) {
                        sb.append(teamTwoMember.getName()).append(", ");
                      }

                      sb.delete(sb.lastIndexOf(", "), sb.length() - 1);

                      final Title title =
                          Title.title(
                              AuroraDuels.getInstance().getLang().getComponent("duel-started"),
                              Component.text("Opponents: " + sb));

                      player.showTitle(title);
                    }

                    this.cancel();
                  }
                }
              }.runTaskTimer(AuroraDuels.getInstance(), 0L, 20L);

              new BukkitRunnable() {
                int seconds = 3;

                @Override
                public void run() {
                  for (Player player : teamTwoMembers) {
                    CompletableFuture<Boolean> teleport = player.teleportAsync(spawn2);

                    teleport.thenRun(() -> player.lookAt(spawn1, LookAnchor.EYES));

                    player.showTitle(
                        Title.title(
                            MiniMessage.miniMessage().deserialize("<yellow><bold>" + seconds),
                            Component.text("")));
                  }

                  if ((seconds -= 1) < 0) {
                    for (Player player : teamTwoMembers) {
                      StringBuilder sb = new StringBuilder();
                      for (Player teamOneMember : teamOneMembers) {
                        sb.append(teamOneMember.getName()).append(", ");
                      }

                      sb.delete(sb.lastIndexOf(", "), sb.length() - 1);

                      final Title title =
                          Title.title(
                              AuroraDuels.getInstance().getLang().getComponent("duel-started"),
                              Component.text("Opponents: " + sb));

                      player.showTitle(title);
                    }

                    this.cancel();
                  }
                }
              }.runTaskTimer(AuroraDuels.getInstance(), 0L, 20L);

              teamOneBar[0] =
                  BossBar.bossBar(
                      Component.text("Red Team")
                          .decorate(TextDecoration.BOLD)
                          .color(NamedTextColor.RED),
                      1.0f,
                      Color.RED,
                      Overlay.PROGRESS);

              for (Player team1Member : teamOneMembers) {
                team1Member.showBossBar(teamOneBar[0]);
              }

              teamTwoBar[0] =
                  BossBar.bossBar(
                      Component.text("Blue Team")
                          .decorate(TextDecoration.BOLD)
                          .color(NamedTextColor.BLUE),
                      1.0f,
                      Color.BLUE,
                      Overlay.PROGRESS);

              for (Player team2Member : teamTwoMembers) {
                team2Member.showBossBar(teamTwoBar[0]);
              }

              spectatorBars.add(teamOneBar[0]);
              spectatorBars.add(teamTwoBar[0]);

              bossbarTask =
                  new BukkitRunnable() {
                    @Override
                    public void run() {
                      if (!thisDuel.isActive()) {
                        this.cancel();
                        return;
                      }

                      double team1Health = 0;

                      for (Player team1Member : teamOneMembers) {
                        if (teamOneDead.get(team1Member.getUniqueId())
                            || thisDuel.isSpectator(team1Member)) {
                          continue;
                        }

                        team1Health =
                            team1Health
                                + team1Member.getHealth()
                                + team1Member.getAbsorptionAmount();
                      }

                      teamTwoBar[0].progress((float) Math.min(team1Health / team1MaxHealth, 1.0f));

                      double team2Health = 0;

                      for (Player team2Member : teamTwoMembers) {
                        if (teamTwoDead.get(team2Member.getUniqueId())
                            || thisDuel.isSpectator(team2Member)) {
                          continue;
                        }

                        team2Health =
                            team2Health
                                + team2Member.getHealth()
                                + team2Member.getAbsorptionAmount();
                      }

                      teamOneBar[0].progress((float) Math.min(team2Health / team2MaxHealth, 1.0f));
                    }
                  };

              timeStarted = System.currentTimeMillis();

              new BukkitRunnable() {
                @Override
                public void run() {
                  new DuelStartEvent(thisDuel).callEvent();
                }
              }.runTask(AuroraDuels.getInstance());
            });
  }

  public void endDuel(Collection<Player> winners, Collection<Player> losers) {
    StringBuilder sb = new StringBuilder();
    for (Player winner : winners) {
      sb.append(winner.getName()).append(", ");
    }

    sb.delete(sb.lastIndexOf(", "), sb.length() - 1);

    String winnerNames = sb.toString();

    sb = new StringBuilder();
    for (Player loser : losers) {
      sb.append(loser.getName()).append(", ");
    }

    sb.delete(sb.lastIndexOf(", "), sb.length() - 1);

    String loserNames = sb.toString();

    for (Player player : winners) {
      player.showTitle(
          Title.title(
              AuroraDuels.getInstance().getLang().getComponent("you-won"),
              Component.text("Opponents: " + loserNames)));
    }

    for (Player player : losers) {
      player.showTitle(
          Title.title(
              AuroraDuels.getInstance().getLang().getComponent("you-lost"),
              Component.text("Opponents: " + winnerNames)));
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
    }.runTask(AuroraDuels.getInstance());
  }

  @Override
  public void draw() {
    for (Player player : this.getPlayers()) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("draw"));
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
    }.runTask(AuroraDuels.getInstance());
  }
}
