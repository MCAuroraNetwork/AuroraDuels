package club.aurorapvp.auroraduels.data;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.duels.Winstracker;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class WinsDataHandler {

  private final PersistentDataContainer container;
  private static final NamespacedKey winsKey =
      new NamespacedKey(AuroraDuels.getInstance(), "duel-wins");
  private static final NamespacedKey lossesKey =
      new NamespacedKey(AuroraDuels.getInstance(), "duel-losses");
  private static final NamespacedKey winStreakKey =
      new NamespacedKey(AuroraDuels.getInstance(), "duel-winstreak");
  private final Winstracker tracker;

  public WinsDataHandler(Winstracker tracker) {
    this.tracker = tracker;
    this.container = tracker.getPlayer().getPersistentDataContainer();
  }

  public int getWinstreak() {
    return container.getOrDefault(winStreakKey, PersistentDataType.INTEGER, -1);
  }

  public int getWins() {
    return container.getOrDefault(winsKey, PersistentDataType.INTEGER, -1);
  }

  public int getLosses() {
    return container.getOrDefault(lossesKey, PersistentDataType.INTEGER, -1);
  }

  public void save() {
    container.set(winStreakKey, PersistentDataType.INTEGER, tracker.getWinstreak());
    container.set(winsKey, PersistentDataType.INTEGER, tracker.getWins());
    container.set(lossesKey, PersistentDataType.INTEGER, tracker.getLosses());
  }

  public boolean exists() {
    return container.has(winStreakKey, PersistentDataType.INTEGER)
        && container.has(winsKey, PersistentDataType.INTEGER)
        && container.has(lossesKey, PersistentDataType.INTEGER);
  }
}
