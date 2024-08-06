package club.aurorapvp.auroraduels.data;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.duels.WinstreakTracker;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class WinstreakDataHandler {

  private final PersistentDataContainer container;
  private static final NamespacedKey winStreakKey =
      new NamespacedKey(AuroraDuels.getInstance(), "duel-winstreak");
  private final WinstreakTracker tracker;

  public WinstreakDataHandler(WinstreakTracker tracker) {
    this.tracker = tracker;
    this.container = tracker.getPlayer().getPersistentDataContainer();
  }

  public int getWins() {
    return container.getOrDefault(winStreakKey, PersistentDataType.INTEGER, -1);
  }

  public void save() {
    container.set(winStreakKey, PersistentDataType.INTEGER, tracker.getWins());
  }

  public boolean exists() {
    return container.get(winStreakKey, PersistentDataType.INTEGER) != null;
  }
}
