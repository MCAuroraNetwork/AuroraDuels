package club.aurorapvp.auroraduels.data;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
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
  private final DuelQueue duelQueue;

  public QueueDataHandler(DuelQueue duelQueue) {
    this.duelQueue = duelQueue;

    FILE = new File(AuroraDuels.getInstance().getDataFolder(),
        "/queues/" + duelQueue.getName() + ".yml");
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
    for (var name : arenaNames) {
      arenas.add(Arena.getArena((String) name));
    }

    return arenas;
  }

  public void create() {
    if (AuroraDuels.getInstance().isAuroraKitsInstalled() && duelQueue.getKit() != null) {
      queueData.set("kit", duelQueue.getKit().getName());
    }

    List<String> arenaNames = new ArrayList<>();

    for (Arena arena : duelQueue.getArenas()) {
      arenaNames.add(arena.getName());
    }

    queueData.set("arenas", arenaNames);

    queueData.set("displayItem", duelQueue.getDisplayItem());

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
        AuroraDuels.getInstance().getLogger().log(Level.SEVERE, "Unable to save queue data", e);
      }
    }
  }
}
