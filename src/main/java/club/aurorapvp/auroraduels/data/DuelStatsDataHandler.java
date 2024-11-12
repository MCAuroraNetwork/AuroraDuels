package club.aurorapvp.auroraduels.data;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.DuelStats;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class DuelStatsDataHandler {
  private static final File dir = new File(AuroraDuels.getInstance().getDataFolder(), "/stats/");
  private static final Yaml yaml = new Yaml(new Constructor(DuelStats.class));
  private final DuelStats stats;

  public DuelStatsDataHandler(DuelStats stats) {
    this.stats = stats;
  }

  private static File getDir(UUID playerUuid) {
    File folder = new File(dir, playerUuid.toString());
    if (!folder.exists()) {
      folder.mkdirs();
    }
    return folder;
  }

  public void save() {
    File dir = getDir(stats.getUuid());
    File statsFile = new File(dir, stats.getTimeStarted() + ".yml");

    try (FileWriter writer = new FileWriter(statsFile)) {
      yaml.dump(stats, writer);
    } catch (IOException e) {
      AuroraDuels.getInstance().getLogger().log(Level.SEVERE, "Failed to save duel stats", e);
    }
  }

  public static DuelStats deserializeDuelStats(UUID playerUuid, long timeStarted) {
    File dir = getDir(playerUuid);
    File statsFile = new File(dir, timeStarted + ".yml");

    try (FileReader reader = new FileReader(statsFile)) {
      return yaml.load(reader);
    } catch (IOException e) {
      AuroraDuels.getInstance().getLogger().log(Level.SEVERE, "Failed to read duel stats", e);

      return null;
    }
  }
}
