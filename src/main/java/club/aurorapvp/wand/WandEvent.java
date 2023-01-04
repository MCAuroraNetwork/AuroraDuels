package club.aurorapvp.wand;

import com.sk89q.worldedit.math.BlockVector3;
import java.io.Serial;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public class WandEvent extends EventObject {
  @Serial
  private static final long serialVersionUID = 1L;
  private static final Set<WandListener> LISTENERS = new HashSet<>();

  public WandEvent(Object source) {
    super(source);
  }

  public void trigger(Action action, BlockVector3 vector, Player p) {
    Wand.actions.add(action);
    Wand.vectors.add(vector);

    // Somewhat hacky method to cause the player to not have action delay and not spam multiple clicks
    ItemStack item = p.getInventory().getItemInMainHand();
    Material itemMaterial = item.getType();

    item.setType(Material.AIR);
    item.setType(itemMaterial);

    for (WandListener listener : LISTENERS) {
      listener.onWandEvent(this);
    }
  }

  public void registerListener(WandListener listener) {
    LISTENERS.add(listener);
  }
}
