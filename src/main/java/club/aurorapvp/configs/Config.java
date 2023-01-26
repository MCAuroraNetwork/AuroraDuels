package club.aurorapvp.configs;

import club.aurorapvp.AuroraDuels;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
  private static final HashMap<String, String> DEFAULTS = new HashMap<>();
  private static final File FILE = new File(AuroraDuels.DATA_FOLDER, "config.yml");
  private static YamlConfiguration config;

  public static void generateDefaults() {
    DEFAULTS.put("duel-requests.request-timeout", "30000");

    for (String path : DEFAULTS.keySet()) {
      if (!get().contains(path) || get().getString(path) == null) {
        get().set(path, DEFAULTS.get(path));

        try {
          get().save(FILE);
        } catch (IOException e) {
          AuroraDuels.PLUGIN.getLogger().severe("Failed to save config file");
        }
      }
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
        AuroraDuels.PLUGIN.getLogger().severe("Failed to generate config file");
      }
    }
    config = YamlConfiguration.loadConfiguration(FILE);
    AuroraDuels.PLUGIN.getLogger().info("Config reloaded!");
  }
}