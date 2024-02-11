package club.aurorapvp.auroraduels.config;

import club.aurorapvp.auroraduels.AuroraDuels;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

  private final File FILE = new File(AuroraDuels.getInstance().getDataFolder(), "config.yml");
  private YamlConfiguration config;

  public Config() {
    this.reload();
    this.generateDefaults();
  }

  public void generateDefaults() {
    final HashMap<String, Object> DEFAULTS = new HashMap<>();

    DEFAULTS.put("duels.requests.request-timeout", 30000);
    DEFAULTS.put("optional-plugins.aurorakits-compatibility", true);
    DEFAULTS.put("optional-plugins.aurorawarps-compatibility", true);
    DEFAULTS.put("optional-plugins.auroracombat-compatibility.enabled", true);
    DEFAULTS.put("optional-plugins.auroracombat-compatibility.always-enable-default-elo", true);
    DEFAULTS.put("optional-plugins.discordsrv-compatibility", true);
    DEFAULTS.put("duels.enable-teleports", true);
    DEFAULTS.put("duels.commands.allow-commands", false);
    DEFAULTS.put("duels.commands.whitelisted", new ArrayList<>(List.of("kill")));
    DEFAULTS.put("duels.max-duration", 600000);
    DEFAULTS.put("requests.expiry-time", 600000);
    DEFAULTS.put("queues.enable-default-queue", true);

    for (String path : DEFAULTS.keySet()) {
      if (!getYaml().isSet(path) || getYaml().getString(path) == null) {
        getYaml().set(path, DEFAULTS.get(path));
      }
    }

    try {
      getYaml().save(FILE);
    } catch (IOException e) {
      AuroraDuels.getInstance().getLogger().log(Level.SEVERE, "Failed to save config file", e);
    }
  }

  public YamlConfiguration getYaml() {
    return config;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void reload() {
    if (!FILE.exists()) {
      try {
        FILE.getParentFile().mkdirs();
        FILE.createNewFile();

        config = YamlConfiguration.loadConfiguration(FILE);

        this.generateDefaults();
      } catch (IOException e) {
        AuroraDuels.getInstance().getLogger()
            .log(Level.SEVERE, "Failed to generate config file", e);
      }
    }
    config = YamlConfiguration.loadConfiguration(FILE);
    AuroraDuels.getInstance().getLogger().info("Config reloaded!");
  }
}
