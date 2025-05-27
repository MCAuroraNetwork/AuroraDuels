package club.aurorapvp.auroraduels.modules.duels.teamduels;

import club.aurorapvp.auroraduels.AuroraDuels;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class FriendlyFireGUI implements InventoryHolder, Listener {

  private final Player player;
  private final TeamDuel duel;
  private final Inventory inventory;

  public FriendlyFireGUI(Player player, TeamDuel duel) {
    this.player = player;
    this.duel = duel;
    this.inventory =
        Bukkit.createInventory(
            this, 9, AuroraDuels.getInstance().getLang().getComponent("friendly-fire-gui"));
    initializeItems();
    Bukkit.getPluginManager().registerEvents(this, AuroraDuels.getInstance());
  }

  private void initializeItems() {
    ItemStack enableItem = new ItemStack(Material.DIAMOND_SWORD);
    ItemMeta enableMeta = enableItem.getItemMeta();
    enableMeta.displayName(
        AuroraDuels.getInstance().getLang().getComponent("enable-friendly-fire"));
    enableItem.setItemMeta(enableMeta);
    inventory.setItem(3, enableItem);

    ItemStack disableItem = new ItemStack(Material.SHIELD);
    ItemMeta disableMeta = disableItem.getItemMeta();
    disableMeta.displayName(
        AuroraDuels.getInstance().getLang().getComponent("disable-friendly-fire"));
    disableItem.setItemMeta(disableMeta);
    inventory.setItem(5, disableItem);
  }

  public void open() {
    player.openInventory(inventory);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player clicker) || event.getInventory() != inventory) {
      return;
    }
    event.setCancelled(true);
    int slot = event.getSlot();
    boolean friendlyFire;
    if (slot == 3) {
      friendlyFire = true;
    } else if (slot == 5) {
      friendlyFire = false;
    } else {
      return;
    }
    duel.setFriendlyFire(friendlyFire);
    clicker.closeInventory();
  }

  @Override
  public @NotNull Inventory getInventory() {
    return inventory;
  }
}
