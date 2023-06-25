package club.aurorapvp.auroraduels.events;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.listeners.DuelEvents;
import club.aurorapvp.auroraduels.events.listeners.InventoryEvents;
import club.aurorapvp.auroraduels.events.listeners.PlayerEvents;
import org.bukkit.Bukkit;

public class Events {
  public static void init() {
    Bukkit.getPluginManager().registerEvents(new PlayerEvents(), AuroraDuels.INSTANCE);
    Bukkit.getPluginManager().registerEvents(new DuelEvents(), AuroraDuels.INSTANCE);
    Bukkit.getPluginManager().registerEvents(new InventoryEvents(), AuroraDuels.INSTANCE);
  }
}
