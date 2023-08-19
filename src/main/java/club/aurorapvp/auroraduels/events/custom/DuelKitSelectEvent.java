package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.aurorakits.modules.kits.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DuelKitSelectEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private final Player player;
  private final Kit kit;

  public DuelKitSelectEvent(Player player, Kit kit) {
    this.kit = kit;
    this.player = player;
  }

  public Player getPlayer() {
    return player;
  }

  public Kit getKit() {
    return kit;
  }

  @SuppressWarnings("unused")
  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }
}
