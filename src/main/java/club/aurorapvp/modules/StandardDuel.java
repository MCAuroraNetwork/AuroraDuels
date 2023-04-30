package club.aurorapvp.modules;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.events.custom.DuelEndEvent;
import club.aurorapvp.events.custom.DuelStartEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class StandardDuel extends Duel implements Listener {
  private static final Set<StandardDuel> STANDARD_DUELS = new HashSet<>();
  private final Player playerOne;
  private final Player playerTwo;

  public StandardDuel(Player p1, Player p2, boolean ranked) {
    super(Set.of(p1, p2), ranked);
    this.playerOne = p1;
    this.playerTwo = p2;
    STANDARD_DUELS.add(this);
  }

  public Player getOpponent(Player p) {
    if (p == playerOne) {
      return playerTwo;
    } else if (p == playerTwo) {
      return playerOne;
    } else {
      return null;
    }
  }

  @Override
  public void startDuel(Arena arena, Kit kit) {
    this.arena = arena;

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

    Location spawn1 =
        new Location(arena.getWorld(),
            arena.getFirstSpawn().getX(), arena.getFirstSpawn().getY(),
            arena.getFirstSpawn().getZ());
    Location spawn2 =
        new Location(arena.getWorld(),
            arena.getSecondSpawn().getX(), arena.getSecondSpawn().getY(),
            arena.getSecondSpawn().getZ());

    playerOne.teleportAsync(spawn1);
    playerTwo.teleportAsync(spawn2);

    final Title title1 =
        Title.title(Lang.getComponent("duel-started"),
            Component.text("Opponent: " + playerTwo.getName()));
    final Title title2 =
        Title.title(Lang.getComponent("duel-started"),
            Component.text("Opponent: " + playerOne.getName()));

    playerOne.showTitle(title1);
    playerTwo.showTitle(title2);

    StandardDuel thisDuel = this;

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        if (thisDuel.isActive()) {
          playerOne.sendMessage(Lang.getComponent("duel-time-expired"));
          playerTwo.sendMessage(Lang.getComponent("duel-time-expired"));
          thisDuel.endDuel();
        }
        this.cancel();
      }
    }, Config.get().getInt("duels.max-duration"));

    Queue.getQueue(playerOne).exitDuelQueue(playerOne);
    arena.setOccupied(true);
    timeStarted = System.currentTimeMillis();
    active = true;

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelStartEvent(thisDuel));
      }
    }.runTask(AuroraDuels.INSTANCE);
  }

  @Override
  public void endDuel() {
    if (playerOne.isDead() || !playerOne.isOnline()) {
      playerTwo.showTitle(
          Title.title(Lang.getComponent("you-won"),
              Component.text("Opponent: " + playerOne.getName())));
      playerOne.sendMessage(Lang.getComponent("you-lost"));
    } else if (playerTwo.isDead() || !playerTwo.isOnline()) {
      playerOne.showTitle(
          Title.title(Lang.getComponent("you-won"),
              Component.text("Opponent: " + playerTwo.getName())));
      playerTwo.sendMessage(Lang.getComponent("you-lost"));
    } else {
      playerOne.sendMessage(Lang.getComponent("draw"));
      playerTwo.sendMessage(Lang.getComponent("draw"));
    }

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        playerOne.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
        playerTwo.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
        arena.reset();
        arena.setOccupied(false);
        this.cancel();
      }
    }, 3000);

    this.active = false;
    StandardDuel thisDuel = this;

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelEndEvent(thisDuel));
      }
    }.runTask(AuroraDuels.INSTANCE);

    STANDARD_DUELS.remove(this);
  }
}