package club.aurorapvp;

import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.listeners.Commands;
import club.aurorapvp.listeners.Events;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Duel;
import club.aurorapvp.modules.Placeholders;
import club.aurorapvp.modules.Queue;
import club.aurorapvp.modules.Wand;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuroraDuels extends JavaPlugin {
  public static Plugin PLUGIN;
  public static File DATA_FOLDER;
  public static PlainTextComponentSerializer COMPONENT_SERIALIZER;
  public static MiniMessage COMPONENT_DESERIALIZER;

  @Override
  public void onEnable() {
    long startTime = System.currentTimeMillis();

    // Register important variables
    PLUGIN = this;
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
    getServer().getPluginManager().registerEvents(new Events(), this);
    Events.wandEvent.registerListener(new Arena(null));
    List<Command> commandList = PluginCommandYamlParser.parse(PLUGIN);
    for (Command command : commandList) {
      getCommand(command.getName()).setExecutor(new Commands());
    }
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      new Placeholders().register();
    }

    getLogger().info("Aurora Duels Loaded in " + Math.subtractExact(System.currentTimeMillis(), startTime) + "ms");
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getLogger().info("Aurora Duels Unloaded");
  }
}
