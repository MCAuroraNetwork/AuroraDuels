package club.aurorapvp.auroraduels.util;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class LocationUtil {
  private static final Random RANDOM = new Random();
  public static Location findSafeSpawnLocation(Location spawn1, Location spawn2, Location center) {
    int radius = (int) Math.floor(spawn1.distance(spawn2));

    World world = center.getWorld();

    int maxAttempts = 100;
    int attempts = 0;

    while (attempts < maxAttempts) {
      double angle = RANDOM.nextDouble() * Math.PI * 2;
      double distance = RANDOM.nextDouble() * radius;
      double x = center.getX() + distance * Math.cos(angle);
      double z = center.getZ() + distance * Math.sin(angle);

      Location spawnLocation = new Location(world, x, center.getY(), z);

      if (isSafeSpawnLocation(spawnLocation)) {
        return spawnLocation;
      }

      attempts++;
    }

    return null;
  }

  public static boolean isSafeSpawnLocation(Location location) {
    Material blockType = location.getBlock().getType();
    Material belowBlockType = location.clone().add(0, -1, 0).getBlock().getType();
    Material aboveBlockType = location.clone().add(0, 1, 0).getBlock().getType();

    boolean isAir = blockType == Material.AIR;
    boolean isNotDangerous =
        !blockType.isOccluding()
            && !blockType.isSolid()
            && blockType != Material.LAVA
            && blockType != Material.MAGMA_BLOCK;
    boolean isOnSolidGround =
        belowBlockType.isSolid()
            && belowBlockType != Material.LAVA
            && belowBlockType != Material.MAGMA_BLOCK;
    boolean hasEnoughHeadroom = aboveBlockType == Material.AIR;

    if (!isAir && !(isNotDangerous && isOnSolidGround && hasEnoughHeadroom)) {
      int maxAdjustments = 5;
      int adjustments = 0;

      while (adjustments < maxAdjustments) {
        location.setY(location.getY() + 1);
        blockType = location.getBlock().getType();
        belowBlockType = location.clone().add(0, -1, 0).getBlock().getType();
        aboveBlockType = location.clone().add(0, 1, 0).getBlock().getType();

        isAir = blockType == Material.AIR;
        isNotDangerous =
            !blockType.isOccluding()
                && !blockType.isSolid()
                && blockType != Material.LAVA
                && blockType != Material.MAGMA_BLOCK;
        isOnSolidGround =
            belowBlockType.isSolid()
                && belowBlockType != Material.LAVA
                && belowBlockType != Material.MAGMA_BLOCK;
        hasEnoughHeadroom = aboveBlockType == Material.AIR;

        if (isAir || (isNotDangerous && isOnSolidGround && hasEnoughHeadroom)) {
          return true;
        }

        adjustments++;
      }
    }

    return isAir || (isNotDangerous && isOnSolidGround && hasEnoughHeadroom);
  }
}
