package club.aurorapvp.datahandlers;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.api.Kits;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Queue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;

public class QueueData {
  private final File FILE;
  private final YamlConfiguration queueData;
  private final Queue queue;

  public QueueData(Queue queue) {
    this.queue = queue;

    FILE = new File(AuroraDuels.DATA_FOLDER, "/queues/" + queue.getName() + ".yml");
    queueData = YamlConfiguration.loadConfiguration(FILE);
  }

  public File getFile() {
    return FILE;
  }

  public club.aurorapvp.modules.Kit getKit() {
    return Kits.getKit(queueData.getString("kit"));
  }

  public Arena[] getArenas() {
    List<?> arenaNames = queueData.getList("arenas");
    Arena[] arenaArray = new Arena[arenaNames.size()];

    for (int i = 0; i < arenaNames.size(); i++) {
      if (arenaNames.get(i) instanceof String) {
        arenaArray[i] = Arena.getArena((String) arenaNames.get(i));
      }
    }

    return arenaArray;
  }

  public void create() {
    if (AuroraDuels.isAuroraKitsInstalled()) {
      queueData.set("kit", queue.getKit().getName());
    }

    List<String> arenaNames = new ArrayList<>();

    for (Arena arena : queue.getArenas()) {
      arenaNames.add(arena.getName());
    }

    queueData.set("arenas", arenaNames);

    save();
  }

  public void save() {
    if (!FILE.exists()) {
      try {
        FILE.getParentFile().mkdirs();
        FILE.createNewFile();
        queueData.save(FILE);
      } catch (IOException e) {
        AuroraDuels.INSTANCE.getLogger().severe("Unable to save queue data");
      }
    }
  }
}
