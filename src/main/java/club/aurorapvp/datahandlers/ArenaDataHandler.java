package club.aurorapvp.datahandlers;

import static club.aurorapvp.AuroraDuels.DataFolder;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

public class ArenaDataHandler {

  public static void createArenaData(String arenaName, Location center) {
    YamlConfiguration arenaData = YamlConfiguration.loadConfiguration(new File(DataFolder, "/arenas/" + arenaName));

    arenaData.set("arena.minimum-point", center);

    try {
      saveArenaData(arenaName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void saveArenaData(String arenaName) throws IOException {
    File file = new File(DataFolder, "/arenas/" + arenaName);
    YamlConfiguration arenaData = YamlConfiguration.loadConfiguration(file);

    if (!file.exists()) {
      file.mkdirs();
      file.createNewFile();
    }

    arenaData.save(file);
  }
}
