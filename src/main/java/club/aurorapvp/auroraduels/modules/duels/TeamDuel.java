package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.aurorachat.modules.NameColor;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.DuelDrawEvent;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
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

public class TeamDuel extends Duel {

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
      DuelQueue duelQueue,
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
            duelQueue,
        kit,
        ranked);

    this.teamOneMembers = teamOneMembers;
    this.teamTwoMembers = teamTwoMembers;

    team1MaxHealth = teamOneMembers.size() * 20;
    team2MaxHealth = teamTwoMembers.size() * 20;
  }

  public Collection<Player> getTeamOneMembers() {
    return teamOneMembers;
  }

  public Collection<Player> getTeamTwoMembers() {
    return teamTwoMembers;
  }

  public Collection<Player> getOpponents(Player player) {
    for (Player teamMember : teamOneMembers) {
      if (teamMember.equals(player)) {
        return teamTwoMembers;
      }
    }
    return teamOneMembers;
  }

  public Collection<Player> getTeammates(Player player) {
    for (Player teamMember : teamOneMembers) {
      if (teamMember.equals(player)) {
        return teamOneMembers;
      }
    }
    return teamTwoMembers;
  }

  @SuppressWarnings("unused")
  public boolean isOnTeamOne(Player player) {
    for (Player teamOneMember : teamOneMembers) {
      if (teamOneMember.equals(player)) {
        return true;
      }
    }

    return false;
  }

  @SuppressWarnings("unused")
  public boolean isOnTeamTwo(Player player) {
    for (Player teamOneMember : teamOneMembers) {
      if (teamOneMember.equals(player)) {
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

    this.setActive(true);

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

      if (AuroraDuels.getInstance().isAuroraChatInstalled()) {
        NameColor.getNameColor(player).setColor(NamedTextColor.BLUE);
      }
    }

    for (Player player : teamTwoMembers) {
      teamTwoDead.put(player.getUniqueId(), false);

      if (AuroraDuels.getInstance().isAuroraChatInstalled()) {
        NameColor.getNameColor(player).setColor(NamedTextColor.RED);
      }
    }

    Duel thisDuel = this;

    Set<CompletableFuture<Boolean>> teleportFutures = new HashSet<>();

    for (Player player : teamOneMembers) {
      CompletableFuture<Boolean> teleportFuture = player.teleportAsync(spawn1);
      teleportFutures.add(teleportFuture);
    }

    for (Player player : teamTwoMembers) {
      CompletableFuture<Boolean> teleportFuture = player.teleportAsync(spawn2);
      teleportFutures.add(teleportFuture);
    }

    CompletableFuture.allOf(teleportFutures.toArray(new CompletableFuture[0]))
        .thenRun(
            () -> {
              this.setStarting(true);

              final int[] seconds = {3};

              new BukkitRunnable() {
                @Override
                public void run() {
                  for (Player player : thisDuel.getPlayers()) {
                    player.showTitle(
                        Title.title(
                            MiniMessage.miniMessage().deserialize("<yellow><bold>" + seconds[0]),
                            Component.text("")));
                  }
                  
                  if (!((seconds[0]--) <= 0)) {
                    return;
                  }

                  thisDuel.setStarting(false);

                  sendOpponents(teamOneMembers, teamTwoMembers);

                  sendOpponents(teamTwoMembers, teamOneMembers);

                  this.cancel();
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

              new BukkitRunnable() {
                @Override
                public void run() {
                  if (!thisDuel.isActive()) {
                    this.cancel();
                    return;
                  }
                  
                  updateTeamBossbar(teamOneMembers, teamOneDead, teamTwoBar[0], team1MaxHealth);

                  updateTeamBossbar(teamTwoMembers, teamTwoDead, teamOneBar[0], team2MaxHealth);
                }
              }.runTaskTimer(AuroraDuels.getInstance(), 0L, 1L);

              timeStarted = System.currentTimeMillis();

              new BukkitRunnable() {
                @Override
                public void run() {
                  new DuelStartEvent(thisDuel).callEvent();
                }
              }.runTask(AuroraDuels.getInstance());
            });
  }

  private void updateTeamBossbar(
      Collection<Player> opponentTeam,
      Map<UUID, Boolean> opponentsDead,
      BossBar bossbar,
      double opponentsMaxHealth) {
    double opponentTeamHealth = 0;

    for (Player opponent : opponentTeam) {
      if (opponentsDead.get(opponent.getUniqueId()) || this.isSpectating(opponent)) {
        continue;
      }

      opponentTeamHealth =
          opponentTeamHealth + opponent.getHealth() + opponent.getAbsorptionAmount();
    }

    bossbar.progress((float) Math.min(opponentTeamHealth / opponentsMaxHealth, 1.0f));
  }

  private void sendOpponents(Collection<Player> team, Collection<Player> opponents) {
    StringBuilder sb = new StringBuilder();
    for (Player teamTwoMember : opponents) {
      sb.append(teamTwoMember.getName()).append(", ");
    }

    sb.delete(sb.lastIndexOf(", "), sb.length() - 1);

    for (Player player : team) {
      final Title title =
          Title.title(
              AuroraDuels.getInstance().getLang().getComponent("duel-started"),
              Component.text("Opponents: " + sb));

      player.showTitle(title);
    }
  }

  public void hideBossbar(Collection<Player> team, BossBar bossbar) {
    for (Player player : team) {
      player.hideBossBar(bossbar);

      if (AuroraDuels.getInstance().isAuroraChatInstalled()) {
        NameColor.getNameColor(player).reload();
      }
    }
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

    hideBossbar(teamOneMembers, teamOneBar[0]);

    hideBossbar(teamTwoMembers, teamTwoBar[0]);

    Duel thisDuel = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelEndEvent(thisDuel, arena, winners, losers).callEvent();
      }
    }.runTask(AuroraDuels.getInstance());
  }

  @Override
  public void draw() {
    for (Player player : this.getPlayers()) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("draw"));
    }

    for (Player player : teamOneMembers) {
      player.hideBossBar(teamOneBar[0]);

      if (AuroraDuels.getInstance().isAuroraChatInstalled()) {
        NameColor.getNameColor(player).reload();
      }
    }

    for (Player player : teamTwoMembers) {
      player.hideBossBar(teamTwoBar[0]);

      if (AuroraDuels.getInstance().isAuroraChatInstalled()) {
        NameColor.getNameColor(player).reload();
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
    
    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelDrawEvent(thisDuel, arena).callEvent();
      }
    }.runTask(AuroraDuels.getInstance());
  }
}
