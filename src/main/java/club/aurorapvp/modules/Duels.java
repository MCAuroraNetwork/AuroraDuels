package club.aurorapvp.modules;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Lang;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Duels {
  public static List<Player> playersInDuel = new ArrayList<>();
  public static Map<Player, Player> pendingInvites = new HashMap<>();
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

  public static void acceptDuelRequest(Player p) {
    if (pendingInvites.containsKey(p)) {
      Player opponent = pendingInvites.get(p);

      p.sendMessage(Lang.getComponent("request-accepted"));
      opponent.sendMessage(Lang.getComponent("request-accepted"));
      pendingInvites.remove(p);

      Queue.enterDuelQueue(p, opponent);
    } else {
      p.sendMessage(Lang.getComponent("no-requests"));
    }
  }

  public static void denyDuelRequest(Player p) {
    if (pendingInvites.containsKey(p)) {
      Player opponent = pendingInvites.get(p);

      p.sendMessage(Lang.getComponent("request-denied"));
      opponent.sendMessage(Lang.getComponent("request-denied"));
      pendingInvites.remove(p);
    } else {
      p.sendMessage(Lang.getComponent("no-requests"));
    }
  }

  public static void sendDuelRequest(Player sender, Player receiver) {
    if (receiver != null && !playersInDuel.contains(receiver)) {
      pendingInvites.put(receiver, sender);

      sender.sendMessage(Lang.getComponent("sent-request"));
      receiver.sendMessage(AuroraDuels.COMPONENT_DESERIALIZER.deserialize(
          Lang.getString("received-request") + sender.getName() + Lang.getString("command-help")));

      TimerTask task = new TimerTask() {
        public void run() {
          if (pendingInvites.containsKey(receiver)) {
            pendingInvites.remove(receiver);
            receiver.sendMessage(Lang.getComponent("request-timed-out"));
            sender.sendMessage(Lang.getComponent("request-timed-out"));
          }
        }
      };
      new Timer().schedule(task, 60 * 1000);

    } else if (receiver == null) {
      sender.sendMessage(Lang.getComponent("unknown-opponent"));
    } else {
      sender.sendMessage(Lang.getComponent("player-in-duel"));
    }
  }

  public static void startDuel(Player p1, Player p2, Arena arena) {

    playersInDuel.add(p1);
    playersInDuel.add(p2);
    arena.setOccupied(false);

    Location spawn1 =
        new Location(arena.getWorld(),
            arena.getFirstSpawn().getX(), arena.getFirstSpawn().getY(),
            arena.getFirstSpawn().getZ());
    Location spawn2 =
        new Location(arena.getWorld(),
            arena.getSecondSpawn().getX(), arena.getSecondSpawn().getY(),
            arena.getSecondSpawn().getZ());

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
