package club.aurorapvp.modules;

import club.aurorapvp.api.Kits;
import club.aurorapvp.api.events.DuelEndEvent;
import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Duel implements Listener {
  public static List<Duel> duels = new ArrayList<>();
  private final DuelEndEvent duelEndEvent = new DuelEndEvent(this);
  private Long timeStarted;
  @Getter
  private final Player playerOne;
  @Getter
  private final Player playerTwo;
  @Getter
  private final boolean isPublic;
  private Arena arena;

  public Duel(Player p1, Player p2, boolean isPublic) {
    this.isPublic = isPublic;
    this.playerOne = p1;
    this.playerTwo = p2;
  }

  public static int inDuels() {
    return duels.size();
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

  public static boolean inDuel(Player p) {
    for (Duel duel : duels) {
      if (duel.getPlayerOne().equals(p) || duel.getPlayerTwo().equals(p)) {
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

  public void startDuel(Kit kit, Arena arena) {
    if (kit != null) {
      Kits.giveKit(kit, playerOne);
      Kits.giveKit(kit, playerTwo);
    }

    this.arena = arena;

    Location spawn1 =
        new Location(arena.getWorld(),
            arena.getSpawn1().getX(), arena.getSpawn1().getY(),
            arena.getSpawn1().getZ());
    Location spawn2 =
        new Location(arena.getWorld(),
            arena.getSpawn2().getX(), arena.getSpawn2().getY(),
            arena.getSpawn2().getZ());

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
    Bukkit.getPluginManager().callEvent(duelEndEvent);
  }
}
