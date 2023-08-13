package club.aurorapvp.auroraduels.modules.queues;

import club.aurorapvp.auroraduels.config.Lang;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class QueueGUI implements InventoryHolder {
  private final Inventory inventory =
      Bukkit.createInventory(this, 27, Lang.getComponent("queue-menu"));

  public QueueGUI() {
    ItemStack[] itemStacks = new ItemStack[Queue.getQueues().size()];

    int i = 0;
    for (Queue queue : Queue.getQueues()) {
      ItemStack displayItem = queue.getDisplayItem();
      ItemMeta meta = displayItem.getItemMeta();

      if (queue.getKit() != null) {
        meta.lore(
            List.of(
                MiniMessage.miniMessage()
                    .deserialize("<gradient:#FFAA00:#FF55FF>Kit: " + queue.getKit().getName())));
      }

      meta.lore(
          List.of(
              MiniMessage.miniMessage()
                  .deserialize(
                      "<gradient:#FFAA00:#FF55FF>Currently in Queue: " + queue.inQueue())));
      displayItem.setItemMeta(meta);

      itemStacks[i] = displayItem;
      i++;
    }

    populateGUI(itemStacks);
  }

  public void populateGUI(ItemStack[] displayItems) {
    int itemStacksCount = displayItems.length;
    int startingSlotInMiddleRow = 9;

    int slotsBetweenItems = Math.max(0, (9 - itemStacksCount) / (itemStacksCount + 1));

    int startingSlot = startingSlotInMiddleRow + slotsBetweenItems;
    int slotsBetweenColumns = slotsBetweenItems + 1;

    for (int i = 0; i < displayItems.length; i++) {
      int row = i / 9;
      int col = i % 9;

      int slotIndex = startingSlot + row * 9 + slotsBetweenColumns * col;
      inventory.setItem(slotIndex, displayItems[i]);
    }
  }

  public void addItem(ItemStack newItem) {
    ItemStack[] currentItems = inventory.getContents();

    long nonNullItemCount = Arrays.stream(currentItems).filter(Objects::nonNull).count();

    ItemStack[] updatedItems = new ItemStack[(int) nonNullItemCount + 1];

    int index = 0;
    for (ItemStack item : currentItems) {
      if (item != null) {
        updatedItems[index++] = item;
      }
    }

    updatedItems[updatedItems.length - 1] = newItem;

    inventory.clear();
    populateGUI(updatedItems);
  }

  public void reload() {
    for (ItemStack itemStack : inventory.getContents()) {
      if (itemStack != null && !itemStack.getType().isAir()) {
        String itemName =
            PlainTextComponentSerializer.plainText().serialize(itemStack.displayName());
        String queueName = itemName.substring(1, itemName.length() - 1);

        Queue queue = Queue.getQueue(queueName.toLowerCase());
        ItemMeta meta = itemStack.getItemMeta();
        assert queue != null;

        meta.lore(
            List.of(
                MiniMessage.miniMessage()
                    .deserialize(
                        "<gradient:#FFAA00:#FF55FF>Currently in Queue: " + queue.inQueue())));
        itemStack.setItemMeta(meta);
      }
    }
  }

  public void open(Player p) {
    reload();
    p.openInventory(inventory);
  }

  @Override
  public @NotNull Inventory getInventory() {
    return inventory;
  }
}
