package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired after a duel ends */
public class DuelStartEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private final Duel duel;

  public DuelStartEvent(Duel duel) {
    this.duel = duel;

    Kit kit = duel.getKit();

    if (kit != null) {
      for (Player p : duel.getPlayers()) {
        Kit.giveKit(kit, p);
      }
    } else {
      for (Player p : duel.getPlayers()) {
        Kit playerKit = Duel.getDuelKit(p);

        if (playerKit != null) {
          Kit.giveKit(playerKit, p);
        }
      }
    }

    duel.setActive(true);
    duel.getArena().setOccupied(true);
  }

  @SuppressWarnings("unused")
  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  public Duel getDuel() {
    return duel;
  }
}
