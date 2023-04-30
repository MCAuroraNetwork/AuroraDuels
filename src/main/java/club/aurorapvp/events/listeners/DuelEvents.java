package club.aurorapvp.events.listeners;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.events.custom.DuelEndEvent;
import club.aurorapvp.events.custom.DuelStartEvent;
import club.aurorapvp.modules.CombatTag;
import club.aurorapvp.modules.Rating;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DuelEvents implements Listener {
  @EventHandler
  public void onDuelStart(DuelStartEvent event) {
    if (!AuroraDuels.isAuroraCombatInstalled()) {
      return;
    }

    for (Player p : event.getDuel().getPlayers()) {
      CombatTag.setTaggable(p, false);
    }

    Rating.setUpdating("default", false);
  }

  @EventHandler
  public void onDuelEnd(DuelEndEvent event) {
    if (!AuroraDuels.isAuroraCombatInstalled()) {
      return;
    }

    for (Player p : event.getDuel().getPlayers()) {
      CombatTag.setTaggable(p, true);
    }

    // TODO update to account for 2v2+ duels
    /*
    if (event.getDuel().isRanked()) {
      Rating.changeRating(event.getLosers(), event.getWinners(), "duels");
    */

    for (Player p : event.getDuel().getSpectators()) {
      event.getDuel().setSpectator(p, false);
    }

    Rating.setUpdating("default", true);
  }
}