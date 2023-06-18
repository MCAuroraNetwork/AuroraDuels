package club.aurorapvp;

import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.events.Events;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Queue;
import club.aurorapvp.modules.Wand;
import club.aurorapvp.placeholders.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuroraDuels extends JavaPlugin {
  public static JavaPlugin INSTANCE;
  public static boolean auroraKitsInstalled = false;
  public static boolean auroraCombatInstalled = false;

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

    if (Bukkit.getPluginManager().getPlugin("AuroraKits") != null) {
      if (Config.get().getBoolean("optional-plugins.aurorakits-compatibility")) {
        auroraKitsInstalled = true;
      }
    }

    if (Bukkit.getPluginManager().getPlugin("AuroraCombat") != null) {
      if (Config.get().getBoolean("optional-plugins.auroracombat-compatibility")) {
        auroraCombatInstalled = true;
      }
    }

    // Initialize classes
    Commands.init();
    Events.init();
    Arena.init();
    Queue.init();
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
