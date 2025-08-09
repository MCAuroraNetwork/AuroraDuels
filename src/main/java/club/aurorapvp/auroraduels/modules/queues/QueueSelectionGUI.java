package club.aurorapvp.auroraduels.modules.queues;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.events.custom.QueueSelectionEvent;
import club.aurorapvp.auroraduels.util.ItemStackUtil;
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

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueueSelectionGUI implements InventoryHolder, Listener {
    private final Inventory inventory;
    private final Player player;
    private final boolean isRanked;
    private final Set<DuelQueue> selectedQueues;
    private final List<DuelQueue> allQueues;
    private final String context;

    public QueueSelectionGUI(Player player, boolean isRanked, String context) {
        this.player = player;
        this.isRanked = isRanked;
        this.context = context;
        this.selectedQueues = new HashSet<>(DuelQueue.getPlayerSelectedQueues().getOrDefault(player, new HashSet<>()));
        this.allQueues = new ArrayList<>(DuelQueue.getQueues());
        this.allQueues.sort(Comparator.comparing(DuelQueue::getName));
        this.inventory = Bukkit.createInventory(this, 54, AuroraDuels.getInstance().getLang().getComponent("queue-menu"));

        populateGUI();
        Bukkit.getPluginManager().registerEvents(this, AuroraDuels.getInstance());
    }

    private void populateGUI() {
        int numItems = allQueues.size();
        int itemsPerRow = (numItems + 1) / 2;
        int usedSlotsFirstRow = 2 * itemsPerRow - 1;
        int unusedSlotsFirstRow = 9 - usedSlotsFirstRow;
        int startSlotFirstRow = 9 + unusedSlotsFirstRow / 2;

        for (int i = 0; i < itemsPerRow && i < numItems; i++) {
            DuelQueue queue = allQueues.get(i);

            ItemStack displayItem = ItemStackUtil.createDisplayItem(queue.getDisplayItem().getType(), queue.getName());
            ItemMeta meta = displayItem.getItemMeta();

            meta.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<yellow>Queue: " + queue.inQueue())
            ));
            displayItem.setItemMeta(meta);
            int slot = startSlotFirstRow + i * 2;
            inventory.setItem(slot, displayItem);

            Material woolType = selectedQueues.contains(queue) ? Material.GREEN_WOOL : Material.RED_WOOL;
            ItemStack wool = new ItemStack(woolType);
            ItemMeta woolMeta = wool.getItemMeta();
            woolMeta.displayName(AuroraDuels.getInstance().getLang().getComponent(
                    selectedQueues.contains(queue) ? "enabled" : "disabled"
            ));
            wool.setItemMeta(woolMeta);
            inventory.setItem(slot + 9, wool);
        }

        if (numItems > itemsPerRow) {
            int usedSlotsSecondRow = 2 * (numItems - itemsPerRow) - 1;
            int unusedSlotsSecondRow = 9 - usedSlotsSecondRow;
            int startSlotSecondRow = 27 + unusedSlotsSecondRow / 2;
            for (int i = itemsPerRow; i < numItems; i++) {
                DuelQueue queue = allQueues.get(i);

                ItemStack displayItem = ItemStackUtil.createDisplayItem(queue.getDisplayItem().getType(), queue.getName());
                ItemMeta meta = displayItem.getItemMeta();

                meta.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<yellow>Queue: " + queue.inQueue())
                ));

                displayItem.setItemMeta(meta);
                int slot = startSlotSecondRow + (i - itemsPerRow) * 2;
                inventory.setItem(slot, displayItem);

                Material woolType = selectedQueues.contains(queue) ? Material.GREEN_WOOL : Material.RED_WOOL;
                ItemStack wool = new ItemStack(woolType);
                ItemMeta woolMeta = wool.getItemMeta();
                woolMeta.displayName(AuroraDuels.getInstance().getLang().getComponent(
                        selectedQueues.contains(queue) ? "enabled" : "disabled"
                ));
                wool.setItemMeta(woolMeta);
                inventory.setItem(slot + 9, wool);
            }
        }

        ItemStack confirm = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(AuroraDuels.getInstance().getLang().getComponent("confirm-selection"));
        confirm.setItemMeta(confirmMeta);
        inventory.setItem(53, confirm);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) {
            return;
        }
        event.setCancelled(true);
        int slot = event.getSlot();

        int queueIndex = -1;
        int itemsPerRow = (allQueues.size() + 1) / 2;
        int firstRowStart = 9 + (9 - (2 * itemsPerRow - 1)) / 2;
        int secondRowStart = 27 + (9 - (2 * (allQueues.size() - itemsPerRow) - 1)) / 2;

        if (slot >= firstRowStart && slot < firstRowStart + itemsPerRow * 2 && (slot - firstRowStart) % 2 == 0) {
            queueIndex = (slot - firstRowStart) / 2;
        } else if (slot >= secondRowStart && slot < secondRowStart + (allQueues.size() - itemsPerRow) * 2 && (slot - secondRowStart) % 2 == 0) {
            queueIndex = (slot - secondRowStart) / 2 + itemsPerRow;
        }
        else if (slot >= firstRowStart + 9 && slot < firstRowStart + 9 + itemsPerRow * 2 && (slot - (firstRowStart + 9)) % 2 == 0) {
            queueIndex = (slot - (firstRowStart + 9)) / 2;
        } else if (slot >= secondRowStart + 9 && slot < secondRowStart + 9 + (allQueues.size() - itemsPerRow) * 2 && (slot - (secondRowStart + 9)) % 2 == 0) {
            queueIndex = (slot - (secondRowStart + 9)) / 2 + itemsPerRow;
        }

        if (queueIndex >= 0 && queueIndex < allQueues.size()) {
            int iconSlot = queueIndex < itemsPerRow ? firstRowStart + queueIndex * 2 : secondRowStart + (queueIndex - itemsPerRow) * 2;
            toggleQueue(allQueues.get(queueIndex), iconSlot);
            return;
        }

        if (slot == 53) {
            if (selectedQueues.isEmpty()) {
                player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("select-at-least-one"));
                return;
            }
            DuelQueue.getPlayerSelectedQueues().put(player, new HashSet<>(selectedQueues));
            QueueSelectionEvent selectionEvent = new QueueSelectionEvent(player, selectedQueues, isRanked, context);
            Bukkit.getPluginManager().callEvent(selectionEvent);
            if (!selectionEvent.isCancelled()) {
                player.closeInventory();
            }
        }
    }

    private void toggleQueue(DuelQueue queue, int iconSlot) {
        if (selectedQueues.contains(queue)) {
            selectedQueues.remove(queue);
        } else {
            selectedQueues.add(queue);
        }
        ItemStack wool = inventory.getItem(iconSlot + 9);
        if (wool != null) {
            wool.setType(selectedQueues.contains(queue) ? Material.GREEN_WOOL : Material.RED_WOOL);
            ItemMeta woolMeta = wool.getItemMeta();
            woolMeta.displayName(AuroraDuels.getInstance().getLang().getComponent(
                    selectedQueues.contains(queue) ? "enabled" : "disabled"
            ));
            wool.setItemMeta(woolMeta);
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public Set<DuelQueue> getSelectedQueues() {
        return new HashSet<>(selectedQueues);
    }

    public String getContext() {
        return context;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}