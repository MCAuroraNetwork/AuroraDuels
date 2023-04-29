package club.aurorapvp.configs;

import club.aurorapvp.AuroraDuels;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
  private static final File FILE = new File(AuroraDuels.DATA_FOLDER, "config.yml");
  private static YamlConfiguration config;

  public static void init() {
    reload();
    generateDefaults();
  }

  public static void generateDefaults() {
    final HashMap<String, Object> DEFAULTS = new HashMap<>();

    DEFAULTS.put("duels.requests.request-timeout", 30000);
    DEFAULTS.put("optional-plugins.aurorakits-compatibility", true);
    DEFAULTS.put("optional-plugins.auroracombat-compatibility", true);
    DEFAULTS.put("duels.enable-teleports", true);
    DEFAULTS.put("duels.max-duration", 600000);
    DEFAULTS.put("queues.enable-default-queue", true);

    for (String path : DEFAULTS.keySet()) {
      if (!get().isSet(path) || get().getString(path) == null) {
        get().set(path, DEFAULTS.get(path));
      }
    }

    try {
      get().save(FILE);
    } catch (IOException e) {
      AuroraDuels.LOGGER.severe("Failed to save config file");
    }
  }

  public static YamlConfiguration get() {
    return config;
  }

  public static void reload() {
    if (!FILE.exists()) {
      try {
        FILE.getParentFile().mkdirs();
        FILE.createNewFile();
      } catch (IOException e) {
        AuroraDuels.LOGGER.severe("Failed to generate config file");
      }
    }
    config = YamlConfiguration.loadConfiguration(FILE);
    AuroraDuels.LOGGER.info("Config reloaded!");
  }
}