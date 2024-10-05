package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroracombat.modules.CombatTag;
import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.aurorawarps.events.custom.NewDeathLocationEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DuelEventListener implements Listener {

  @EventHandler
  public void onNewDeathLocation(NewDeathLocationEvent event) {
    for (Arena arena : Arena.getArenas()) {
      if (arena.isInBounds(event.getLocation())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onDuelStart(DuelStartEvent event) {
    if (!AuroraDuels.getInstance().isAuroraCombatInstalled()) {
      return;
    }

    Rating rating = Rating.getRating("overall");

    for (Player player : event.getDuel().getPlayers()) {
      rating.setPlayerEnabled(player, false);

      CombatTag.setTaggable(player, false);
    }
  }
}
