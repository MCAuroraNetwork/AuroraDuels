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

    Defaults.put("prefix", "~<gradient:#FFAA00:#FF55FF><bold>AuroraDuels ><reset>~");
    Defaults.put("set-center", "<gradient:#FFAA00:#FF55FF>prefix Click on the center of the arena");
    Defaults.put("set-pos1-and-pos2",
        "<gradient:#FFAA00:#FF55FF>prefix Right click on the first position of the arena and left click on the second position");
    Defaults.put("pos1-set", "<gradient:#FFAA00:#FF55FF>prefix Position 1 set!");
    Defaults.put("pos2-set", "<gradient:#FFAA00:#FF55FF>prefix Position 2 set!");

    for (String path : Defaults.keySet()) {
      if (!getLangFile().contains(path) || getLangFile().getString(path) == null) {
        getLangFile().set(path, Defaults.get(path));
        getLangFile().save(file);
      }
    }
  }

  public static Component getLangComponent(String path) {
    String pathString = getLangFile().getString(path);
    for (String placeholder : placeholders.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder,
            placeholders.get(placeholder));
      }
    }
    return deserializeComponent.deserialize(pathString);
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
