package club.aurorapvp.auroraduels.modules.queues;

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

public class RankedUnrankedGUI implements InventoryHolder, Listener {
    private final Inventory inventory;
    private final Player player;

    public RankedUnrankedGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 9, AuroraDuels.getInstance().getLang().getComponent("queue-menu"));

        ItemStack rankedItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta rankedMeta = rankedItem.getItemMeta();
        rankedMeta.displayName(AuroraDuels.getInstance().getLang().getComponent("ranked-match"));
        rankedItem.setItemMeta(rankedMeta);
        inventory.setItem(3, rankedItem);

        ItemStack unrankedItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta unrankedMeta = unrankedItem.getItemMeta();
        unrankedMeta.displayName(AuroraDuels.getInstance().getLang().getComponent("unranked-match"));
        unrankedItem.setItemMeta(unrankedMeta);
        inventory.setItem(5, unrankedItem);

        Bukkit.getPluginManager().registerEvents(this, AuroraDuels.getInstance());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) {
            return;
        }
        event.setCancelled(true);
        int slot = event.getSlot();
        if (slot == 3) {
            new QueueSelectionGUI(player, true, "matchmaking").open();
        } else if (slot == 5) {
            new QueueSelectionGUI(player, false, "matchmaking").open();
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}