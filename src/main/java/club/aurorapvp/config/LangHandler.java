package club.aurorapvp.config;

import static club.aurorapvp.AuroraDuels.DataFolder;
import static club.aurorapvp.AuroraDuels.deserializeComponent;
import static club.aurorapvp.AuroraDuels.plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

public class LangHandler {
  private static final HashMap<String, String> placeholders = new HashMap<>();
  private static final HashMap<String, String> Defaults = new HashMap<>();
  private static final File file = new File(DataFolder, "lang.yml");

  private static YamlConfiguration lang;

  public static void generateLangDefaults() throws IOException {
    for (Object path : getLangFile().getKeys(false).toArray()) {
      if (getLangFile().getString((String) path).startsWith("~") &&
          getLangFile().getString((String) path).endsWith("~")) {
        placeholders.put((String) path, getLangFile().getString((String) path).replace("~", ""));
      }
    }

    Defaults.put("", "");

    for (String path : Defaults.keySet()) {
      if (!getLangFile().contains(path) || getLangFile().getString(path) == null) {
        getLangFile().set(path, Defaults.get(path));
        getLangFile().save(file);
      }
    }
  }

  public static Component getLangComponent(String path) {
    if (getLangFile().contains(path)) {
      String pathString = getLangFile().getString(path);
      for (String placeholder : placeholders.keySet()) {
        if (pathString.contains(placeholder)) {
          pathString = pathString.replace(placeholder,
              placeholders.get(placeholder));
        }
      }
      return deserializeComponent.deserialize(pathString);
    }
    return null;
  }

  public static void saveLangFile() throws IOException {
    getLangFile().save(file);
  }

  public static YamlConfiguration getLangFile() {
    return lang;
  }

  public static void reloadLang() throws IOException {
    if (!file.exists()) {
      file.getParentFile().mkdirs();

      file.createNewFile();
    }

    lang = YamlConfiguration.loadConfiguration(file);
    plugin.getLogger().info("Lang reloaded!");
  }
}
