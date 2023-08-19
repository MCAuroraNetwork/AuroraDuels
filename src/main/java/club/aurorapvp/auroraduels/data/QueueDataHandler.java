package club.aurorapvp.auroraduels.data;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class QueueDataHandler {
  private final File FILE;
  private final YamlConfiguration queueData;
  private final Queue queue;

  public QueueDataHandler(Queue queue) {
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

  public ItemStack getDisplayItem() {
    return queueData.getItemStack("displayItem");
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
    if (AuroraDuels.isAuroraKitsInstalled() && queue.getKit() != null) {
      queueData.set("kit", queue.getKit().getName());
    }

    List<String> arenaNames = new ArrayList<>();

    for (Arena arena : queue.getArenas()) {
      arenaNames.add(arena.getName());
    }

    queueData.set("arenas", arenaNames);

    queueData.set("displayItem", queue.getDisplayItem());

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
        AuroraDuels.INSTANCE.getLogger().log(Level.SEVERE, "Unable to save queue data", e);
      }
    }
  }
}
