package club.aurorapvp.auroraduels.datahandlers;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.Kit;
import club.aurorapvp.auroraduels.modules.Queue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;

public class QueueData {
  private final File FILE;
  private final YamlConfiguration queueData;
  private final Queue queue;

  public QueueData(Queue queue) {
    this.queue = queue;

    FILE = new File(AuroraDuels.INSTANCE.getDataFolder(), "/queues/" + queue.getName() + ".yml");
    queueData = YamlConfiguration.loadConfiguration(FILE);
  }

  public File getFile() {
    return FILE;
  }

  public Kit getKit() {
    return Kit.getKit(queueData.getString("kit"));
  }

  public Set<Arena> getArenas() {
    List<?> arenaNames = queueData.getList("arenas");

    Set<Arena> arenas = new HashSet<>();

    assert arenaNames != null;
    for (Object name : arenaNames) {
      arenas.add(Arena.getArena((String) name));
    }

    return arenas;
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

  @SuppressWarnings("ResultOfMethodCallIgnored")
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
