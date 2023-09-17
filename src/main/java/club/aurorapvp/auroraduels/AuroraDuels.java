package club.aurorapvp.auroraduels;

import club.aurorapvp.auroraduels.commands.CommandManager;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.EventManager;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.Placeholders;
import club.aurorapvp.auroraduels.modules.Wand;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.TeamDuel;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.auroraduels.modules.queues.QueueGUI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class AuroraDuels extends JavaPlugin {

  private static AuroraDuels INSTANCE;
  private static QueueGUI queueGUI;
  private static Config config;
  private static Lang lang;
  private boolean auroraKitsInstalled = false;
  private boolean auroraCombatInstalled = false;
  private boolean auroraWarpsInstalled = false;
  private boolean discordSrvInstalled = false;

  public static AuroraDuels getInstance() {
    return INSTANCE;
  }

  public boolean isAuroraKitsInstalled() {
    return auroraKitsInstalled;
  }

  public boolean isAuroraCombatInstalled() {
    return auroraCombatInstalled;
  }

  public boolean isAuroraWarpsInstalled() {
    return auroraWarpsInstalled;
  }

  public boolean isDiscordSRVInstalled() { return discordSrvInstalled; }

  public static QueueGUI getQueueGUI() {
    return queueGUI;
  }

  public @NotNull YamlConfiguration getConfig() {
    return config.getYaml();
  }

  public Lang getLang() {
    return lang;
  }

  @Override
  public void onEnable() {
    long startTime = System.currentTimeMillis();

    INSTANCE = this;

    // Initialize configs
    config = new Config();
    lang = new Lang();

    // Check if soft depends are installed
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      new Placeholders().register();
    }

    if (Bukkit.getPluginManager().getPlugin("AuroraKits") != null
        && this.getConfig().getBoolean("optional-plugins.aurorakits-compatibility")) {
      auroraKitsInstalled = true;
    }

    if (Bukkit.getPluginManager().getPlugin("AuroraCombat") != null
        && this.getConfig().getBoolean("optional-plugins.auroracombat-compatibility.enabled")) {
      auroraCombatInstalled = true;
    }

    if (Bukkit.getPluginManager().getPlugin("AuroraWarps") != null
        && this.getConfig().getBoolean("optional-plugins.aurorawarps-compatibility")) {
      auroraWarpsInstalled = true;
    }

    if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null
        && this.getConfig().getBoolean("optional-plugins.discordsrv-compatibility")) {
      discordSrvInstalled = true;
    }

    // Initialize classes
    CommandManager.init();
    EventManager.init();
    Arena.init();
    Queue.init();
    Wand.init();
    TeamDuel.init();

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

  public void reloadConfig() {
    config.reload();
  }
}
