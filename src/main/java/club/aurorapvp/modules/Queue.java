package club.aurorapvp.modules;

import club.aurorapvp.configs.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;

public class Queue {

    public static LinkedHashMap<Player, Player> duelQueue = new LinkedHashMap<>();

    public static void enterDuelQueue(Player p, String opponent) {
        if (Bukkit.getPlayer(opponent) != null) {
            duelQueue.put(p, Bukkit.getPlayer(opponent));
        } else if (opponent == null) {
            p.sendMessage(Lang.getComponent("pick-opponent"));
        } else {
            p.sendMessage(Lang.getComponent("unknown-opponent"));
        }
    }

    public static void checkQueue() {

    }
}
