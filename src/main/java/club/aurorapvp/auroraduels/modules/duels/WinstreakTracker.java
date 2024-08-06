package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.data.WinstreakDataHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class WinstreakTracker {

  private static final Map<UUID, WinstreakTracker> TRACKERS = new HashMap<>();
  private final WinstreakDataHandler data;
  private final Player player;
  private int wins;

  public WinstreakTracker(Player player) {
    this.player = player;
    data = new WinstreakDataHandler(this);

    if (this.exists()) {
      this.reload();
    } else {
      wins = 0;
      this.save();
    }
  }

  public static void register(Player player) {
    TRACKERS.put(player.getUniqueId(), new WinstreakTracker(player));
  }

  public static void unregister(Player player) {
    TRACKERS.remove(player.getUniqueId());
  }

  public Player getPlayer() {
    return player;
  }

  public int getWins() {
    return this.wins;
  }

  public boolean exists() {
    return this.data.exists();
  }

  public void reload() {
    wins = data.getWins();
  }

  public void save() {
    this.data.save();
  }

  public void addWin() {
    this.wins++;

    this.save();
  }

  public void resetWins() {
    wins = 0;

    this.save();
  }

  public static WinstreakTracker getTracker(Player player) {
    return TRACKERS.get(player.getUniqueId());
  }
}
