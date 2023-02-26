package club.aurorapvp.modules;

import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.events.custom.DuelEndEvent;
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

public class Duel implements Listener {
  public static Set<Duel> duels = new HashSet<>();
  private Long timeStarted;
  private final Player playerOne;
  private final Player playerTwo;
  private final boolean isPublic;
  private boolean started = false;
  private Arena arena;

  public Duel(Player p1, Player p2, boolean isPublic) {
    this.isPublic = isPublic;
    this.playerOne = p1;
    this.playerTwo = p2;
  }

  public Long getElapsed() {
    return System.currentTimeMillis() - timeStarted;
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

  public boolean isPublic() {
    return isPublic;
  }

  public Player getPlayerOne() {
    return playerOne;
  }

  public Player getPlayerTwo() {
    return playerTwo;
  }

  public void startDuel(Kit kit, Arena arena) {
    if (kit != null) {
      Kit.giveKit(kit, playerOne);
      Kit.giveKit(kit, playerTwo);
    }

    this.arena = arena;

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

    Duel thisDuel = this;
    new Timer().schedule(new TimerTask() {
                           @Override
                           public void run() {

                             playerOne.sendMessage(Lang.getComponent("duel-time-expired"));
                             playerTwo.sendMessage("duel-time-expired");
                             thisDuel.endDuel();
                             this.cancel();
                           }
                         }, Config.get().getInt("duels.requests.request-timeout")
    );

    Queue.getQueue(playerOne).exitDuelQueue(playerOne);
    arena.setOccupied(true);
    duels.add(this);
    timeStarted = System.currentTimeMillis();
    started = true;
  }

  public void endDuel() {
    playerOne.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    playerTwo.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());

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
      playerOne.sendMessage("draw");
      playerTwo.sendMessage("draw");
    }

    arena.setOccupied(false);
    arena.reset();
    duels.remove(this);
    Bukkit.getPluginManager().callEvent(new DuelEndEvent(this));
  }

  public boolean isStarted() {
    return started;
  }

  public static int inDuels() {
    return duels.size();
  }

  public static boolean inDuel(Player p) {
    for (Duel duel : duels) {
      if ((duel.getPlayerOne().equals(p) || duel.getPlayerTwo().equals(p)) && duel.isStarted()) {
        return true;
      }
    }
    return false;
  }

  public static Duel getDuel(Player p) {
    for (Duel duel : duels) {
      if (duel.getPlayerOne().equals(p) || duel.getPlayerTwo().equals(p)) {
        return duel;
      }

    }
    return null;
  }
}
