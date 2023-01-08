package club.aurorapvp.modules;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Lang;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Duels {
  public static List<Player> playersInDuel = new ArrayList<>();
  public static List<Arena> arenas = new ArrayList<>();

  public static void setup() {
    File dir = new File(AuroraDuels.DATAFOLDER, "/arenas/");

    for (File file : dir.listFiles()) {
      Arena arena = new Arena(file.getName());
      if (arena.exists()) {
        arenas.add(arena);
      }
    }
  }

  public void startDuel(Player p1, Player p2, Arena arena) {
    playersInDuel.add(p1);
    playersInDuel.add(p2);
    arena.setAvailability(false);

    Location spawn1 =
        new Location(arena.getWorld(),
            arena.getSpawn(1).getX(), arena.getSpawn(1).getY(), arena.getSpawn(1).getZ());
    Location spawn2 =
        new Location(arena.getWorld(),
            arena.getSpawn(2).getX(), arena.getSpawn(2).getY(), arena.getSpawn(2).getZ());

    p1.teleport(spawn1);
    p2.teleport(spawn2);

    final Title title1 =
        Title.title(Lang.getComponent("duel-started"), Component.text("Opponent: " + p2.getName()));
    final Title title2 =
        Title.title(Lang.getComponent("duel-started"), Component.text("Opponent: " + p1.getName()));

    p1.showTitle(title1);
    p2.showTitle(title2);
  }
}
