package club.aurorapvp;

import club.aurorapvp.commands.Commands;
import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.events.Events;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Queue;
import club.aurorapvp.modules.Wand;
import club.aurorapvp.placeholders.Placeholders;
import java.io.File;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuroraDuels extends JavaPlugin {
  public static JavaPlugin INSTANCE;
  public static File DATA_FOLDER;
  public static PlainTextComponentSerializer COMPONENT_SERIALIZER;
  public static MiniMessage COMPONENT_DESERIALIZER;
  public static boolean auroraKitsInstalled = false;

  @Override
  public void onEnable() {
    long startTime = System.currentTimeMillis();

    // Register important variables
    INSTANCE = this;
    DATA_FOLDER = this.getDataFolder();
    COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();
    COMPONENT_DESERIALIZER = MiniMessage.miniMessage();

    // Setup classes
    new Config();
    new Lang();
    new Wand();
    new Events();
    new Arena();
    new Queue();
    new Commands();

    // Check if soft depends are installed
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      new Placeholders().register();
    }

    if (Bukkit.getPluginManager().getPlugin("AuroraKits") != null) {
      if (Config.get().getBoolean("optional-plugins.aurorakits-compatibility")) {
        auroraKitsInstalled = true;
      }
    }

    getLogger().info(
        "Aurora Duels Loaded in " + Math.subtractExact(System.currentTimeMillis(), startTime) +
            "ms");
  }

  public static boolean isAuroraKitsInstalled() {
    return auroraKitsInstalled;
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getLogger().info("Aurora Duels Unloaded");
  }
}
