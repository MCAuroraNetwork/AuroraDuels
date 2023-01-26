package club.aurorapvp.datahandlers;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.api.KitAPI;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Kit;
import club.aurorapvp.modules.Queue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.configuration.file.YamlConfiguration;

public class QueueData {
  private final Kit kit;
  private final Arena[] arenas;
  private final File FILE;
  private final YamlConfiguration queueData;
  public QueueData(Queue queue) {
    this.kit = queue.getKit();
    this.arenas = queue.getArenas();

    FILE = new File(AuroraDuels.DATA_FOLDER, "/queues/" + queue.getName() + ".yml");
    queueData = YamlConfiguration.loadConfiguration(FILE);
  }

  public File getFile() {
    return FILE;
  }

  public Kit getKit() {
    return KitAPI.getKit(queueData.getString("kit"));
  }

  public Arena[] getArenas() {
    List<?> arenaNames = queueData.getList("arenas");


    Arena[] arenaArray = new Arena[arenaNames.size()];
    int i = 0;

    for (Object arenaName : arenaNames) {
      if (arenaName instanceof String) {
        arenaArray[i] = Arena.getArena((String) arenaName);
      }
    }

    return arenaArray;
  }

  public void create() {
    queueData.set("kit", kit.getName());

    List<String> arenaNames = new ArrayList<>();

    for (Arena arena : arenas) {
      arenaNames.add(arena.getName());
    }

    queueData.set("arenas", arenaNames);

    save();
  }

  public void save() {
    if (!FILE.exists()) {
      try {
        FILE.mkdirs();
        FILE.createNewFile();
        queueData.save(FILE);
      } catch (IOException e) {
        AuroraDuels.PLUGIN.getLogger().severe("Unable to save queue data");
      }
    }
  }
}
