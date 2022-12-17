package club.aurorapvp.config;

import static club.aurorapvp.AuroraDuels.DataFolder;
import static club.aurorapvp.AuroraDuels.plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigHandler {
  private static final HashMap<String, String> Defaults = new HashMap<>();
  private static final File file = new File(DataFolder, "config.yml");
  private static YamlConfiguration config;

  public static void generateConfigDefaults() throws IOException {
    Defaults.put("", "");

    for (String path : Defaults.keySet()) {
      if (!getConfig().contains(path) || getConfig().getString(path) == null) {
        getConfig().set(path, Defaults.get(path));
        getConfig().save(file);
      }
    }
  }

  public static void saveConfig() throws IOException {
    getConfig().save(file);
  }

  public static YamlConfiguration getConfig() {
    return config;
  }

  public static void reloadConfig() throws IOException {
    if (!file.exists()) {
      file.getParentFile().mkdirs();

      file.createNewFile();
    }
    config = YamlConfiguration.loadConfiguration(file);
    plugin.getLogger().info("Config reloaded!");
  }
}
