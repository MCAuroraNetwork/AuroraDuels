package club.aurorapvp.datahandlers;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

import static club.aurorapvp.AuroraDuels.DataFolder;

public class ArenaDataHandler {

  public static void createArenaData(String arenaName, Location center) {
    YamlConfiguration arenaData =
        YamlConfiguration.loadConfiguration(new File(DataFolder, "/arenas/" + arenaName + ".yml"));

    arenaData.set("arena.center", center);

    try {
      saveArenaData(arenaName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void saveArenaData(String arenaName) throws IOException {
    File file = new File(DataFolder, "/arenas/" + arenaName + ".yml");
    YamlConfiguration arenaData = YamlConfiguration.loadConfiguration(file);

    if (!file.exists()) {
      file.mkdirs();
      file.createNewFile();
    }

    arenaData.save(file);
  }

  public static File getArenaDataFile(String arenaName) {
    File file = new File(DataFolder, "/arenas/" + arenaName + ".yml");
    if (file.exists()) {
      return file;
    } else {
      return null;
    }
  }
}
