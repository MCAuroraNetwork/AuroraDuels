package club.aurorapvp.config;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static club.aurorapvp.AuroraDuels.*;

public class LangHandler {
  private static final HashMap<String, String> placeholders = new HashMap<>();
  private static final HashMap<String, String> Defaults = new HashMap<>();
  private static final File file = new File(DataFolder, "lang.yml");

  private static YamlConfiguration lang;

  public static void generateLangDefaults() throws IOException {
    for (Object path : getLang().getKeys(false).toArray()) {
      if (getLang().getString((String) path).startsWith("~") &&
          getLang().getString((String) path).endsWith("~")) {
        placeholders.put((String) path, getLang().getString((String) path).replace("~", ""));
      }
    }

    Defaults.put("prefix", "~<gradient:#FFAA00:#FF55FF><bold>AuroraDuels ><reset>~");
    Defaults.put(
        "set-spawn1-and-spawn2",
        "<gradient:#FFAA00:#FF55FF>prefix Left click on the first spawn point of the arena and right click on the second spawn point");
    Defaults.put("set-spawn1", "<gradient:#FFAA00:#FF55FF>prefix Left click to set first spawn");
    Defaults.put("set-spawn2", "<gradient:#FFAA00:#FF55FF>prefix Right click to set second spawn");
    Defaults.put("spawn1-set", "<gradient:#FFAA00:#FF55FF>prefix First spawn set!");
    Defaults.put("spawn2-set", "<gradient:#FFAA00:#FF55FF>prefix Second spawn set!");
    Defaults.put("set-pos1-and-pos2",
        "<gradient:#FFAA00:#FF55FF>prefix Left click on the first position of the arena and right click on the second position");
    Defaults.put("set-pos1", "<gradient:#FFAA00:#FF55FF>prefix Left click to set Position 1");
    Defaults.put("set-pos2", "<gradient:#FFAA00:#FF55FF>prefix Right click to set Position 2");
    Defaults.put("pos1-set", "<gradient:#FFAA00:#FF55FF>prefix Position 1 set!");
    Defaults.put("pos2-set", "<gradient:#FFAA00:#FF55FF>prefix Position 2 set!");
    Defaults.put("arena-reset", "<gradient:#FFAA00:#FF55FF>prefix Arena reset successfully!");
    Defaults.put("arena-deleted", "<gradient:#FFAA00:#FF55FF>prefix Arena deleted");
    Defaults.put("arena-does-not-exist", "<gradient:#FFAA00:#FF55FF>prefix Arena does not exist!");
    Defaults.put("arena-already-being-created", "<gradient:#FFAA00:#FF55FF>prefix There is already an Arena being created!");

    for (String path : Defaults.keySet()) {
      if (!getLang().contains(path) || getLang().getString(path) == null) {
        getLang().set(path, Defaults.get(path));
        getLang().save(file);
      }
    }
  }

  public static Component getLangComponent(String path) {
    String pathString = getLang().getString(path);
    for (String placeholder : placeholders.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder,
            placeholders.get(placeholder));
      }
    }
    return deserializeComponent.deserialize(pathString);
  }

  public static void saveLangFile() throws IOException {
    getLang().save(file);
  }

  public static YamlConfiguration getLang() {
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
