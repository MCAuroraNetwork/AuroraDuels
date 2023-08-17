package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.checkerframework.checker.nullness.qual.NonNull;

public class DuelKitGui implements Listener {
  private static final Map<Player, DuelKitGui> KITS_GUIS = new HashMap<>();
  private final Player p;
  private Page[] pages;
  private int currentPage = 0;
  private int index;

  public DuelKitGui(Player p) {
    this.p = p;

    reload();

    KITS_GUIS.put(p, this);

    Bukkit.getPluginManager().registerEvents(this, AuroraDuels.INSTANCE);

    open();
  }

  public Player getPlayer() {
    return p;
  }

  public static void remove(Player p) {
    KITS_GUIS.remove(p);
  }

  public void nextPage() {
    if (currentPage <= index) {
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
      currentPage = index;
    }
    this.open();
  }

  public Inventory getCurrentPage() {
    return pages[currentPage].getInventory();
  }

  public void reload() {
    Set<Kit> kitSet = Kit.getKits(this.getPlayer(), true);

    for (int i = 0; i < kitSet.size(); i++) {
      if (i % 44 == 0 && i != 0) {
        index++;
      }
    }

    pages = new Page[index + 1];

    int currentPages = index + 1;

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

      pages[index] = new Page(kits, this);
      index++;
    }
  }

  private void open() {
    p.openInventory(this.getCurrentPage());
  }

  @EventHandler
  public void onInventoryClicked(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder() instanceof Page page)) {
      return;
    }

    Player p = (Player) event.getWhoClicked();
    DuelKitGui duelGUI = page.getGui();
    Duel duel = Duel.getDuel(p);

    assert duel != null;

    event.setCancelled(true);

    if (event.getSlot() == 45) {
      duelGUI.previousPage();
      return;
    } else if (event.getSlot() == 53) {
      duelGUI.nextPage();
      return;
    } else if (event.getSlot() == 49) {
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
      String itemName =
          PlainTextComponentSerializer.plainText()
              .serialize(event.getCurrentItem().displayName());
      String kitName = itemName.substring(1, itemName.length() - 1);

      Kit kit = Kit.getKit(p, kitName);

      assert kit != null;
      duel.setKit(kit);
      p.closeInventory();
    }
  }

  public static DuelKitGui getGui(Player p) {
    return KITS_GUIS.getOrDefault(p, new DuelKitGui(p));
  }

  public static class Page implements InventoryHolder {
    private final Inventory inventory;
    private final DuelKitGui gui;

    private Page(Kit[] kits, DuelKitGui gui) {
      inventory = Bukkit.createInventory(this, 54, Lang.getComponent("duel-kit-gui"));
      this.gui = gui;

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

      ItemStack noKit = new ItemStack(Material.BARRIER);
      meta = noKit.getItemMeta();
      meta.displayName(Lang.getComponent("no-kit"));
      noKit.setItemMeta(meta);

      inventory.setItem(49, noKit);
      inventory.setItem(45, previousPage);
      inventory.setItem(53, nextPage);
    }

    public DuelKitGui getGui() {
      return gui;
    }

    @Override
    @NonNull
    public Inventory getInventory() {
      return inventory;
    }
  }
}
