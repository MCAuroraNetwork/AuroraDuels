package club.aurorapvp.auroraduels.config;

import club.aurorapvp.auroraduels.AuroraDuels;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {
  private static final HashMap<String, String> PLACEHOLDERS = new HashMap<>();
  private static final File FILE = new File(AuroraDuels.INSTANCE.getDataFolder(), "lang.yml");
  private static YamlConfiguration lang;

  public static void init() {
    reload();
    generateDefaults();
  }

  public static void generateDefaults() {
    final HashMap<String, String> DEFAULTS = new HashMap<>();

    for (Object path : get().getKeys(false).toArray()) {
      if (Objects.requireNonNull(get().getString((String) path)).startsWith("~")
          && Objects.requireNonNull(get().getString((String) path)).endsWith("~")) {
        PLACEHOLDERS.put(
            (String) path, Objects.requireNonNull(get().getString((String) path)).replace("~", ""));
      }
    }

    DEFAULTS.put("prefix", "~<gradient:#FFAA00:#FF55FF><bold>AuroraDuels ><reset>~");
    DEFAULTS.put("set-spawn1", "prefix <aqua>Set first spawn: Left click");
    DEFAULTS.put("set-spawn2", "prefix <aqua>Set second spawn: Right click");
    DEFAULTS.put("spawn1-set", "prefix <aqua>Spawn 1 set!");
    DEFAULTS.put("spawn2-set", "prefix <aqua>Spawn 2 set!");
    DEFAULTS.put("spawns-set", "prefix <aqua>Spawns set!");
    DEFAULTS.put("set-pos1", "prefix <aqua>Set Pos 1: Left click");
    DEFAULTS.put("set-pos2", "prefix <aqua>Set Pos 2: Right click");
    DEFAULTS.put("pos1-set", "prefix <aqua>Pos 1 set!");
    DEFAULTS.put("pos2-set", "prefix <aqua>Pos 2 set!");
    DEFAULTS.put("arena-created", "prefix <aqua>Arena created!");
    DEFAULTS.put("arena-reset", "prefix <aqua>Arena reset!");
    DEFAULTS.put("arena-deleted", "prefix <aqua>Arena deleted");
    DEFAULTS.put("arena-does-not-exist", "prefix <aqua>No arena");
    DEFAULTS.put("arena-already-being-created", "prefix <aqua>Arena creation in progress");
    DEFAULTS.put("arena-already-exists", "prefix <aqua>Arena exists");
    DEFAULTS.put("unknown-opponent", "prefix <aqua>Opponent not found");
    DEFAULTS.put("unknown-duel", "prefix <aqua>Duel not found");
    DEFAULTS.put("player-in-duel", "prefix <aqua>Player in duel");
    DEFAULTS.put("duel-started", "<bold><aqua>Duel started!");
    DEFAULTS.put("even-rounds", "prefix <aqua>Odd rounds required for duel");
    DEFAULTS.put("entered-queue", "prefix <aqua>In duel queue");
    DEFAULTS.put("sent-request", "prefix <aqua>Request sent");
    DEFAULTS.put(
        "received-request", "prefix <aqua>Request from %s. Use /duel accept or /duel deny");
    DEFAULTS.put("request-timed-out", "prefix <aqua>Request timed out");
    DEFAULTS.put("no-requests", "prefix <aqua>No requests");
    DEFAULTS.put("request-denied", "prefix <aqua>Request denied");
    DEFAULTS.put("request-accepted", "prefix <aqua>Request accepted");
    DEFAULTS.put("you-won", "<aqua>You won!");
    DEFAULTS.put("you-lost", "<aqua>You lost");
    DEFAULTS.put("rounds-remaining", "<aqua>Round %1$s of %2$s");
    DEFAULTS.put("draw", "prefix <aqua>Draw!");
    DEFAULTS.put("no-leaving-arena", "prefix <aqua>Can't leave arena");
    DEFAULTS.put("duel-time-expired", "prefix <aqua>Duel time expired");
    DEFAULTS.put("looking-match", "prefix <aqua>Searching for opponent");
    DEFAULTS.put("not-looking-match", "prefix <aqua>Stopped searching for an opponent");
    DEFAULTS.put("match-found", "prefix <aqua>Opponent found");
    DEFAULTS.put("in-queue", "prefix <aqua>Player is in a queue");
    DEFAULTS.put("queue-already-exists", "prefix <aqua>Queue exists");
    DEFAULTS.put("queue-being-created", "prefix <aqua>Queue creation in progress");
    DEFAULTS.put("queue-invalid-item", "prefix <aqua>Invalid queue item");
    DEFAULTS.put("queue-created", "prefix <aqua>Queue created");
    DEFAULTS.put("queue-not-found", "prefix <aqua>Queue not found");
    DEFAULTS.put("you-have-request", "prefix <aqua>You have a pending request");
    DEFAULTS.put("already-has-request", "prefix <aqua>You have a request pending to this player");
    DEFAULTS.put("add-arena", "prefix <aqua>Type the name of the arena you want to add arena to queue. Added %s so far");
    DEFAULTS.put("no-teleport", "prefix <aqua>Can't teleport during duel");
    DEFAULTS.put("commands-disabled", "prefix <aqua>Commands disabled during duel");
    DEFAULTS.put("duel-kit", "prefix <aqua>Duel kit set");
    DEFAULTS.put("start-spectating", "prefix <aqua>Spectating");
    DEFAULTS.put("stop-spectating", "prefix <aqua>Stopped spectating");
    DEFAULTS.put("no-last-duel", "prefix <aqua>No previous duel");
    DEFAULTS.put("unknown-duel-command", "prefix <aqua>Unknown Duel Command! Use /help");
    DEFAULTS.put(
        "unknown-matchmaking-command", "prefix <aqua>Unknown Matchmaking Command! Use /help");
    DEFAULTS.put("unknown-arena-command", "prefix <aqua>Unknown Arena Command! Use /help");
    DEFAULTS.put("unknown-queue-command", "prefix <aqua>Unknown Queue Command! Use /help");
    DEFAULTS.put("queue-menu", "<aqua><bold>Select Duel Type");
    DEFAULTS.put("duel-kit-gui", "<aqua><bold>Select Duel Kit");
    DEFAULTS.put("next-page", "<aqua>Next");
    DEFAULTS.put("previous-page", "<aqua>Previous");
    DEFAULTS.put("no-kit", "<aqua>No kit");
    DEFAULTS.put("kit-invalid-name", "prefix <aqua>Invalid kit name");
    DEFAULTS.put("duel-lobby-set", "prefix <aqua>Duel lobby set");
    DEFAULTS.put("reloaded", "prefix <aqua>Reloaded");
    DEFAULTS.put(
        "opponent-bossbar",
        "<aqua><bold>%1$s <reset><red>| %2$s❤ |<reset><aqua> %3$sm |<reset><yellow> %4$sms");
    DEFAULTS.put(
        "help-command",
        """
        prefix <gradient:#FFAA00:#FF55FF>All Commands:\s
        <blue><bold>/duel [player]: <reset><yellow>Sends a duel request to a player\s
        <blue><bold>/ranked [name]: <reset><yellow>Joins a ranked queue, or the default one if no name is given\s
        <blue><bold>/unranked [name]: <reset><yellow>Joins a ranked queue, or the default one if no name is given\s
        <blue><bold>/rematch: <reset><yellow>Sends a duel request identical to the last one sent to all previous duel members\s
        """);

    for (String path : DEFAULTS.keySet()) {
      if (!get().contains(path) || get().getString(path) == null) {
        get().set(path, DEFAULTS.get(path));
      }
    }

    try {
      get().save(FILE);
    } catch (IOException e) {
      AuroraDuels.INSTANCE.getLogger().severe("Failed to save lang file");
    }

    for (Object path : get().getKeys(false).toArray()) {
      if (Objects.requireNonNull(get().getString((String) path)).startsWith("~")
          && Objects.requireNonNull(get().getString((String) path)).endsWith("~")) {
        PLACEHOLDERS.put(
            (String) path, Objects.requireNonNull(get().getString((String) path)).replace("~", ""));
      }
    }
  }

  public static String getString(String message) {
    String pathString = get().getString(message);
    for (String placeholder : PLACEHOLDERS.keySet()) {
      assert pathString != null;
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder, PLACEHOLDERS.get(placeholder));
      }
    }
    return pathString;
  }

  public static Component formatComponent(String message, Object... args) {
    String pathString = get().getString(message);
    assert pathString != null;
    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder, PLACEHOLDERS.get(placeholder));
      }
    }

    pathString = String.format(pathString, args);

    return MiniMessage.miniMessage().deserialize(pathString);
  }

  public static Component getComponent(String message) {
    String pathString = get().getString(message);
    assert pathString != null;

    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder, PLACEHOLDERS.get(placeholder));
      }
    }
    return MiniMessage.miniMessage().deserialize(pathString);
  }

  public static YamlConfiguration get() {
    return lang;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static void reload() {
    if (!FILE.exists()) {
      try {
        FILE.getParentFile().mkdirs();
        FILE.createNewFile();
      } catch (IOException e) {
        AuroraDuels.INSTANCE.getLogger().severe("Failed to generate lang file");
      }
    }
    lang = YamlConfiguration.loadConfiguration(FILE);
    AuroraDuels.INSTANCE.getLogger().info("Lang reloaded!");
  }
}
