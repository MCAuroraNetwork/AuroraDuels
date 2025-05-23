package club.aurorapvp.auroraduels;

import club.aurorapvp.auroraduels.commands.CommandManager;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.EventManager;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.Placeholders;
import club.aurorapvp.auroraduels.modules.Wand;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class AuroraDuels extends JavaPlugin {

  private static AuroraDuels INSTANCE;
  private static Config config;
  private static Lang lang;
  private boolean auroraKitsInstalled = false;
  private boolean auroraCombatInstalled = false;
  private boolean auroraWarpsInstalled = false;
  private boolean auroraChatInstalled = false;
  private boolean discordSrvInstalled = false;

  private MongoClient mongoClient;
  private MongoDatabase mongoDatabase;

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

  public boolean isAuroraChatInstalled() {
    return auroraChatInstalled;
  }

  public boolean isDiscordSRVInstalled() {
    return discordSrvInstalled;
  }
  
  public @NotNull YamlConfiguration getConfig() {
    return config.getYaml();
  }

  public Lang getLang() {
    return lang;
  }

  public void reloadConfig() {
    config.reload();
  }

  public MongoDatabase getDatabase() {
    return mongoDatabase;
  }

  @Override
  public void onEnable() {
    long startTime = System.currentTimeMillis();

    INSTANCE = this;

    // Initialize configs
    config = new Config();
    lang = new Lang();
    
    String connectionString =
            this.getConfig().getString("mongodb.address", "mongodb://localhost:27017");
    String databaseName = this.getConfig().getString("mongodb.database-name", "aurora_combat");

    mongoClient = MongoClients.create(connectionString);
    mongoDatabase = mongoClient.getDatabase(databaseName);

    getLogger().info("Connected to MongoDB database: " + databaseName);

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

    if (Bukkit.getPluginManager().getPlugin("AuroraChat") != null
            && this.getConfig().getBoolean("optional-plugins.aurorachat-compatibility")) {
      auroraChatInstalled = true;
    }

    if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null
        && this.getConfig().getBoolean("optional-plugins.discordsrv-compatibility")) {
      discordSrvInstalled = true;
    }

    // Initialize classes
    CommandManager.init();
    EventManager.init();
    Arena.init();
    DuelQueue.init();
    Wand.init();

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

    if (mongoClient != null) {
      mongoClient.close();
      getLogger().info("MongoDB connection closed.");
    }

    getLogger().info("AuroraDuels disabled in " + (System.currentTimeMillis() - startTime) + "ms");
  }
}
