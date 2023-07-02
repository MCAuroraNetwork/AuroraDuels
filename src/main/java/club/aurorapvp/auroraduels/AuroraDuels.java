package club.aurorapvp.auroraduels;

import club.aurorapvp.auroraduels.commands.CommandManager;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.EventManager;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.Placeholders;
import club.aurorapvp.auroraduels.modules.Wand;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.auroraduels.modules.queues.QueueGUI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuroraDuels extends JavaPlugin {
  public static JavaPlugin INSTANCE;
  private static boolean auroraKitsInstalled = false;
  private static boolean auroraCombatInstalled = false;

  public static boolean isAuroraKitsInstalled() {
    return auroraKitsInstalled;
  }

  public static boolean isAuroraCombatInstalled() {
    return auroraCombatInstalled;
  }

  @Override
  public void onEnable() {
    long startTime = System.currentTimeMillis();

    INSTANCE = this;

    // Initialize configs
    Config.init();
    Lang.init();

    // Check if soft depends are installed
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      new Placeholders().register();
    }

    if (Bukkit.getPluginManager().getPlugin("AuroraKits") != null &&
        Config.get().getBoolean("optional-plugins.aurorakits-compatibility")) {
      auroraKitsInstalled = true;
    }

    if (Bukkit.getPluginManager().getPlugin("AuroraCombat") != null &&
        Config.get().getBoolean("optional-plugins.auroracombat-compatibility.enabled")) {
      auroraCombatInstalled = true;
    }

    // Initialize classes
    CommandManager.init();
    EventManager.init();
    Arena.init();
    Queue.init();
    QueueGUI.init();
    Wand.init();

    getLogger().info(
        "AuroraDuels Enabled in " + (System.currentTimeMillis() - startTime) +
            "ms");
  }

  @Override
  public void onDisable() {
    long startTime = System.currentTimeMillis();

    for (Arena arena : Arena.getArenas()) {
      if (arena.isOccupied())  {
        arena.reset();
      }
    }

    getLogger().info(
        "AuroraDuels Disabled in " + (System.currentTimeMillis() - startTime) +
            "ms");
  }
}
