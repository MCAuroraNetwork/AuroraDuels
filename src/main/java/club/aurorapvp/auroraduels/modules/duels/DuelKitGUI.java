package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.custom.DuelKitSelectEvent;
import club.aurorapvp.aurorakits.events.custom.KitCreateEvent;
import club.aurorapvp.aurorakits.events.custom.KitDeleteEvent;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DuelKitGUI implements Listener {

  private static final Map<Player, DuelKitGUI> KITS_GUIS = new HashMap<>();
  private final Player player;
  private final List<Page> pages = new ArrayList<>();
  private int currentPage = 0;
  private int index;

  public DuelKitGUI(Player player) {
    this.player = player;

    reload();

    KITS_GUIS.put(player, this);

    Bukkit.getPluginManager().registerEvents(this, AuroraDuels.INSTANCE);
  }

  public Player getPlayer() {
    return player;
  }

  public static void remove(Player player) {
    KITS_GUIS.remove(player);
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
    return pages.get(currentPage).getInventory();
  }

  public void reload() {
    Set<Kit> kitSet = Kit.getKits(this.getPlayer(), true);

    index = 0;
    pages.clear();

    List<Kit> publicKits = new ArrayList<>();
    List<Kit> privateKits = new ArrayList<>();

    for (Kit kit : kitSet) {
      if (kit.isPublic()) {
        publicKits.add(kit);
      } else {
        privateKits.add(kit);
      }
    }

    publicKits.sort(Comparator.comparing(Kit::getName));
    privateKits.sort(Comparator.comparing(Kit::getName));

    List<Kit> sortedKits = new ArrayList<>(publicKits);
    sortedKits.addAll(privateKits);

    int pageSize = 45;
    int totalPages = (int) Math.ceil((double) sortedKits.size() / pageSize);

    for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
      int start = pageIndex * pageSize;
      int end = Math.min(start + pageSize, sortedKits.size());

      List<Kit> kitsOnPage = sortedKits.subList(start, end);
      Kit[] kitsArray = kitsOnPage.toArray(new Kit[0]);

      pages.add(new DuelKitGUI.Page(kitsArray, this));
    }
  }

  public void addKit(Kit kit) {
    Inventory lastPage = pages.get(index - 1).getInventory();

    if (lastPage.getContents()[44] != null) {
      new DuelKitGUI.Page(kit, this);
    } else {
      lastPage.addItem(kit.getDisplayItem());
    }
  }

  public void open() {
    player.openInventory(this.getCurrentPage());
  }

  @EventHandler
  public void onKitCreate(KitCreateEvent event) {
    if (!(event.getPlayer() == this.getPlayer())) {
      return;
    }

    this.addKit(event.getKit());
  }

  @EventHandler
  public void onKitDelete(KitDeleteEvent event) {
    if (!(event.getPlayer() == this.getPlayer())) {
      return;
    }

    DuelKitGUI gui = this;
    new BukkitRunnable() {
      @Override
      public void run() {
        gui.reload();
      }
    }.runTaskLater(AuroraDuels.INSTANCE, 1L);

    this.reload();
  }

  @EventHandler
  public void onInventoryClicked(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder() instanceof Page page)) {
      return;
    }

    DuelKitGUI gui = page.getGui();

    if (gui != this) {
      return;
    }

    Player player = (Player) event.getWhoClicked();

    event.setCancelled(true);

    if (event.getSlot() == 45) {
      gui.previousPage();
      return;
    } else if (event.getSlot() == 53) {
      gui.nextPage();
      return;
    } else if (event.getSlot() == 49) {
      player.closeInventory();

      new DuelKitSelectEvent(player, null).callEvent();
      return;
    }

    final ItemStack clickedItem = event.getCurrentItem();

    if (clickedItem == null || clickedItem.getType().isAir()) {
      return;
    }

    if (event.getCurrentItem().getItemMeta().displayName() == null) {
      player.sendMessage(Lang.getComponent("kit-invalid-name"));
    } else {
      String itemName =
          PlainTextComponentSerializer.plainText()
              .serialize(event.getCurrentItem().displayName());
      String kitName = itemName.substring(1, itemName.length() - 1);

      Kit kit = Kit.getKit(player, kitName);

      assert kit != null;
      player.closeInventory();

      new DuelKitSelectEvent(player, kit).callEvent();
    }
  }

  public static DuelKitGUI getGUI(Player player) {
    DuelKitGUI gui = KITS_GUIS.get(player);

    if (gui == null) {
      return new DuelKitGUI(player);
    }

    return gui;
  }

  public static class Page implements InventoryHolder {

    private final Inventory inventory;
    private final DuelKitGUI gui;

    private Page(Kit[] kits, DuelKitGUI gui) {
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
      gui.index++;
    }

    private Page(Kit kit, DuelKitGUI gui) {
      inventory = Bukkit.createInventory(this, 54, Lang.getComponent("duel-kit-gui"));
      this.gui = gui;

      inventory.addItem(kit.getDisplayItem());

      ItemMeta meta;

      ItemStack nextPage = new ItemStack(Material.PAPER);
      meta = nextPage.getItemMeta();
      meta.displayName(club.aurorapvp.aurorakits.config.Lang.getComponent("next-page"));
      nextPage.setItemMeta(meta);

      ItemStack previousPage = new ItemStack(Material.PAPER);
      meta = previousPage.getItemMeta();
      meta.displayName(club.aurorapvp.aurorakits.config.Lang.getComponent("previous-page"));
      previousPage.setItemMeta(meta);

      ItemStack noKit = new ItemStack(Material.BARRIER);
      meta = noKit.getItemMeta();
      meta.displayName(Lang.getComponent("no-kit"));
      noKit.setItemMeta(meta);

      inventory.setItem(49, noKit);
      inventory.setItem(45, nextPage);
      inventory.setItem(53, nextPage);
      gui.index++;
    }

    public DuelKitGUI getGui() {
      return gui;
    }

    @Override
    @NonNull
    public Inventory getInventory() {
      return inventory;
    }
  }
}
