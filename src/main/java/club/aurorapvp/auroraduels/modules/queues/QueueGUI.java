package club.aurorapvp.auroraduels.modules.queues;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.custom.QueueSelectEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class QueueGUI implements InventoryHolder, Listener {

  private final Inventory inventory =
      Bukkit.createInventory(this, 54, Lang.getComponent("queue-menu"));

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

    Bukkit.getPluginManager().registerEvents(this, AuroraDuels.INSTANCE);
  }

  @EventHandler
  public void onInventoryClicked(InventoryClickEvent event) {
    if (!Objects.equals(event.getInventory().getHolder(), this)) {
      return;
    }

    Player player = (Player) event.getWhoClicked();

    final ItemStack clickedItem = event.getCurrentItem();

    if (clickedItem == null || clickedItem.getType().isAir()) {
      return;
    }

    event.setCancelled(true);

    if (event.getCurrentItem().getItemMeta().displayName() == null) {
      player.sendMessage(Lang.getComponent("queue-not-found"));
    } else {
      String itemName =
          PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName());
      String queueName = itemName.substring(1, itemName.length() - 1);

      Queue queue = Queue.getQueue(queueName.toLowerCase());

      if (queue == null) {
        player.sendMessage(Lang.getComponent("queue-not-found"));
        return;
      }

      new QueueSelectEvent(player, queue).callEvent();
    }
  }

  public void populateGUI(ItemStack[] displayItems) {
    int numItems = displayItems.length;

    int itemsPerRow = (numItems + 1) / 2;

    int usedSlotsFirstRow = 2 * itemsPerRow - 1;
    int unusedSlotsFirstRow = 9 - usedSlotsFirstRow;
    int startSlotFirstRow = 9 + unusedSlotsFirstRow / 2;
    for (int i = 0; i < itemsPerRow; i++) {
      int slot = startSlotFirstRow + i * 2;
      inventory.setItem(slot, displayItems[i]);
    }

    if (numItems > itemsPerRow) {
      int usedSlotsSecondRow = 2 * (numItems - itemsPerRow) - 1;
      int unusedSlotsSecondRow = 9 - usedSlotsSecondRow;
      int startSlotSecondRow = 27 + unusedSlotsSecondRow / 2;
      for (int i = itemsPerRow; i < numItems; i++) {
        int slot = startSlotSecondRow + (i - itemsPerRow) * 2;
        inventory.setItem(slot, displayItems[i]);
      }
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
                        "<yellow>Currently in Queue: " + queue.inQueue())));
        itemStack.setItemMeta(meta);
      }
    }
  }

  public void open(Player player) {
    reload();

    player.openInventory(inventory);
  }

  @Override
  public @NotNull Inventory getInventory() {
    return inventory;
  }
}
