package club.aurorapvp.modules;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Queue {

    public static LinkedHashMap<Player, Player> duelQueue = new LinkedHashMap<>();

    public static void enterDuelQueue(Player p, Player opponent) {
        if (opponent != null) {
            duelQueue.put(p, opponent);
        } else {

        }
    }
}
