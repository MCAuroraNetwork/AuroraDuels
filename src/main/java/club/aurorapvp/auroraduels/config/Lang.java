package club.aurorapvp.auroraduels.config;

import club.aurorapvp.auroraduels.AuroraDuels;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {

  private final HashMap<String, String> PLACEHOLDERS = new HashMap<>();
  private final File FILE = new File(AuroraDuels.getInstance().getDataFolder(), "lang.yml");
  private YamlConfiguration lang;

  public Lang() {
    this.reload();
    this.generateDefaults();
  }

  public void generateDefaults() {
    final HashMap<String, String> DEFAULTS = new HashMap<>();

    for (var path : getYaml().getKeys(false).toArray()) {
      if (Objects.requireNonNull(getYaml().getString((String) path)).startsWith("~")
          && Objects.requireNonNull(getYaml().getString((String) path)).endsWith("~")) {
        PLACEHOLDERS.put(
            (String) path,
            Objects.requireNonNull(getYaml().getString((String) path)).replace("~", ""));
      }
    }

    DEFAULTS.put("prefix", "~<gradient:#FFAA00:#FF55FF><bold>AuroraDuels ><reset>~");
    DEFAULTS.put("set-spawn1", "prefix <yellow>Set first spawn: Left click");
    DEFAULTS.put("set-spawn2", "prefix <yellow>Set second spawn: Right click");
    DEFAULTS.put("spawn1-set", "prefix <yellow>Spawn 1 set!");
    DEFAULTS.put("spawn2-set", "prefix <yellow>Spawn 2 set!");
    DEFAULTS.put("spawns-set", "prefix <yellow>Spawns set!");
    DEFAULTS.put("set-pos1", "prefix <yellow>Set Pos 1: Left click");
    DEFAULTS.put("set-pos2", "prefix <yellow>Set Pos 2: Right click");
    DEFAULTS.put("pos1-set", "prefix <yellow>Pos 1 set!");
    DEFAULTS.put("pos2-set", "prefix <yellow>Pos 2 set!");
    DEFAULTS.put("arena-created", "prefix <green>Arena created!");
    DEFAULTS.put("arena-reset", "prefix <yellow>Arena reset!");
    DEFAULTS.put("arena-deleted", "prefix <red>Arena deleted");
    DEFAULTS.put("arena-does-not-exist", "prefix <red>No such arena");
    DEFAULTS.put("arena-already-being-created", "prefix <yellow>Arena creation in progress");
    DEFAULTS.put("arena-already-exists", "prefix <yellow>Arena exists");
    DEFAULTS.put("unknown-opponent", "prefix <red>Opponent not found");
    DEFAULTS.put("unknown-duel", "prefix <red>Duel not found");
    DEFAULTS.put("player-in-duel", "prefix <red>Player in duel");
    DEFAULTS.put("duel-started", "<bold><green>Duel started!");
    DEFAULTS.put("even-rounds", "prefix <red>Odd rounds required for duel");
    DEFAULTS.put("entered-queue", "prefix <green>You have entered the duel queue");
    DEFAULTS.put("sent-request", "prefix <green>Request sent successfully!");
    DEFAULTS.put(
        "received-request",
        "prefix <yellow>Request from <bold>%s<reset><yellow>. Use <bold>/duel accept<reset><yellow> or <bold>/duel deny<reset><yellow>");
    DEFAULTS.put("request-timed-out", "prefix <yellow>Request timed out");
    DEFAULTS.put("no-requests", "prefix <yellow>No requests");
    DEFAULTS.put("request-denied", "prefix <red>Request denied");
    DEFAULTS.put("request-accepted", "prefix <green>Request accepted!");
    DEFAULTS.put("you-won", "<green>You won!");
    DEFAULTS.put("you-lost", "<red>You lost");
    DEFAULTS.put("duel-won",
        "<bold><yellow>%1$s<reset><yellow> won a duel against <bold>%2$s<reset><yellow>!");
    DEFAULTS.put("discord-duel-won", "%1$s won a duel against %2$s!");
    DEFAULTS.put("rounds-remaining", "<yellow>Round %1$s of %2$s /n<red>%3$s<yellow - <blue>%4$s");
    DEFAULTS.put("draw", "prefix <yellow>Draw!");
    DEFAULTS.put("no-leaving-arena", "prefix <red>Can't leave arena");
    DEFAULTS.put("duel-time-expired", "prefix <red>Duel time expired");
    DEFAULTS.put("looking-match", "prefix <yellow>Searching for opponent");
    DEFAULTS.put("not-looking-match", "prefix <yellow>Stopped searching for an opponent");
    DEFAULTS.put("match-found", "prefix <green>Opponent found!");
    DEFAULTS.put("in-queue", "prefix <yellow>Player is already in a queue");
    DEFAULTS.put("queue-already-exists", "prefix <yellow>Queue exists");
    DEFAULTS.put("queue-being-created", "prefix <yellow>Queue creation in progress");
    DEFAULTS.put("queue-invalid-item", "prefix <red>Invalid queue item");
    DEFAULTS.put("queue-created", "prefix <green>Queue created");
    DEFAULTS.put("queue-not-found", "prefix <red>Queue not found");
    DEFAULTS.put("you-have-request", "prefix <red>You already have a pending request");
    DEFAULTS.put("already-has-request",
        "prefix <red>You already have a request pending to this player");
    DEFAULTS.put(
        "add-arena",
        "prefix <yellow>Type the name of the arena you want to add arena to queue. Added %s so far");
    DEFAULTS.put("no-teleport", "prefix <red>Can't teleport during a duel");
    DEFAULTS.put("commands-disabled", "prefix <red>Commands disabled during duel");
    DEFAULTS.put("duel-kit", "prefix <green>Duel kit set!");
    DEFAULTS.put("start-spectating", "prefix <yellow>Spectating!");
    DEFAULTS.put("stop-spectating", "prefix <yellow>Stopped spectating");
    DEFAULTS.put("no-last-duel", "prefix <red>No previous duel");
    DEFAULTS.put("queue-menu", "<yellow><bold>Select Duel Type");
    DEFAULTS.put("duel-kit-gui", "<yellow><bold>Select Duel Kit");
    DEFAULTS.put("next-page", "<yellow><bold>Next");
    DEFAULTS.put("previous-page", "<yellow><bold>Previous");
    DEFAULTS.put("kit-invalid-name", "prefix <red>Invalid kit name");
    DEFAULTS.put("duel-lobby-set", "prefix <green>Duel lobby set");
    DEFAULTS.put("new-win", "prefix <bold><green>+1 Win");
    DEFAULTS.put("reloaded", "prefix <green>Reloaded");
    DEFAULTS.put(
        "help-command",
        """
            prefix <yellow><bold>All Commands:
                    
                    
            <blue><bold>/duel [player]: <reset><yellow>Sends a duel request to a player
            
            <blue><bold>/ranked or /unranked [name]: <reset><yellow>Joins a ranked or unranked queue, or the default one if no name is given
            
            <blue><bold>/rematch: <reset><yellow>Sends a duel request identical to the last one sent to all previous duel members
            """);

    for (String path : DEFAULTS.keySet()) {
      if (!getYaml().contains(path) || getYaml().getString(path) == null) {
        getYaml().set(path, DEFAULTS.get(path));
      }
    }

    try {
      getYaml().save(FILE);
    } catch (IOException e) {
      AuroraDuels.getInstance().getLogger().log(Level.SEVERE, "Failed to save lang file", e);
    }

    for (var path : getYaml().getKeys(false).toArray()) {
      if (Objects.requireNonNull(getYaml().getString((String) path)).startsWith("~")
          && Objects.requireNonNull(getYaml().getString((String) path)).endsWith("~")) {
        PLACEHOLDERS.put(
            (String) path,
            Objects.requireNonNull(getYaml().getString((String) path)).replace("~", ""));
      }
    }
  }

  public String getString(String message) {
    String pathString = getYaml().getString(message);
    for (String placeholder : PLACEHOLDERS.keySet()) {
      assert pathString != null;
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder, PLACEHOLDERS.get(placeholder));
      }
    }
    return pathString;
  }

  public Component formatComponent(String message, Object... args) {
    String pathString = getYaml().getString(message);
    assert pathString != null;
    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder, PLACEHOLDERS.get(placeholder));
      }
    }

    pathString = String.format(pathString, args);

    return MiniMessage.miniMessage().deserialize(pathString);
  }

  public Component getComponent(String message) {
    String pathString = getYaml().getString(message);
    assert pathString != null;

    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder, PLACEHOLDERS.get(placeholder));
      }
    }
    return MiniMessage.miniMessage().deserialize(pathString);
  }

  public YamlConfiguration getYaml() {
    return lang;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void reload() {
    if (!FILE.exists()) {
      try {
        FILE.getParentFile().mkdirs();
        FILE.createNewFile();

        lang = YamlConfiguration.loadConfiguration(FILE);

        this.generateDefaults();
      } catch (IOException e) {
        AuroraDuels.getInstance().getLogger().log(Level.SEVERE, "Failed to generate lang file", e);
      }
    }
    lang = YamlConfiguration.loadConfiguration(FILE);
    AuroraDuels.getInstance().getLogger().info("Lang reloaded!");
  }
}
