package club.aurorapvp.api;

import club.aurorapvp.modules.Duel;
import org.bukkit.entity.Player;

public class Duels {
  public static boolean inDuel(Player p) {
    return Duel.inDuel(p);
  }
}