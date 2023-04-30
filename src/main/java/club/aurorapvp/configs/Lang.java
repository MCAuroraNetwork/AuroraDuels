package club.aurorapvp.configs;

import club.aurorapvp.AuroraDuels;
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
      if (get().getString((String) path).startsWith("~") &&
          get().getString((String) path).endsWith("~")) {
        PLACEHOLDERS.put((String) path, get().getString((String) path).replace("~", ""));
      }
    }

    DEFAULTS.put("prefix", "~<gradient:#FFAA00:#FF55FF><bold>AuroraDuels ><reset>~");
    DEFAULTS.put(
        "set-spawn1-and-spawn2",
        "prefix <gradient:#FFAA00:#FF55FF>Left click on the first spawn point of the arena and right click on the second spawn point");
    DEFAULTS.put("set-spawn1", "prefix <gradient:#FFAA00:#FF55FF>Left click to set first spawn");
    DEFAULTS.put("set-spawn2", "prefix <gradient:#FFAA00:#FF55FF>Right click to set second spawn");
    DEFAULTS.put("spawn1-set", "prefix <gradient:#FFAA00:#FF55FF>First spawn set!");
    DEFAULTS.put("spawn2-set", "prefix <gradient:#FFAA00:#FF55FF>Second spawn set!");
    DEFAULTS.put("spawns-set", "prefix <gradient:#FFAA00:#FF55FF>Arena spawnpoints set!");
    DEFAULTS.put("set-pos1-and-pos2",
        "prefix <gradient:#FFAA00:#FF55FF>Left click on the first corner of the arena and right click on the second corner");
    DEFAULTS.put("set-pos1", "prefix <gradient:#FFAA00:#FF55FF>Left click to set Position 1");
    DEFAULTS.put("set-pos2", "prefix <gradient:#FFAA00:#FF55FF>Right click to set Position 2");
    DEFAULTS.put("pos1-set", "prefix <gradient:#FFAA00:#FF55FF>Position 1 set!");
    DEFAULTS.put("pos2-set", "prefix <gradient:#FFAA00:#FF55FF>Position 2 set!");
    DEFAULTS.put("arena-created", "prefix <gradient:#FFAA00:#FF55FF>Arena created successfully!");
    DEFAULTS.put("arena-reset", "prefix <gradient:#FFAA00:#FF55FF>Arena reset successfully!");
    DEFAULTS.put("arena-deleted", "prefix <gradient:#FFAA00:#FF55FF>Arena deleted");
    DEFAULTS.put("arena-does-not-exist", "prefix <gradient:#FFAA00:#FF55FF>Arena does not exist!");
    DEFAULTS.put("arena-already-being-created",
        "prefix <gradient:#FFAA00:#FF55FF>There is already an Arena being created!");
    DEFAULTS.put("arena-already-exists",
        "prefix <gradient:#FFAA00:#FF55FF>That arena already exists!");
    DEFAULTS.put("unknown-opponent", "prefix <gradient:#FFAA00:#FF55FF>Cannot find opponent");
    DEFAULTS.put("unknown-duel",
        "prefix <gradient:#FFAA00:#FF55FF>Cannot find specified duel match");
    DEFAULTS.put("player-in-duel", "prefix <gradient:#FFAA00:#FF55FF>That player is in a duel!");
    DEFAULTS.put("duel-started", "<bold><gradient:#FFAA00:#FF55FF>Duel started!");
    DEFAULTS.put("entered-queue",
        "prefix <gradient:#FFAA00:#FF55FF>You have entered the duel queue");
    DEFAULTS.put("sent-request", "prefix <gradient:#FFAA00:#FF55FF>Request sent!");
    DEFAULTS.put("received-request",
        "prefix <gradient:#FFAA00:#FF55FF>You have received a request from %s use /duel accept to accept, or /duel deny to deny");
    DEFAULTS.put("request-timed-out", "prefix <gradient:#FFAA00:#FF55FF>Duel request timed out");
    DEFAULTS.put("no-requests", "prefix <gradient:#FFAA00:#FF55FF>No pending requests");
    DEFAULTS.put("request-denied", "prefix <gradient:#FFAA00:#FF55FF>Duel request denied!");
    DEFAULTS.put("request-accepted", "prefix <gradient:#FFAA00:#FF55FF>Duel request accepted!");
    DEFAULTS.put("you-won", "<gradient:#FFAA00:#FF55FF>You won the duel!");
    DEFAULTS.put("you-lost", "prefix <gradient:#FFAA00:#FF55FF>You lost the duel");
    DEFAULTS.put("draw", "prefix <gradient:#FFAA00:#FF55FF>The duel was a draw!");
    DEFAULTS.put("duel-time-expired", "prefix <gradient:#FFAA00:#FF55FF>The duel ran out of time");
    DEFAULTS.put("looking-match", "prefix <gradient:#FFAA00:#FF55FF>Searching for an opponent");
    DEFAULTS.put("match-found", "prefix <gradient:#FFAA00:#FF55FF>Opponent found!");
    DEFAULTS.put("queue-already-exists", "prefix <gradient:#FFAA00:#FF55FF>Queue already exists!");
    DEFAULTS.put("queue-being-created",
        "prefix <gradient:#FFAA00:#FF55FF>Queue already being created!");
    DEFAULTS.put("queue-created", "prefix <gradient:#FFAA00:#FF55FF>Queue successfully created!");
    DEFAULTS.put("queue-not-found", "prefix <gradient:#FFAA00:#FF55FF>Queue not found!");
    DEFAULTS.put("already-has-request",
        "prefix <gradient:#FFAA00:#FF55FF>You already have a pending request!");
    DEFAULTS.put("requested-player-has-request",
        "prefix <gradient:#FFAA00:#FF55FF>Requested player already has a pending request!");
    DEFAULTS.put("add-arena",
        "prefix <gradient:#FFAA00:#FF55FF>What arena would you like to add to the queue? Type its name into chat. You have added %s arenas so far");
    DEFAULTS.put("no-teleport",
        "prefix <gradient:#FFAA00:#FF55FF>You cannot teleport duing a duel!");
    DEFAULTS.put("duel-kit", "prefix <gradient:#FFAA00:#FF55FF>Duel kit set!");
    DEFAULTS.put("start-spectating",
        "prefix <gradient:#FFAA00:#FF55FF>You are now spectating!");
    DEFAULTS.put("stop-spectating",
        "prefix <gradient:#FFAA00:#FF55FF>You are no longer spectating");

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
      if (Objects.requireNonNull(get().getString((String) path)).startsWith("~") &&
          Objects.requireNonNull(get().getString((String) path)).endsWith("~")) {
        PLACEHOLDERS.put((String) path, Objects.requireNonNull(get().getString((String) path))
            .replace("~", ""));
      }
    }
  }

  public static String getString(String message) {
    String pathString = get().getString(message);
    for (String placeholder : PLACEHOLDERS.keySet()) {
      assert pathString != null;
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder,
            PLACEHOLDERS.get(placeholder));
      }
    }
    return pathString;
  }

  public static Component formatComponent(String message, Object... args) {
    String pathString = get().getString(message);
    assert pathString != null;
    for (String placeholder : PLACEHOLDERS.keySet()) {
      if (pathString.contains(placeholder)) {
        pathString = pathString.replace(placeholder,
            PLACEHOLDERS.get(placeholder));
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
        pathString = pathString.replace(placeholder,
            PLACEHOLDERS.get(placeholder));
      }
    }
    return MiniMessage.miniMessage().deserialize(pathString);
  }

  public static YamlConfiguration get() {
    return lang;
  }

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
