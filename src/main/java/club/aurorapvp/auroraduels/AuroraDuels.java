package club.aurorapvp.auroraduels;

import club.aurorapvp.auroraduels.configs.Config;
import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.events.Events;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.auroraduels.modules.Wand;
import club.aurorapvp.auroraduels.modules.queues.QueueGUI;
import club.aurorapvp.auroraduels.placeholders.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuroraDuels extends JavaPlugin {
  public static JavaPlugin INSTANCE;
  private static boolean auroraKitsInstalled = false;
  private static boolean auroraCombatInstalled = false;

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
        Config.get().getBoolean("optional-plugins.auroracombat-compatibility")) {
      auroraCombatInstalled = true;
    }

    // Initialize classes
    Commands.init();
    Events.init();
    Arena.init();
    Queue.init();
    QueueGUI.init();
    Wand.init();

    getLogger().info(
        "AuroraDuels loaded in " + (System.currentTimeMillis() - startTime) +
            "ms");
  }

  public static boolean isAuroraKitsInstalled() {
    return auroraKitsInstalled;
  }

  public static boolean isAuroraCombatInstalled() {
    return auroraCombatInstalled;
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getLogger().info("AuroraDuels unloaded");
  }
}
