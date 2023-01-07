package club.aurorapvp.datahandlers;

import club.aurorapvp.AuroraDuels;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ArenaData {

  public static void create(String arenaName, World arenaWorld, BlockVector3 spawnVector1, BlockVector3 spawnVector2)
      throws IOException {
    File file = new File(AuroraDuels.DATAFOLDER, "/arenas/" + arenaName + ".yml");
    YamlConfiguration arenaData =
        YamlConfiguration.loadConfiguration(file);
    Location spawn1 = new Location(arenaWorld, spawnVector1.getBlockX(), spawnVector1.getBlockY(), spawnVector1.getBlockZ());
    Location spawn2 = new Location(arenaWorld, spawnVector2.getBlockX(), spawnVector2.getBlockY(), spawnVector2.getBlockZ());

    arenaData.set("arena.world", arenaWorld.getName());
    arenaData.set("arena.spawns.spawn1", spawn1);
    arenaData.set("arena.spawns.spawn2", spawn2);

    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }

    try {
      arenaData.save(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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