package club.aurorapvp.configs;

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
        "prefix <gradient:#FFAA00:#FF55FF>Left click on the first spawn point of the arena and right click on the second spawn point");
    DEFAULTS.put("set-spawn1", "prefix <gradient:#FFAA00:#FF55FF>Left click to set first spawn");
    DEFAULTS.put("set-spawn2", "prefix <gradient:#FFAA00:#FF55FF>Right click to set second spawn");
    DEFAULTS.put("spawn1-set", "prefix <gradient:#FFAA00:#FF55FF>First spawn set!");
    DEFAULTS.put("spawn2-set", "prefix <gradient:#FFAA00:#FF55FF>Second spawn set!");
    DEFAULTS.put("spawns-set", "prefix <gradient:#FFAA00:#FF55FF>Arena spawnpoints set!");
    DEFAULTS.put("set-pos1-and-pos2",
        "prefix <gradient:#FFAA00:#FF55FF>Left click on the first corner of the arena and right click on the second corner");
    DEFAULTS.put("set-pos1", "prefix <gradient:#FFAA00:#FF55FF>Left click to set Position 1");
    DEFAULTS.put("set-pos2", "prefix <gradient:#FFAA00:#FF55FF>Right click to set Position 2");
    DEFAULTS.put("pos1-set", "prefix <gradient:#FFAA00:#FF55FF>Position 1 set!");
    DEFAULTS.put("pos2-set", "prefix <gradient:#FFAA00:#FF55FF>Position 2 set!");
    DEFAULTS.put("arena-created", "prefix <gradient:#FFAA00:#FF55FF>Arena created successfully!");
    DEFAULTS.put("arena-reset", "prefix <gradient:#FFAA00:#FF55FF>Arena reset successfully!");
    DEFAULTS.put("arena-deleted", "prefix <gradient:#FFAA00:#FF55FF>Arena deleted");
    DEFAULTS.put("arena-does-not-exist", "prefix <gradient:#FFAA00:#FF55FF>Arena does not exist!");
    DEFAULTS.put("arena-already-being-created", "prefix <gradient:#FFAA00:#FF55FF>There is already an Arena being created!");
    DEFAULTS.put("arena-already-exists", "prefix <gradient:#FFAA00:#FF55FF>That arena already exists!");
    DEFAULTS.put("pick-opponent", "prefix <gradient:#FFAA00:#FF55FF>Please specify an opponent!");
    DEFAULTS.put("unknown-opponent", "prefix <gradient:#FFAA00:#FF55FF>Cannot find opponent");

    for (String path : DEFAULTS.keySet()) {
      if (!get().contains(path) || get().getString(path) == null) {
        get().set(path, DEFAULTS.get(path));
        get().save(FILE);
      }
    }
  }

  public static String getString(String message) {
    String pathString = get().getString(message);
    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder,
            PLACEHOLDERS.get(placeholder));
      }
    }
    return pathString;
  }

  public static Component getComponent(String message) {
    String pathString = get().getString(message);
    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder,
            PLACEHOLDERS.get(placeholder));
      }
    }
    return AuroraDuels.COMPONENT_DESERIALIZER.deserialize(pathString);
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
