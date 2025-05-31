package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.QueueSelectionEvent;
import club.aurorapvp.auroraduels.modules.queues.matchmaking.MatchmakingRequest;
import club.aurorapvp.auroraduels.modules.queues.matchmaking.MatchmakingService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MatchmakingListener implements Listener {
  public MatchmakingListener() {
    AuroraDuels.getInstance().getServer().getPluginManager().registerEvents(this, AuroraDuels.getInstance());
  }

  @EventHandler
  public void onQueueSelect(QueueSelectionEvent event) {
    if (!event.getContext().equals("matchmaking") || event.isCancelled()) {
      return;
    }
    if (MatchmakingService.getInstance().isInQueue(event.getPlayer())) {
      return;
    }
    MatchmakingService.getInstance().addRequest(
            new MatchmakingRequest(
                    event.getPlayer(),
                    event.getSelectedQueues(),
                    event.isRanked()
            )
    );
    event.getPlayer().sendMessage(AuroraDuels.getInstance().getLang().getComponent("looking-match"));
  }
}