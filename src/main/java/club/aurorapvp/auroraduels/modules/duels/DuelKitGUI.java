package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.modules.requests.Request;
import club.aurorapvp.aurorakits.modules.Kit;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DuelKitGUI {
  private static final Set<DuelKitGUI> KITS_GUIS = new HashSet<>();
  private final Player p;
  private Inventory[] inventories;
  private int currentPage = 0;
  private int pages;

  public DuelKitGUI(Player p) {
    this.p = p;

    reload();

    KITS_GUIS.add(this);

    open();
  }

  public Player getPlayer() {
    return p;
  }

  public void addPage(Kit[] kits) {
    Inventory inventory = Bukkit.createInventory(null, 54, Lang.getComponent("duel-kit-gui"));

    for (Kit kit : kits) {
      inventory.addItem(kit.getDisplayItem());
    }

    ItemMeta meta;

    ItemStack nextPage = new ItemStack(Material.PAPER);
    meta = nextPage.getItemMeta();
    meta.displayName(Lang.getComponent("next-page"));
    nextPage.setItemMeta(meta);

    ItemStack previousPage = new ItemStack(Material.PAPER);
    meta = previousPage.getItemMeta();
    meta.displayName(Lang.getComponent("previous-page"));
    previousPage.setItemMeta(meta);

    ItemStack noKit = new ItemStack(Material.RED_WOOL);
    meta = noKit.getItemMeta();
    meta.displayName(Lang.getComponent("no-kit"));
    noKit.setItemMeta(meta);

    inventory.setItem(49, noKit);
    inventory.setItem(45, previousPage);
    inventory.setItem(53, nextPage);

    inventories[pages] = inventory;
    pages++;
  }

  public void nextPage() {
    if (currentPage <= pages) {
      currentPage++;
    } else {
      currentPage = 0;
    }
    this.open();
  }

  public void previousPage() {
    if (currentPage >= 1) {
      currentPage--;
    } else {
      currentPage = pages;
    }
    this.open();
  }

  public Inventory getCurrentPage() {
    return inventories[currentPage];
  }

  public boolean isGuiInventory(Inventory inventory) {
    for (Inventory guiInventory : inventories) {
      if (inventory.equals(guiInventory)) {
        return true;
      }
    }
    return false;
  }

  public void reload() {
    Set<Kit> kitSet = Kit.getKits(this.getPlayer(), true);

    for (int i = 0; i < kitSet.size(); i++) {
      if (i % 44 == 0 && i != 0) {
        pages++;
      }
    }

    inventories = new Inventory[pages + 1];

    int currentPages = pages + 1;

    for (int i = 0; i < currentPages; i++) {
      int numKits = kitSet.size() - i * 45;
      Kit[] kits = new Kit[Math.max(numKits, 0)];

      Iterator<Kit> iterator = kitSet.iterator();

      int counter = 0;

      while (iterator.hasNext()) {
        if (counter < i * 45) {
          iterator.next();
          counter++;
          continue;
        }

        if (counter - i * 45 >= kits.length) {
          break;
        }

        kits[counter - i * 45] = iterator.next();
        counter++;
      }

      addPage(kits);
    }
  }

  public static void onInventoryClose(InventoryCloseEvent event) {
    Player p = (Player) event.getPlayer();

    DuelKitGUI duelGUI = DuelKitGUI.getGui(p);

    if (duelGUI == null) {
      return;
    }

    if (duelGUI.isGuiInventory(event.getInventory())) {
      KITS_GUIS.remove(duelGUI);
    }
  }

  public static void onInventoryClicked(InventoryClickEvent event) {
    Player p = (Player) event.getWhoClicked();

    DuelKitGUI duelGUI = DuelKitGUI.getGui(p);

    Request request = Request.getRequest(p);
    assert request != null;

    if (duelGUI == null || !duelGUI.isGuiInventory(event.getInventory())) {
      return;
    }

    event.setCancelled(true);

    if (event.getSlot() == 45) {
      duelGUI.previousPage();
      return;
    } else if (event.getSlot() == 53) {
      duelGUI.nextPage();
      return;
    } else if (event.getSlot() == 53) {
      request.setKit(null);
      p.closeInventory();
      return;
    }

    final ItemStack clickedItem = event.getCurrentItem();

    if (clickedItem == null || clickedItem.getType().isAir()) {
      return;
    }

    if (event.getCurrentItem().getItemMeta().displayName() == null) {
      p.sendMessage(Lang.getComponent("kit-invalid-name"));
    } else {
      String itemName = PlainTextComponentSerializer.plainText()
          .serialize(event.getCurrentItem().displayName());
      String kitName = itemName.substring(1, itemName.length() - 1);

      Kit kit = Kit.getKit(p, kitName);

      assert kit != null;
      request.setKit(kit);
      p.closeInventory();
    }
  }

  public static DuelKitGUI getGui(Player p) {
    for (DuelKitGUI kitsGui : KITS_GUIS) {
      if (kitsGui.getPlayer() == p) {
        return kitsGui;
      }
    }
    return null;
  }

  private void open() {
    p.openInventory(this.getCurrentPage());
  }
}