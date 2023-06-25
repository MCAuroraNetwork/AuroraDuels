package club.aurorapvp.auroraduels.modules.queues;

import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.modules.requests.Request;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class QueueGUI {
  private static final Inventory inventory = Bukkit.createInventory(null, 27, Lang.getComponent("queue-menu"));
  public static void init() {
    ItemStack[] itemStacks = new ItemStack[Queue.getQueues().size()];

    int i = 0;
    for (Queue queue : Queue.getQueues()) {
      ItemStack displayItem = queue.getDisplayItem();
      ItemMeta meta = displayItem.getItemMeta();
      meta.lore(List.of(MiniMessage.miniMessage()
          .deserialize("<gradient:#FFAA00:#FF55FF>Currently in Queue: " + queue.inQueue())));
      displayItem.setItemMeta(meta);

      itemStacks[i] = queue.getDisplayItem();
    }

    populateGUI(itemStacks);
  }

  public static void populateGUI(ItemStack[] displayItems) {
    int itemStacksCount = displayItems.length;
    int startingSlotInMiddleRow = 9;

    int slotsBetweenItems = (9 - itemStacksCount % 9) / (itemStacksCount % 9 + 1);

    int startingSlot = startingSlotInMiddleRow + slotsBetweenItems;

    for (int i = 0; i < displayItems.length; i++) {
      int row = i / 9;
      int col = i % 9;

      int slotIndex = startingSlot + row * 9 + (slotsBetweenItems + 1) * col;
      inventory.setItem(slotIndex, displayItems[i]);
    }
  }

  public static void updateGUI(ItemStack displayItem) {
    int startingSlotInMiddleRow = 9;

    for (int i = startingSlotInMiddleRow; i < startingSlotInMiddleRow + 9; i++) {
      ItemStack currentItem = inventory.getItem(i);
      if (currentItem != null && currentItem.isSimilar(displayItem)) {

        inventory.setItem(i, displayItem);
        break;
      }
    }
  }

  public static void open(Player p) {
    p.openInventory(inventory);
  }

  public static boolean isQueueGUI(Inventory inv) {
    return inv == inventory;
  }

  public static void onInventoryClicked(InventoryClickEvent event) {
    if (!QueueGUI.isQueueGUI(event.getClickedInventory())) {
      return;
    }

    final ItemStack clickedItem = event.getCurrentItem();

    if (clickedItem == null || clickedItem.getType().isAir()) {
      return;
    }

    event.setCancelled(true);

    Player p = (Player) event.getWhoClicked();

    if (event.getCurrentItem().getItemMeta().displayName() == null) {
      p.sendMessage(Lang.getComponent("queue-not-found"));
    } else {
      String itemName = PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName());
      String queueName = itemName.substring(1, itemName.length() - 1);

      Queue queue = Queue.getQueue(queueName.toLowerCase());

      if (queue == null) {
        p.sendMessage(Lang.getComponent("queue-not-found"));
        return;
      }

      Objects.requireNonNull(Request.getRequest(p)).setQueue(queue);
    }
  }
}
