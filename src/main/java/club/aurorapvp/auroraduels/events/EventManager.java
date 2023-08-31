package club.aurorapvp.auroraduels.events;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.listeners.DuelEventListener;
import club.aurorapvp.auroraduels.events.listeners.PlayerEventListener;
import club.aurorapvp.auroraduels.events.listeners.RequestEventListener;
import org.bukkit.Bukkit;

public class EventManager {

  public static void init() {
    Bukkit.getPluginManager().registerEvents(new PlayerEventListener(), AuroraDuels.INSTANCE);
    Bukkit.getPluginManager().registerEvents(new DuelEventListener(), AuroraDuels.INSTANCE);
    Bukkit.getPluginManager().registerEvents(new RequestEventListener(), AuroraDuels.INSTANCE);
  }
}
