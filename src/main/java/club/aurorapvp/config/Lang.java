package club.aurorapvp.config;

import club.aurorapvp.AuroraDuels;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Lang {
  private static final HashMap<String, String> PLACEHOLDERS = new HashMap<>();
  private static final HashMap<String, String> DEFAULTS = new HashMap<>();
  private static final File FILE = new File(AuroraDuels.DATAFOLDER, "lang.yml");

  private static YamlConfiguration lang;

  public static void generateDefaults() throws IOException {
    for (Object path : get().getKeys(false).toArray()) {
      if (get().getString((String) path).startsWith("~") &&
          get().getString((String) path).endsWith("~")) {
        PLACEHOLDERS.put((String) path, get().getString((String) path).replace("~", ""));
      }
    }

    DEFAULTS.put("prefix", "~<gradient:#FFAA00:#FF55FF><bold>AuroraDuels ><reset>~");
    DEFAULTS.put(
        "set-spawn1-and-spawn2",
        "<gradient:#FFAA00:#FF55FF>prefix Left click on the first spawn point of the arena and right click on the second spawn point");
    DEFAULTS.put("set-spawn1", "<gradient:#FFAA00:#FF55FF>prefix Left click to set first spawn");
    DEFAULTS.put("set-spawn2", "<gradient:#FFAA00:#FF55FF>prefix Right click to set second spawn");
    DEFAULTS.put("spawn1-set", "<gradient:#FFAA00:#FF55FF>prefix First spawn set!");
    DEFAULTS.put("spawn2-set", "<gradient:#FFAA00:#FF55FF>prefix Second spawn set!");
    DEFAULTS.put("set-pos1-and-pos2",
        "<gradient:#FFAA00:#FF55FF>prefix Left click on the first position of the arena and right click on the second position");
    DEFAULTS.put("set-pos1", "<gradient:#FFAA00:#FF55FF>prefix Left click to set Position 1");
    DEFAULTS.put("set-pos2", "<gradient:#FFAA00:#FF55FF>prefix Right click to set Position 2");
    DEFAULTS.put("pos1-set", "<gradient:#FFAA00:#FF55FF>prefix Position 1 set!");
    DEFAULTS.put("pos2-set", "<gradient:#FFAA00:#FF55FF>prefix Position 2 set!");
    DEFAULTS.put("arena-reset", "<gradient:#FFAA00:#FF55FF>prefix Arena reset successfully!");
    DEFAULTS.put("arena-deleted", "<gradient:#FFAA00:#FF55FF>prefix Arena deleted");
    DEFAULTS.put("arena-does-not-exist", "<gradient:#FFAA00:#FF55FF>prefix Arena does not exist!");
    DEFAULTS.put("arena-already-being-created", "<gradient:#FFAA00:#FF55FF>prefix There is already an Arena being created!");

    for (String path : DEFAULTS.keySet()) {
      if (!get().contains(path) || get().getString(path) == null) {
        get().set(path, DEFAULTS.get(path));
        get().save(FILE);
      }
    }
  }

  public static Component getComponent(String path) {
    String pathString = get().getString(path);
    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder,
            PLACEHOLDERS.get(placeholder));
      }
    }
    return AuroraDuels.COMPONENT_DESERIALIZER.deserialize(pathString);
  }

  public static void saveLangFile() throws IOException {
    get().save(FILE);
  }

  public static YamlConfiguration get() {
    return lang;
  }

  public static void reload() throws IOException {
    if (!FILE.exists()) {
      FILE.getParentFile().mkdirs();

      FILE.createNewFile();
    }

    lang = YamlConfiguration.loadConfiguration(FILE);
    AuroraDuels.PLUGIN.getLogger().info("Lang reloaded!");
  }
}
