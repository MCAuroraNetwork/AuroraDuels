package club.aurorapvp;

import static club.aurorapvp.config.Lang.reload;
import static club.aurorapvp.wand.Wand.setup;

import club.aurorapvp.config.Config;
import club.aurorapvp.config.Lang;
import club.aurorapvp.listeners.Commands;
import club.aurorapvp.listeners.Events;
import java.io.File;
import java.io.IOException;
import java.util.List;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuroraDuels extends JavaPlugin {
  public static Plugin PLUGIN;
  public static File DATAFOLDER;
  public static PlainTextComponentSerializer COMPONENT_SERIALIZER;
  public static MiniMessage COMPONENT_DESERIALIZER;

  @Override
  public void onEnable() {
    // Register important variables
    setup();
    PLUGIN = this;
    DATAFOLDER = this.getDataFolder();
    COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();
    COMPONENT_DESERIALIZER = MiniMessage.miniMessage();

    // Setup configs
    try {
      reload();
      Config.reload();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Generate default values
    try {
      Lang.generateDefaults();
      Config.generateDefaults();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Register Listeners
    getServer().getPluginManager().registerEvents(new Events(), this);
    List<Command> commandList = PluginCommandYamlParser.parse(PLUGIN);
    for (Command command : commandList) {
      getCommand(command.getName()).setExecutor(new Commands());
    }

    getLogger().info("Aurora Duels Loaded");
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getLogger().info("Aurora Duels Unloaded");
  }
}
