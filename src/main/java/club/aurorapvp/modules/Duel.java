package club.aurorapvp.modules;

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

public class Duel {
  public static List<Duel> duels = new ArrayList<>();
  @Getter
  private final Player p1;
  @Getter
  private final Player p2;
  private static Long timeStarted;
  @Getter
  private final boolean isPublic;
  private Arena arena;

  public Duel(Player p1, Player p2, boolean isPublic) {
    this.isPublic = isPublic;
    this.p1 = p1;
    this.p2 = p2;
    timeStarted = System.currentTimeMillis();
  }

  public static int inDuels() {
    return duels.size();
  }

  public Long getElapsed() {
    return System.currentTimeMillis() - timeStarted;
  }

  public Player getOpponent(Player p) {
    if (p == p1) {
      return p2;
    } else if (p == p2) {
      return p1;
    } else {
      return null;
    }
  }

  public static boolean inDuel(Player p) {
    for (Duel duel : duels) {
      if (duel.getP1().equals(p) || duel.getP2().equals(p)) {
        return true;
      }

    }
    return false;
  }

  public static Duel getDuel(Player p) {
    for (Duel duel : duels) {
      if (duel.getP1().equals(p) || duel.getP2().equals(p)) {
        return duel;
      }

    }
    return null;
  }

  public void startDuel(Kit kit, Arena arena) {
    if (kit != null) {
      kit.giveKit(p1);
      kit.giveKit(p2);
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

    p1.teleportAsync(spawn1);
    p2.teleportAsync(spawn2);

    final Title title1 =
        Title.title(Lang.getComponent("duel-started"), Component.text("Opponent: " + p2.getName()));
    final Title title2 =
        Title.title(Lang.getComponent("duel-started"), Component.text("Opponent: " + p1.getName()));

    p1.showTitle(title1);
    p2.showTitle(title2);

    Duel thisDuel = this;
    new Timer().schedule(new TimerTask() {
                           @Override
                           public void run() {

                               p1.sendMessage(Lang.getComponent("duel-time-expired"));
                               p2.sendMessage("duel-time-expired");
                               thisDuel.endDuel();
                               this.cancel();
                           }
                         }, Config.get().getInt("duels.requests.request-timeout")
    );

    Queue.getQueue(p1).exitDuelQueue(p1);
    arena.setOccupied(true);
    duels.add(this);
  }

  public void endDuel() {
    p1.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    p2.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());

    if (p1.isDead() || !p1.isOnline()) {
      p2.showTitle(
          Title.title(Lang.getComponent("you-won"), Component.text("Opponent: " + p1.getName())));
      p1.sendMessage(Lang.getComponent("you-lost"));
    } else if (p2.isDead() || !p2.isOnline()) {
      p1.showTitle(
          Title.title(Lang.getComponent("you-won"), Component.text("Opponent: " + p2.getName())));
      p2.sendMessage(Lang.getComponent("you-lost"));
    } else {
      p1.sendMessage("draw");
      p2.sendMessage("draw");
    }

    arena.setOccupied(false);
    arena.reset();
    duels.remove(this);
  }
}
