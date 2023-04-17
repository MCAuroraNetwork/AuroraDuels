package club.aurorapvp.events;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.events.listeners.DuelEvents;
import club.aurorapvp.events.listeners.PlayerEvents;
import org.bukkit.Bukkit;

public class Events {
  public static void init() {
    Bukkit.getPluginManager().registerEvents(new PlayerEvents(), AuroraDuels.INSTANCE);
    Bukkit.getPluginManager().registerEvents(new DuelEvents(), AuroraDuels.INSTANCE);
  }
}
