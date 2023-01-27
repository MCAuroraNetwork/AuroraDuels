package club.aurorapvp.events;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;

public class WandEvent extends Event {
  private final Player p;
  private final BlockVector3 vector;
  private final Action action;
  private static final HandlerList HANDLERS = new HandlerList();

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  public WandEvent(Action action, BlockVector3 vector, Player p) {
    this.p = p;
    this.vector = vector;
    this.action = action;

    p.getPlayer().setCooldown(Material.IRON_HOE, 20);
  }

  public Player getPlayer() {
    return p;
  }

  public Action getAction() {
    return action;
  }

  public BlockVector3 getVector() {
    return vector;
  }

}
