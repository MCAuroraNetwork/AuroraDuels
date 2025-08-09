package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.data.StatsDataHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class DuelStats {

  private final UUID uuid;
  private final Map<Long, Stats> statsMap = new HashMap<>();
  private final List<Long> sortedTimesDesc = new ArrayList<>();

  public DuelStats(UUID uuid) {
    this.uuid = uuid;
    load();
  }

  private void load() {
    List<Stats> all = StatsDataHandler.loadAll(uuid);
    for (Stats s : all) {
      statsMap.put(s.getTimeStarted(), s);
      sortedTimesDesc.add(s.getTimeStarted());
    }
  }

  public Stats getStats(int index) {
    if (index < 0 || index >= sortedTimesDesc.size()) {
      return null;
    }
    return statsMap.get(sortedTimesDesc.get(index));
  }

  public static DuelStats get(Player player) {
    return new DuelStats(player.getUniqueId());
  }

  public void addStats(Stats stats) {
    if (!stats.getUuid().equals(this.uuid)) {
      return;
    }
    statsMap.put(stats.getTimeStarted(), stats);
    sortedTimesDesc.add(0, stats.getTimeStarted());
  }

  public static class Stats {
    private final UUID uuid;
    private final String arenaName;
    private final int elo;
    private final long timeStarted;
    private int totemPops = 0;
    private int damageReceived = 0;
    private int damageDealt = 0;
    private int healthRemaining;

    public Stats(UUID uuid, String arenaName, int elo, long timeStarted) {
      this.uuid = uuid;
      this.arenaName = arenaName;
      this.elo = elo;
      this.timeStarted = timeStarted;
    }

    public UUID getUuid() {
      return uuid;
    }

    public String getArenaName() {
      return arenaName;
    }

    public int getElo() {
      return elo;
    }

    public long getTimeStarted() {
      return timeStarted;
    }

    public int getTotemPops() {
      return totemPops;
    }

    public int getDamageReceived() {
      return damageReceived;
    }

    public int getDamageDealt() {
      return damageDealt;
    }

    public int getHealthRemaining() {
      return healthRemaining;
    }

    public void addTotemPop() {
      this.totemPops++;
    }

    public void addDamageReceived(int damage) {
      this.damageReceived += damage;
    }

    public void addDamageDealt(int damage) {
      this.damageDealt += damage;
    }

    public void setHealthRemaining(int health) {
      this.healthRemaining = health;
    }
  }
}
