package club.aurorapvp.config;

import club.aurorapvp.AuroraDuels;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
  private static final HashMap<String, String> DEFAULTS = new HashMap<>();
  private static final File FILE = new File(AuroraDuels.DATAFOLDER, "config.yml");
  private static YamlConfiguration config;

  public static void generateDefaults() throws IOException {
    DEFAULTS.put("", "");

    for (String path : DEFAULTS.keySet()) {
      if (!get().contains(path) || get().getString(path) == null) {
        get().set(path, DEFAULTS.get(path));
        get().save(FILE);
      }
    }
  }

  public static YamlConfiguration get() {
    return config;
  }

  public static void reload() throws IOException {
    if (!FILE.exists()) {
      FILE.getParentFile().mkdirs();

      FILE.createNewFile();
    }
    config = YamlConfiguration.loadConfiguration(FILE);
    AuroraDuels.PLUGIN.getLogger().info("Config reloaded!");
  }
}
