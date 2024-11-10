package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.DuelDrawEvent;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.aurorakits.modules.kits.Kit;
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
      DuelQueue duelQueue,
      Kit kit,
      Location respawnLocation,
      boolean ranked,
      int rounds) {
    super(Set.of(p1, p2), respawnLocation, duelQueue, kit, ranked);
    this.playerOne = p1;
    this.playerTwo = p2;
    this.rounds = rounds;
  }

  public Player getOpponent(Player player) {
    if (player.equals(playerOne)) {
      return playerTwo;
    } else if (player.equals(playerTwo)) {
      return playerOne;
    } else {
      return null;
    }
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

    StandardDuel thisDuel = this;

    CompletableFuture<Boolean> teleport1 = playerOne.teleportAsync(spawn1);
    CompletableFuture<Boolean> teleport2 = playerTwo.teleportAsync(spawn2);

    CompletableFuture.allOf(teleport1, teleport2)
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
                }
              }.runTaskTimer(AuroraDuels.getInstance(), 0L, 20L);

              playerOneBar[0] = createBossbar(playerTwo);
              playerTwoBar[0] = createBossbar(playerOne);

              spectatorBars.add(playerOneBar[0]);
              spectatorBars.add(playerTwoBar[0]);

              new BukkitRunnable() {
                @Override
                public void run() {
                  if (!thisDuel.isActive()) {
                    this.cancel();
                    return;
                  }

                  updateBossbar(playerOneBar[0], playerTwo);
                  updateBossbar(playerTwoBar[0], playerOne);
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

  private void updateBossbar(BossBar bossbar, Player opponent) {
    bossbar
        .name(
            opponent
                .displayName()
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.YELLOW)
                .appendSpace()
                .append(
                    Component.text(
                            (int) (opponent.getHealth() + opponent.getAbsorptionAmount()) + "❤")
                        .color(NamedTextColor.RED))
                .appendSpace()
                .append(Component.text(opponent.getPing() + "ms").color(NamedTextColor.AQUA)))
        .progress(
            (float) Math.min((opponent.getHealth() + opponent.getAbsorptionAmount()) / 20, 1.0f));
  }

  private BossBar createBossbar(Player opponent) {
    BossBar bossbar = BossBar.bossBar(
            opponent
                    .displayName()
                    .decorate(TextDecoration.BOLD)
                    .color(NamedTextColor.YELLOW)
                    .appendSpace()
                    .append(
                            Component.text(
                                            (int) (opponent.getHealth() + opponent.getAbsorptionAmount()) + "❤")
                                    .color(NamedTextColor.RED))
                    .appendSpace()
                    .append(Component.text(opponent.getPing() + "ms").color(NamedTextColor.AQUA)),
            1.0f,
            Color.RED,
            Overlay.PROGRESS);


    return bossbar;
  }

  public void endDuel(Collection<Player> winners, Collection<Player> losers) {
    Player winner = winners.iterator().next();
    Player loser = losers.iterator().next();

    if (rounds > 1 && loser.isOnline()) {
      if (wins.containsKey(winner.getUniqueId())) {

        wins.compute(winner.getUniqueId(), (k, currentWins) -> currentWins + 1);
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
        new DuelEndEvent(thisDuel, arena, winners, losers).callEvent();
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

    Duel thisDuel = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        new DuelDrawEvent(thisDuel, arena).callEvent();
      }
    }.runTask(AuroraDuels.getInstance());
  }
}
