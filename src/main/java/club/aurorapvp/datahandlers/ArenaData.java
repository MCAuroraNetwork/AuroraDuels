package club.aurorapvp.datahandlers;

import club.aurorapvp.AuroraDuels;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ArenaData {

  public static void create(String arenaName, World arenaWorld, BlockVector3 spawnVector1, BlockVector3 spawnVector2) {
    YamlConfiguration arenaData =
        YamlConfiguration.loadConfiguration(new File(AuroraDuels.DATAFOLDER, "/arenas/" + arenaName + ".yml"));
    Location spawn1 = new Location(arenaWorld, spawnVector1.getBlockX(), spawnVector1.getBlockY(), spawnVector1.getBlockZ());
    Location spawn2 = new Location(arenaWorld, spawnVector2.getBlockX(), spawnVector2.getBlockY(), spawnVector2.getBlockZ());

    arenaData.set("arena.spawn.spawn1", spawn1);
    arenaData.set("arena.spawns.spawn2", spawn2);

    try {
      save(arenaName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void save(String arenaName) throws IOException {
    File file = new File(AuroraDuels.DATAFOLDER, "/arenas/" + arenaName + ".yml");
    YamlConfiguration arenaData = YamlConfiguration.loadConfiguration(file);

    if (!file.exists()) {
      file.mkdirs();
      file.createNewFile();
    }

    arenaData.save(file);
  }

  public static File get(String arenaName) {
    File file = new File(AuroraDuels.DATAFOLDER, "/arenas/" + arenaName + ".yml");
    if (file.exists()) {
      return file;
    } else {
      return null;
    }
  }
}
