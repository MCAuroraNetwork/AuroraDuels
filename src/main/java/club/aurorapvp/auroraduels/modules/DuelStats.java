package club.aurorapvp.auroraduels.modules;

import club.aurorapvp.auroraduels.data.DuelStatsDataHandler;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public class DuelStats implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private final DuelStatsDataHandler data = new DuelStatsDataHandler(this);
  private final UUID uuid;
  private final String arenaName;
  private final int elo;
  private final long timeStarted;
  private int totemPops = 0;
  private int damageReceived = 0;
  private int damageDealt = 0;
  private int healthRemaining;

  public DuelStats(UUID uuid, String arenaName, int elo, long timeStarted) {
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

  public void save() {
    data.save();
  }
}
