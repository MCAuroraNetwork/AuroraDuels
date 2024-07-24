package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.DuelDrawEvent;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import io.papermc.paper.entity.LookAnchor;
import java.util.Collection;
import java.util.HashMap;
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
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class StandardDuel extends Duel implements Listener {

  private final Map<UUID, Integer> wins = new HashMap<>();
  private final BossBar[] playerOneBar = new BossBar[1];
  private final BossBar[] playerTwoBar = new BossBar[1];
  private final Player playerOne;
  private final Player playerTwo;

  public StandardDuel(Player p1, Player p2, Location respawnLocation, boolean ranked, int rounds) {
    super(Set.of(p1, p2), respawnLocation, ranked);
    this.playerOne = p1;
    this.playerTwo = p2;
    this.rounds = rounds;
  }

  public StandardDuel(
      Player p1,
      Player p2,
      Queue queue,
      Kit kit,
      Location respawnLocation,
      boolean ranked,
      int rounds) {
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

    StandardDuel thisDuel = this;

    CompletableFuture<Boolean> teleport1 = playerOne.teleportAsync(spawn1);
    CompletableFuture<Boolean> teleport2 = playerTwo.teleportAsync(spawn2);

    CompletableFuture.allOf(teleport1, teleport2)
        .thenRun(
            () -> {
              new BukkitRunnable() {
                int seconds = 4;

                @Override
                public void run() {
                  CompletableFuture<Boolean> teleport1 = playerOne.teleportAsync(spawn1);
                  teleport1.thenRun(() -> playerOne.lookAt(spawn2, LookAnchor.EYES));
                  CompletableFuture<Boolean> teleport2 = playerTwo.teleportAsync(spawn2);
                  teleport2.thenRun(() -> playerTwo.lookAt(spawn1, LookAnchor.EYES));

                  if ((seconds -= 1) <= 0) {
                    final Title title1 =
                        Title.title(
                            AuroraDuels.getInstance().getLang().getComponent("duel-started"),
                            Component.text("Opponent: " + playerTwo.getName()));
                    final Title title2 =
                        Title.title(
                            AuroraDuels.getInstance().getLang().getComponent("duel-started"),
                            Component.text("Opponent: " + playerOne.getName()));

                    if (rounds > 1) {
                      playerOne.sendActionBar(
                          AuroraDuels.getInstance()
                              .getLang()
                              .formatComponent(
                                  "rounds-remaining",
                                  currentRound,
                                  rounds,
                                  wins.get(playerOne.getUniqueId()),
                                  wins.get(playerTwo.getUniqueId())));
                      playerTwo.sendActionBar(
                          AuroraDuels.getInstance()
                              .getLang()
                              .formatComponent(
                                  "rounds-remaining",
                                  currentRound,
                                  rounds,
                                  wins.get(playerOne.getUniqueId()),
                                  wins.get(playerTwo.getUniqueId())));
                    }

                    playerOne.showTitle(title1);
                    playerTwo.showTitle(title2);

                    this.cancel();
                    return;
                  }

                  for (Player player : thisDuel.getPlayers()) {
                    player.showTitle(
                        Title.title(
                            MiniMessage.miniMessage().deserialize("<yellow><bold>" + seconds),
                            Component.text("")));
                  }
                }
              }.runTaskTimer(AuroraDuels.getInstance(), 0L, 20L);

              playerOneBar[0] =
                  BossBar.bossBar(
                      playerTwo
                          .displayName()
                          .decorate(TextDecoration.BOLD)
                          .color(NamedTextColor.YELLOW)
                          .appendSpace()
                          .append(
                              Component.text(
                                      (int)
                                              (playerTwo.getHealth()
                                                  + playerTwo.getAbsorptionAmount())
                                          + "❤")
                                  .color(NamedTextColor.RED))
                          .appendSpace()
                          .append(
                              Component.text(playerTwo.getPing() + "ms")
                                  .color(NamedTextColor.AQUA)),
                      1.0f,
                      Color.RED,
                      Overlay.PROGRESS);

              playerOne.showBossBar(playerOneBar[0]);

              playerTwoBar[0] =
                  BossBar.bossBar(
                      playerOne
                          .displayName()
                          .decorate(TextDecoration.BOLD)
                          .color(NamedTextColor.YELLOW)
                          .appendSpace()
                          .append(
                              Component.text(
                                      (int)
                                              (playerOne.getHealth()
                                                  + playerOne.getAbsorptionAmount())
                                          + "❤")
                                  .color(NamedTextColor.RED))
                          .appendSpace()
                          .append(
                              Component.text(playerOne.getPing() + "ms")
                                  .color(NamedTextColor.AQUA)),
                      1.0f,
                      Color.RED,
                      Overlay.PROGRESS);

              playerTwo.showBossBar(playerTwoBar[0]);

              spectatorBars.add(playerOneBar[0]);
              spectatorBars.add(playerTwoBar[0]);

              bossbarTask =
                  new BukkitRunnable() {
                    @Override
                    public void run() {
                      if (!thisDuel.isActive()) {
                        this.cancel();
                        return;
                      }

                      playerOneBar[0]
                          .name(
                              playerTwo
                                  .displayName()
                                  .decorate(TextDecoration.BOLD)
                                  .color(NamedTextColor.YELLOW)
                                  .appendSpace()
                                  .append(
                                      Component.text(
                                              (int)
                                                      (playerTwo.getHealth()
                                                          + playerTwo.getAbsorptionAmount())
                                                  + "❤")
                                          .color(NamedTextColor.RED))
                                  .appendSpace()
                                  .append(
                                      Component.text(playerTwo.getPing() + "ms")
                                          .color(NamedTextColor.AQUA)))
                          .progress(
                              (float)
                                  Math.min(
                                      (playerTwo.getHealth() + playerTwo.getAbsorptionAmount())
                                          / 20,
                                      1.0f));
                      playerTwoBar[0]
                          .name(
                              playerOne
                                  .displayName()
                                  .decorate(TextDecoration.BOLD)
                                  .color(NamedTextColor.YELLOW)
                                  .appendSpace()
                                  .append(
                                      Component.text(
                                              (int)
                                                      (playerOne.getHealth()
                                                          + playerOne.getAbsorptionAmount())
                                                  + "❤")
                                          .color(NamedTextColor.RED))
                                  .appendSpace()
                                  .append(
                                      Component.text(playerOne.getPing() + "ms")
                                          .color(NamedTextColor.AQUA)))
                          .progress(
                              (float)
                                  Math.min(
                                      (playerOne.getHealth() + playerOne.getAbsorptionAmount())
                                          / 20,
                                      1.0f));
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
    Player winner = winners.iterator().next();
    Player loser = winners.iterator().next();

    if (rounds > 1 && loser.isOnline()) {
      if (wins.containsKey(winner.getUniqueId())) {
        int currentWins = wins.get(winner.getUniqueId());

        wins.put(winner.getUniqueId(), currentWins + 1);
      } else {
        wins.put(winner.getUniqueId(), 1);
      }

      if (!(wins.get(winner.getUniqueId()) >= ((rounds - 1) / 2 + 1))) {
        loser.spigot().respawn();

        new BukkitRunnable() {
          @Override
          public void run() {
            if (kit != null) {
              kit.giveKit(playerTwo);
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
        }.runTaskLater(AuroraDuels.getInstance(), 1);

        arena.reset();

        this.startDuel(arena);
        return;
      }
    }

    winner.showTitle(
        Title.title(
            AuroraDuels.getInstance().getLang().getComponent("you-won"),
            Component.text("Opponent: " + loser.getName())));
    loser.showTitle(
        Title.title(
            AuroraDuels.getInstance().getLang().getComponent("you-lost"),
            Component.text("Opponent: " + winner.getName())));

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
        new DuelEndEvent(thisDuel, winners, losers).callEvent();
      }
    }.runTask(AuroraDuels.getInstance());
  }

  public void draw() {
    if (rounds > 1) {
      arena.reset();

      this.startDuel(arena);
      return;
    }

    for (Player player : this.getPlayers()) {
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("draw"));
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
    }.runTask(AuroraDuels.getInstance());
  }
}
