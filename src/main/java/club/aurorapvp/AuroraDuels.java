package club.aurorapvp;

import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.listeners.Commands;
import club.aurorapvp.listeners.Events;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Placeholders;
import club.aurorapvp.modules.Queue;
import club.aurorapvp.modules.Wand;
import java.io.File;
import java.util.List;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuroraDuels extends JavaPlugin {
  public static Plugin INSTANCE;
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
    Wand.setup();
    Arena.setup();
    Queue.setup();

    // Setup configs
    Lang.reload();
    Config.reload();
    Lang.generateDefaults();
    Config.generateDefaults();

    // Register Listeners
    for (Object eventHandler : List.of(new Events(), new Arena(), new Queue())) {
      if (eventHandler instanceof Listener) {
        getServer().getPluginManager().registerEvents((Listener) eventHandler, this);
      }
    }
    List<Command> commandList = PluginCommandYamlParser.parse(INSTANCE);
    for (Command command : commandList) {
      getCommand(command.getName()).setExecutor(new Commands());
    }
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      new Placeholders().register();
    }
    if (Bukkit.getPluginManager().getPlugin("AuroraKits") != null) {
      auroraKitsInstalled = true;
    }

    getLogger().info(
        "Aurora Duels Loaded in " + Math.subtractExact(System.currentTimeMillis(), startTime) +
            "ms");
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getLogger().info("Aurora Duels Unloaded");
  }
}
