package club.aurorapvp.datahandlers;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.modules.Arena;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ArenaData implements ArenaDataInterface {
  private static Arena arena;
  public ArenaData(Arena arena) {
    ArenaData.arena = arena;
  }

  @Override
  public File getFile() {
    File file = new File(AuroraDuels.DATAFOLDER, "/arenas/" + arena.getName() + ".yml");
    if (file.exists()) {
      return file;
    } else {
      return null;
    }
  }

  @Override
  public void create() {
    File file = new File(AuroraDuels.DATAFOLDER, "/arenas/" + arena.getName() + ".yml");
    YamlConfiguration arenaData =
        YamlConfiguration.loadConfiguration(file);
    Location spawn1 = new Location(arena.getWorld(), arena.getFirstSpawn().getBlockX(), arena.getFirstSpawn().getBlockY(), arena.getFirstSpawn().getBlockZ());
    Location spawn2 = new Location(arena.getWorld(), arena.getSecondSpawn().getBlockX(), arena.getSecondSpawn().getBlockY(), arena.getSecondSpawn().getBlockZ());

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

interface ArenaDataInterface {
  File getFile();
  void create();
}