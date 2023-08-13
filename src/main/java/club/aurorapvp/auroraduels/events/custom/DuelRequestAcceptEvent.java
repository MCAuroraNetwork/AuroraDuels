package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.modules.duels.Duel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired when a duel enters the duel queue */
public class DuelRequestAcceptEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private final Player p;
  private final Duel duel;

  public DuelRequestAcceptEvent(Player p, Duel duel) {
    this.p = p;
    this.duel = duel;
  }

  @SuppressWarnings("unused")
  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  public Player getPlayer() {
    return p;
  }

  public Duel getDuel() {
    return duel;
  }
}

