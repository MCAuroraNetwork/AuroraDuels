package club.aurorapvp.datahandlers;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.modules.Arena;
import java.io.File;
import java.io.IOException;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

public class ArenaData {
  private static Arena arena;
  private static File file;
  public ArenaData(Arena arena) {
    ArenaData.arena = arena;
    if (!arena.exists()) {
      create();
    }

    file = new File(AuroraDuels.DATA_FOLDER, "/arenas/" + arena.getName() + ".yml");
  }

  public File getFile() {
    if (file.exists()) {
      return file;
    } else {
      return null;
    }
  }

  private void create() {
    YamlConfiguration arenaData =
        YamlConfiguration.loadConfiguration(file);
    Location spawn1 = new Location(arena.getWorld(), arena.getFirstSpawn().getBlockX(),
        arena.getFirstSpawn().getBlockY(), arena.getFirstSpawn().getBlockZ());
    Location spawn2 = new Location(arena.getWorld(), arena.getSecondSpawn().getBlockX(),
        arena.getSecondSpawn().getBlockY(), arena.getSecondSpawn().getBlockZ());

    arenaData.set("arena.world", arena.getWorld().getName());
    arenaData.set("arena.spawns.spawn1", spawn1);
    arenaData.set("arena.spawns.spawn2", spawn2);

    if (!file.exists()) {
      try {
        file.getParentFile().mkdirs();
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    try {
      arenaData.save(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

