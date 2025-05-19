package club.aurorapvp.auroraduels.data;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class ArenaDataHandler {

  private static Arena arena;
  private final File FILE;
  private final YamlConfiguration arenaData;

  public ArenaDataHandler(Arena arena) {
    ArenaDataHandler.arena = arena;

    FILE =
            new File(AuroraDuels.getInstance().getDataFolder(), "/arenas/" + arena.getName() + ".yml");
    arenaData = YamlConfiguration.loadConfiguration(FILE);
  }

  public World getWorld() {
    return Bukkit.getWorld(Objects.requireNonNull(arenaData.getString("arena.world")));
  }

  public BlockVector3 getFirstSpawn() {
    return BukkitAdapter.asBlockVector(
            Objects.requireNonNull(arenaData.getLocation("arena.spawns.spawn1")));
  }

  public BlockVector3 getSecondSpawn() {
    return BukkitAdapter.asBlockVector(
            Objects.requireNonNull(arenaData.getLocation("arena.spawns.spawn2")));
  }

  public Location getCornerOne() {
    return arenaData.getLocation("arena.corners.pos1");
  }

  public Location getCornerTwo() {
    return arenaData.getLocation("arena.corners.pos2");
  }

  public boolean exists() {
    return FILE.exists();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void delete() {
    FILE.delete();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void create() {
    Location spawn1 =
            new Location(
                    arena.getWorld(),
                    arena.getFirstSpawn().x(),
                    arena.getFirstSpawn().y(),
                    arena.getFirstSpawn().z());
    Location spawn2 =
            new Location(
                    arena.getWorld(),
                    arena.getSecondSpawn().x(),
                    arena.getSecondSpawn().y(),
                    arena.getSecondSpawn().z());
    Location pos1 =
            new Location(
                    arena.getWorld(),
                    arena.getFirstPosition().x(),
                    arena.getFirstPosition().y(),
                    arena.getFirstPosition().z());
    Location pos2 =
            new Location(
                    arena.getWorld(),
                    arena.getSecondPosition().x(),
                    arena.getSecondPosition().y(),
                    arena.getSecondPosition().z());

    arenaData.set("arena.world", arena.getWorld().getName());
    arenaData.set("arena.spawns.spawn1", spawn1);
    arenaData.set("arena.spawns.spawn2", spawn2);
    arenaData.set("arena.corners.pos1", pos1);
    arenaData.set("arena.corners.pos2", pos2);

    if (!FILE.exists()) {
      try {
        FILE.getParentFile().mkdirs();
        FILE.createNewFile();
        arenaData.save(FILE);
      } catch (IOException e) {
        AuroraDuels.getInstance().getLogger().log(Level.SEVERE, "Failed to save arena data", e);
      }
    }
  }
}