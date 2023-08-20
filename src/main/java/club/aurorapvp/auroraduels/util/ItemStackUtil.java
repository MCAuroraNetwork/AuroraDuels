package club.aurorapvp.auroraduels.util;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackUtil {

  public static ItemStack createDisplayItem(final Material material, final String name) {
    final ItemStack item = new ItemStack(material, 1);
    final ItemMeta meta = item.getItemMeta();

    meta.displayName(
        MiniMessage.miniMessage()
            .deserialize(
                "<gradient:#FFAA00:#FF55FF><bold>"
                    + name.substring(0, 1).toUpperCase()
                    + name.substring(1))
            .decoration(TextDecoration.ITALIC, false));

    item.setItemMeta(meta);

    return item;
  }
}
