package club.aurorapvp;

import club.aurorapvp.config.Config;
import club.aurorapvp.config.Lang;
import club.aurorapvp.listeners.Commands;
import club.aurorapvp.listeners.Events;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.wand.Wand;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.retrooper.packetevents.PacketEvents;

public final class AuroraDuels extends JavaPlugin {
  public static Plugin PLUGIN;
  public static File DATAFOLDER;
  public static PlainTextComponentSerializer COMPONENT_SERIALIZER;
  public static MiniMessage COMPONENT_DESERIALIZER;

  @Override
  public void onLoad() {
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
    //Are all listeners read only?
    PacketEvents.getAPI().getSettings().readOnlyListeners(true)
        .checkForUpdates(true)
        .bStats(true);
    PacketEvents.getAPI().load();
  }

  @Override
  public void onEnable() {
    // Register important variables
    Wand.setup();
    PLUGIN = this;
    DATAFOLDER = this.getDataFolder();
    COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();
    COMPONENT_DESERIALIZER = MiniMessage.miniMessage();

    // Setup configs
    try {
      Lang.reload();
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
    Events.wandEvent.registerListener(new Arena());
    List<Command> commandList = PluginCommandYamlParser.parse(PLUGIN);
    PacketEvents.getAPI().init();    for (Command command : commandList) {
      Objects.requireNonNull(getCommand(command.getName())).setExecutor(new Commands());
    }

    getLogger().info("Aurora Duels Loaded");
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getLogger().info("Aurora Duels Unloaded");
    PacketEvents.getAPI().terminate();
  }
}
