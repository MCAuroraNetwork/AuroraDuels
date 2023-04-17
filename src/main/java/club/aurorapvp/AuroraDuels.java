package club.aurorapvp;

import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.events.Events;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Duel;
import club.aurorapvp.modules.DuelSign;
import club.aurorapvp.modules.Queue;
import club.aurorapvp.modules.Wand;
import club.aurorapvp.placeholders.Placeholders;
import co.aikar.commands.PaperCommandManager;
import java.io.File;
import java.util.logging.Logger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuroraDuels extends JavaPlugin {
  public static JavaPlugin INSTANCE;
  public static Logger LOGGER;
  public static File DATA_FOLDER;
  public static PlainTextComponentSerializer COMPONENT_SERIALIZER;
  public static MiniMessage COMPONENT_DESERIALIZER;
  public static boolean auroraKitsInstalled = false;
  public static boolean auroraCombatInstalled = false;

  @Override
  public void onEnable() {
    long startTime = System.currentTimeMillis();

    // Register important variables
    INSTANCE = this;
    LOGGER = this.getLogger();
    DATA_FOLDER = this.getDataFolder();
    COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();
    COMPONENT_DESERIALIZER = MiniMessage.miniMessage();

    // Setup classes
    Commands.init();
    Commands.registerCompletions();
    Events.init();
    Config.init();
    Lang.init();
    Arena.init();
    Queue.init();
    Wand.init();
    Duel.init();

    // Check if soft depends are installed
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      new Placeholders().register();
    }

    if (Bukkit.getPluginManager().getPlugin("AuroraKits") != null) {
      if (Config.get().getBoolean("optional-plugins.aurorakits-compatibility")) {
        auroraKitsInstalled = true;
      }
    }

    if (Bukkit.getPluginManager().getPlugin("AuroraCombat") != null) {
      if (Config.get().getBoolean("optional-plugins.auroracombat-compatibility")) {
        auroraCombatInstalled = true;
      }
    }

    getLogger().info(
        "AuroraDuels loaded in " + (System.currentTimeMillis() - startTime) +
            "ms");
  }

  public static boolean isAuroraKitsInstalled() {
    return auroraKitsInstalled;
  }

  public static boolean isAuroraCombatInstalled() {
    return auroraCombatInstalled;
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getLogger().info("AuroraDuels unloaded");
  }
}
