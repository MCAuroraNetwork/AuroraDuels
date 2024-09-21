package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.duels.Duel;

/* Event that is fired after a duel ends in a draw */
public class DuelDrawEvent extends DuelOverEvent {

  public DuelDrawEvent(Duel duel, Arena arena) {
    super(duel, arena);
  }
}
