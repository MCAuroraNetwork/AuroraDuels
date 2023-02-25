package club.aurorapvp.events;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.events.listeners.Player;
import org.bukkit.Bukkit;

public class Events {
  public Events() {
    Bukkit.getPluginManager().registerEvents(new Player(), AuroraDuels.INSTANCE);
  }
}
