package club.aurorapvp.auroraduels;

import club.aurorapvp.auroraduels.commands.CommandManager;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.EventManager;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.Placeholders;
import club.aurorapvp.auroraduels.modules.Wand;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.auroraduels.modules.queues.QueueGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuroraDuels extends JavaPlugin {

  public static AuroraDuels INSTANCE;
  private static QueueGUI queueGUI;
  private static boolean auroraKitsInstalled = false;
  private static boolean auroraCombatInstalled = false;
  private static boolean auroraWarpsInstalled = false;
  private static boolean discordSrvInstalled = false;

  public static boolean isAuroraKitsInstalled() {
    return auroraKitsInstalled;
  }

  public static boolean isAuroraCombatInstalled() {
    return auroraCombatInstalled;
  }

  public static boolean isAuroraWarpsInstalled() {
    return auroraWarpsInstalled;
  }

  public static boolean isDiscordSRVInstalled() { return discordSrvInstalled; }

  public static QueueGUI getQueueGUI() {
    return queueGUI;
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

    if (Bukkit.getPluginManager().getPlugin("AuroraKits") != null
        && Config.get().getBoolean("optional-plugins.aurorakits-compatibility")) {
      auroraKitsInstalled = true;
    }

    if (Bukkit.getPluginManager().getPlugin("AuroraCombat") != null
        && Config.get().getBoolean("optional-plugins.auroracombat-compatibility.enabled")) {
      auroraCombatInstalled = true;
    }

    if (Bukkit.getPluginManager().getPlugin("AuroraWarps") != null
        && Config.get().getBoolean("optional-plugins.aurorawarps-compatibility")) {
      auroraWarpsInstalled = true;
    }

    if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null
        && Config.get().getBoolean("optional-plugins.discordsrv-compatibility")) {
      discordSrvInstalled = true;
    }

    // Initialize classes
    CommandManager.init();
    EventManager.init();
    Arena.init();
    Queue.init();
    Wand.init();

    queueGUI = new QueueGUI();

    getLogger().info("AuroraDuels enabled in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public void onDisable() {
    long startTime = System.currentTimeMillis();

    for (Duel duel : Duel.getDuels()) {
      if (duel.isActive()) {
        duel.draw();
      }
    }

    getLogger().info("AuroraDuels disabled in " + (System.currentTimeMillis() - startTime) + "ms");
  }
}
