package club.aurorapvp.events.listeners;

import club.aurorapvp.events.custom.DuelEndEvent;
import club.aurorapvp.events.custom.DuelStartEvent;
import club.aurorapvp.modules.CombatTag;
import club.aurorapvp.modules.Rating;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DuelEvents implements Listener {
  @EventHandler
  public void onDuelStart(DuelStartEvent event) {
    CombatTag.setTaggable(event.getPlayerOne(), false);
    CombatTag.setTaggable(event.getPlayerTwo(), false);

    Rating.setUpdating("default", false);
  }

  @EventHandler
  public void onDuelEnd(DuelEndEvent event) {
    CombatTag.setTaggable(event.getPlayerOne(), true);
    CombatTag.setTaggable(event.getPlayerTwo(), true);

    Rating.changeRating(event.getLoser(), event.getWinner(), "duels");
    Rating.setUpdating("default", true);
  }
}