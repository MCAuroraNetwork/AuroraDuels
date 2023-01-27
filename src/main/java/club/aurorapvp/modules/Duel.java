package club.aurorapvp.modules;

import club.aurorapvp.configs.Lang;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Duel {
  public static List<Duel> duels = new ArrayList<>();
  private final Player p1;
  private final Player p2;
  private static Long timeStarted;
  private final boolean isPublic;
  private Arena arena;

  public Duel(Player p1, Player p2, boolean isPublic) {
    this.isPublic = isPublic;
    this.p1 = p1;
    this.p2 = p2;
    timeStarted = System.currentTimeMillis();
  }

  public Player getPlayerOne() {
    return p1;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public Player getPlayerTwo() {
    return p2;
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

  public static int inDuels() {
    return duels.size();
  }

  public void startDuel(Kit kit, Arena arena) {
    if (kit != null) {
      kit.giveKit(p1);
      kit.giveKit(p2);
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

    p1.teleportAsync(spawn1);
    p2.teleportAsync(spawn2);

    final Title title1 =
        Title.title(Lang.getComponent("duel-started"), Component.text("Opponent: " + p2.getName()));
    final Title title2 =
        Title.title(Lang.getComponent("duel-started"), Component.text("Opponent: " + p1.getName()));

    p1.showTitle(title1);
    p2.showTitle(title2);

    Queue.getQueue(p1).exitDuelQueue(p1);
    arena.setOccupied(true);
    duels.add(this);
  }

  public void endDuel() {
    p1.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    p2.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());

    if (p1.isDead() || !p1.isOnline()) {
      p2.sendMessage(Lang.getComponent("you-win"));
      p1.sendMessage(Lang.getComponent("you-lose"));
    } else {
      p1.sendMessage(Lang.getComponent("you-win"));
      p2.sendMessage(Lang.getComponent("you-lose"));
    }

    arena.setOccupied(false);
    arena.reset();
    duels.remove(this);
  }
}
