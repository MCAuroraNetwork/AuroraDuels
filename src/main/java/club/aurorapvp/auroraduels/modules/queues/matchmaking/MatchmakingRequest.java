package club.aurorapvp.auroraduels.modules.queues.matchmaking;

import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class MatchmakingRequest {
    private final Player player;
    private final Set<DuelQueue> selectedQueues;
    private final boolean isRanked;
    private final long timestamp;

    public MatchmakingRequest(Player player, Set<DuelQueue> selectedQueues, boolean isRanked) {
        this.player = player;
        this.selectedQueues = new HashSet<>(selectedQueues);
        this.isRanked = isRanked;
        this.timestamp = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public Set<DuelQueue> getSelectedQueues() {
        return selectedQueues;
    }

    public boolean isRanked() {
        return isRanked;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchmakingRequest that = (MatchmakingRequest) o;
        return player.equals(that.player);
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }
}