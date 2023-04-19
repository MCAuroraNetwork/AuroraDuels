package club.aurorapvp.events.listeners;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.events.custom.DuelEndEvent;
import club.aurorapvp.events.custom.DuelStartEvent;
import club.aurorapvp.modules.CombatTag;
import club.aurorapvp.modules.Rating;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DuelEvents implements Listener {
  @EventHandler
  public void onDuelStart(DuelStartEvent event) {
    if (!AuroraDuels.isAuroraCombatInstalled()) {
      return;
    }

    CombatTag.setTaggable(event.getPlayerOne(), false);
    CombatTag.setTaggable(event.getPlayerTwo(), false);

    Rating.setUpdating("default", false);
  }

  @EventHandler
  public void onDuelEnd(DuelEndEvent event) {
    if (!AuroraDuels.isAuroraCombatInstalled()) {
      return;
    }

    CombatTag.setTaggable(event.getPlayerOne(), true);
    CombatTag.setTaggable(event.getPlayerTwo(), true);

    if (event.getDuel().isRanked()) {
      Rating.changeRating(event.getLoser(), event.getWinner(), "duels");
    }

    Rating.setUpdating("default", true);
  }
}