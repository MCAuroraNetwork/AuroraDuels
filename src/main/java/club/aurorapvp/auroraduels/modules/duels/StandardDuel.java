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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class StandardDuel extends Duel implements Listener {

  private final Map<Player, Integer> wins = new HashMap<>();
  private final BossBar[] playerOneBar = new BossBar[1];
  private final BossBar[] playerTwoBar = new BossBar[1];
  private BukkitTask bossbarTask;
  private final Player playerOne;
  private final Player playerTwo;

  public StandardDuel(Player p1, Player p2, Location respawnLocation, boolean ranked, int rounds) {
    super(Set.of(p1, p2), respawnLocation, ranked);
    this.playerOne = p1;
    this.playerTwo = p2;
    this.rounds = rounds;
  }

  public StandardDuel(Player p1, Player p2, Queue queue, Kit kit, Location respawnLocation,
      boolean ranked, int rounds) {
    super(Set.of(p1, p2), respawnLocation, queue, kit, ranked);
    this.playerOne = p1;
    this.playerTwo = p2;
    this.rounds = rounds;
  }

  public Player getOpponent(Player player) {
    if (player == playerOne) {
      return playerTwo;
    } else if (player == playerTwo) {
      return playerOne;
    } else {
      return null;
    }
  }

  public void startDuel(Arena arena) {
    this.arena = arena;

    if (rounds % 2 == 0) {
      for (Player player : this.players) {
        player.sendMessage(Lang.getComponent("even-rounds"));
      }
    }

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

    StandardDuel thisDuel = this;

    new BukkitRunnable() {
      int seconds = 4;

      @Override
      public void run() {
        playerOne.teleportAsync(spawn1);
        playerOne.lookAt(spawn2, LookAnchor.EYES);
        playerTwo.teleportAsync(spawn2);
        playerTwo.lookAt(spawn1, LookAnchor.EYES);

        if ((seconds -= 1) == 0) {
          final Title title1 =
              Title.title(
                  Lang.getComponent("duel-started"),
                  Component.text("Opponent: " + playerTwo.getName()));
          final Title title2 =
              Title.title(
                  Lang.getComponent("duel-started"),
                  Component.text("Opponent: " + playerOne.getName()));

          if (rounds > 1) {
            playerOne.sendActionBar(
                Lang.formatComponent("rounds-remaining", currentRound, rounds));
            playerTwo.sendActionBar(
                Lang.formatComponent("rounds-remaining", currentRound, rounds));
          }

          playerOne.showTitle(title1);
          playerTwo.showTitle(title2);

          this.cancel();
          return;
        }

        for (Player player : thisDuel.getPlayers()) {
          player.showTitle(
              Title.title(MiniMessage.miniMessage().deserialize("<yellow><bold>" + seconds),
                  Component.text("")));
        }
      }
    }.runTaskTimer(AuroraDuels.INSTANCE, 40L, 20L);

    checkBounds.runTaskTimer(AuroraDuels.INSTANCE, 200L, 200L);

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                if (thisDuel.isActive()) {
                  playerOne.sendMessage(Lang.getComponent("duel-time-expired"));
                  playerTwo.sendMessage(Lang.getComponent("duel-time-expired"));
                  thisDuel.draw();
                }
                this.cancel();
              }
            },
            Config.get().getInt("duels.max-duration"));

    bossbarTask =
        new BukkitRunnable() {
          @Override
          public void run() {
            if (playerOneBar[0] != null) {
              playerOne.hideBossBar(playerOneBar[0]);
            }

            try {
              playerOneBar[0] =
                  BossBar.bossBar(
                      Lang.formatComponent(
                          "opponent-bossbar",
                          playerTwo.getName(),
                          (int) (playerTwo.getHealth() + playerTwo.getAbsorptionAmount()),
                          (int) playerTwo.getLocation().distance(playerOne.getLocation()),
                          playerTwo.getPing()),
                      (float) Math.min(playerTwo.getHealth() + playerTwo.getAbsorptionAmount() / 20, 1.0f),
                      BossBar.Color.RED,
                      BossBar.Overlay.PROGRESS);
            } catch (IllegalArgumentException e) {
              this.cancel();
            }

            playerOne.showBossBar(playerOneBar[0]);

            if (playerTwoBar[0] != null) {
              playerTwo.hideBossBar(playerTwoBar[0]);
            }

            try {
              playerTwoBar[0] =
                  BossBar.bossBar(
                      Lang.formatComponent(
                          "opponent-bossbar",
                          playerOne.getName(),
                          (int) (playerOne.getHealth() + playerTwo.getAbsorptionAmount()),
                          (int) playerOne.getLocation().distance(playerTwo.getLocation()),
                          playerOne.getPing()),
                      (float) Math.min(playerOne.getHealth() + playerOne.getAbsorptionAmount() / 20, 1.0f),
                      BossBar.Color.RED,
                      BossBar.Overlay.PROGRESS);
            } catch (IllegalArgumentException e) {
              this.cancel();
            }

            playerTwo.showBossBar(playerTwoBar[0]);
          }
        }.runTaskTimer(AuroraDuels.INSTANCE, 0L, 1L);

    timeStarted = System.currentTimeMillis();

    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelStartEvent(thisDuel).callEvent();
      }
    }.runTask(AuroraDuels.INSTANCE);
  }

  public void endDuel(Player[] winner, Player[] loser) {
    if (rounds > 1 && loser[0].isOnline()) {
      if (wins.containsKey(winner[0])) {
        int currentWins = wins.get(winner[0]);

        wins.put(winner[0], currentWins + 1);
      } else {
        wins.put(winner[0], 1);
      }

      if (!(wins.get(winner[0]) >= (rounds / 2 + 1))) {
        loser[0].spigot().respawn();

        new BukkitRunnable() {
          @Override
          public void run() {
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
          }
        }.runTaskLater(AuroraDuels.INSTANCE, 1);

        arena.reset();

        this.startDuel(arena);
        return;
      }
    }

    checkBounds.cancel();
    bossbarTask.cancel();

    winner[0].showTitle(
        Title.title(
            Lang.getComponent("you-won"), Component.text("Opponent: " + loser[0].getName())));
    loser[0].showTitle(
        Title.title(
            Lang.getComponent("you-lost"), Component.text("Opponent: " + winner[0].getName())));

    if (playerOneBar[0] != null) {
      playerOne.hideBossBar(playerOneBar[0]);
    }

    if (playerTwoBar[0] != null) {
      playerTwo.hideBossBar(playerTwoBar[0]);
    }

    Duel thisDuel = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelEndEvent(thisDuel, winner, loser).callEvent();
      }
    }.runTask(AuroraDuels.INSTANCE);

    duels.remove(this);
  }

  public void draw() {
    if (rounds > 1) {
      arena.reset();

      this.startDuel(arena);
      return;
    }

    checkBounds.cancel();
    bossbarTask.cancel();

    for (Player player : this.getPlayers()) {
      player.sendMessage(Lang.getComponent("draw"));
    }

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                playerOne.teleportAsync(respawnLocation);
                playerTwo.teleportAsync(respawnLocation);
                arena.reset();
                arena.setOccupied(false);
                this.cancel();
              }
            },
            3000);

    if (playerOneBar[0] != null) {
      playerOne.hideBossBar(playerOneBar[0]);
    }

    if (playerTwoBar[0] != null) {
      playerTwo.hideBossBar(playerTwoBar[0]);
    }

    this.setActive(false);
    Duel thisDuel = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelDrawEvent(thisDuel).callEvent();
      }
    }.runTask(AuroraDuels.INSTANCE);

    duels.remove(this);
  }
}
