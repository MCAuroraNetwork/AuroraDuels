package club.aurorapvp.auroraduels.config;

import club.aurorapvp.auroraduels.AuroraDuels;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
  private static final File FILE = new File(AuroraDuels.INSTANCE.getDataFolder(), "config.yml");
  private static YamlConfiguration config;

  public static void init() {
    reload();
    generateDefaults();
  }

  public static void generateDefaults() {
    final HashMap<String, Object> DEFAULTS = new HashMap<>();

    DEFAULTS.put("duels.requests.request-timeout", 30000);
    DEFAULTS.put("optional-plugins.aurorakits-compatibility", true);
    DEFAULTS.put("optional-plugins.aurorawarps-compatibility", true);
    DEFAULTS.put("optional-plugins.auroracombat-compatibility.enabled", true);
    DEFAULTS.put("optional-plugins.auroracombat-compatibility.always-enable-default-elo", true);
    DEFAULTS.put("duels.enable-teleports", true);
    DEFAULTS.put("duels.allow-commands", false);
    DEFAULTS.put("duels.max-duration", 600000);
    DEFAULTS.put("requests.expiry-time", 600000);
    DEFAULTS.put("queues.enable-default-queue", true);

    for (String path : DEFAULTS.keySet()) {
      if (!get().isSet(path) || get().getString(path) == null) {
        get().set(path, DEFAULTS.get(path));
      }
    }

    try {
      get().save(FILE);
    } catch (IOException e) {
      AuroraDuels.INSTANCE.getLogger().log(Level.SEVERE, "Failed to save config file", e);
    }
  }

  public static YamlConfiguration get() {
    return config;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static void reload() {
    if (!FILE.exists()) {
      try {
        FILE.getParentFile().mkdirs();
        FILE.createNewFile();
      } catch (IOException e) {
        AuroraDuels.INSTANCE.getLogger().log(Level.SEVERE, "Failed to generate config file", e);
      }
    }
    config = YamlConfiguration.loadConfiguration(FILE);
    AuroraDuels.INSTANCE.getLogger().info("Config reloaded!");
  }
}
