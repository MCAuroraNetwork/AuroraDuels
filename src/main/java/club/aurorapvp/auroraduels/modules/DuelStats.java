package club.aurorapvp.auroraduels.modules;

import org.bukkit.entity.Player;

public class DuelStats {
    private final Player player;
    private final Arena arena;
    private final int elo;
    private int totemPops = 0;
    private int damageReceived = 0;
    private int damageDealt = 0;
    private int healthRemaining;

    public DuelStats(Player player, Arena arena, int elo) {
        this.player = player;
        this.arena = arena;
        this.elo = elo;
    }

    public Player getPlayer() {
        return player;
    }

    public Arena getArena() {
        return arena;
    }

    public int getElo() {
        return elo;
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
