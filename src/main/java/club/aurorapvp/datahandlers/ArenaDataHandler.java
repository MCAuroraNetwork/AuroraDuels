package club.aurorapvp.datahandlers;

import static club.aurorapvp.AuroraDuels.DataFolder;

import com.sk89q.worldedit.math.BlockVector3;
import java.io.File;
import org.bukkit.Location;
import org.bukkit.World;

public class ArenaDataHandler {

  public static void saveArenaData(String arenaName, World world, BlockVector3 minPoint) {
    Location loc = new Location(world, minPoint.getBlockX(), minPoint.getY(), minPoint.getBlockZ());
    File file = new File(DataFolder, "/arenas/" + arenaName);


  }
}
