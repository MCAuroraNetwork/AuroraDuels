package club.aurorapvp.auroraduels.modules.queues.matchmaking;

import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.duels.StandardDuel;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MatchmakingService {
    private static final MatchmakingService INSTANCE = new MatchmakingService();
    private static final long MATCHMAKING_INTERVAL = 5000; // 5 seconds
    private final Set<MatchmakingRequest> waitingRequests = new HashSet<>();
    private final Timer timer = new Timer();

    private MatchmakingService() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                attemptMatchmaking();
            }
        }, 0, MATCHMAKING_INTERVAL);
    }

    public static MatchmakingService getInstance() {
        return INSTANCE;
    }

    public Set<MatchmakingRequest> getWaitingRequests() {
        return waitingRequests;
    }

    public void addRequest(MatchmakingRequest request) {
        waitingRequests.removeIf(r -> r.getPlayer().equals(request.getPlayer()));
        waitingRequests.add(request);
    }

    public void removeRequest(MatchmakingRequest request) {
        waitingRequests.remove(request);
    }

    public void removeRequest(Player player) {
        waitingRequests.removeIf(r -> r.getPlayer().equals(player));
    }

    public boolean isInQueue(Player player) {
        return waitingRequests.stream().anyMatch(r -> r.getPlayer().equals(player));
    }

    private void attemptMatchmaking() {
        Iterator<MatchmakingRequest> iterator = waitingRequests.iterator();
        while (iterator.hasNext()) {
            MatchmakingRequest req1 = iterator.next();
            if (!req1.getPlayer().isOnline()) {
                iterator.remove();
                continue;
            }
            for (MatchmakingRequest req2 : waitingRequests) {
                if (req1 == req2 || !req2.getPlayer().isOnline()) {
                    continue;
                }
                if (canMatch(req1, req2)) {
                    createDuel(req1, req2);
                    iterator.remove();
                    waitingRequests.remove(req2);
                    break;
                }
            }
        }
    }

    private boolean canMatch(MatchmakingRequest req1, MatchmakingRequest req2) {
        if (req1.isRanked() != req2.isRanked()) {
            return false;
        }
        Set<DuelQueue> queues1 = req1.getSelectedQueues();
        Set<DuelQueue> queues2 = req2.getSelectedQueues();
        for (DuelQueue q1 : queues1) {
            if (queues2.contains(q1)) {
                if (!req1.isRanked()) {
                    return true;
                } else {
                    int elo1 = getElo(req1.getPlayer());
                    int elo2 = getElo(req2.getPlayer());
                    int diff = Math.abs(elo1 - elo2);
                    long waitingTime = Math.max(
                            System.currentTimeMillis() - req1.getTimestamp(),
                            System.currentTimeMillis() - req2.getTimestamp()
                    );
                    int threshold = 100 + (int)(waitingTime / 1000) * 50;
                    return diff <= threshold;
                }
            }
        }
        return false;
    }

    private void createDuel(MatchmakingRequest req1, MatchmakingRequest req2) {
        Player p1 = req1.getPlayer();
        Player p2 = req2.getPlayer();
        Set<DuelQueue> commonQueues = new HashSet<>(req1.getSelectedQueues());
        commonQueues.retainAll(req2.getSelectedQueues());
        DuelQueue queue = commonQueues.iterator().next();
        Location loc = DuelQueue.findRespawnLocation(p1);
        Duel duel = new StandardDuel(p1, p2, queue, null, loc, req1.isRanked(), 1);
        queue.enterDuelQueue(duel);
        p1.sendMessage(AuroraDuels.getInstance().getLang().getComponent("match-found"));
        p2.sendMessage(AuroraDuels.getInstance().getLang().getComponent("match-found"));
        DuelQueue.getPlayerSelectedQueues().remove(p1);
        DuelQueue.getPlayerSelectedQueues().remove(p2);
    }

    private int getElo(Player player) {
        if (AuroraDuels.getInstance().isAuroraCombatInstalled()) {
            return Rating.getRating("overall_duels").getScore(player).getPoints();
        }
        return 0;
    }
}