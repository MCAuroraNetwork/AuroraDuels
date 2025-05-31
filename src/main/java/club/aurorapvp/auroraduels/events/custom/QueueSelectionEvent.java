package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Set;

public class QueueSelectionEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Set<DuelQueue> selectedQueues;
    private final boolean isRanked;
    private final String context;
    private boolean cancelled;

    public QueueSelectionEvent(Player player, Set<DuelQueue> selectedQueues, boolean isRanked, String context) {
        this.player = player;
        this.selectedQueues = selectedQueues;
        this.isRanked = isRanked;
        this.context = context;
        this.cancelled = false;
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

    public String getContext() {
        return context;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}