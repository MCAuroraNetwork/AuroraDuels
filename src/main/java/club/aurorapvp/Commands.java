package club.aurorapvp;


import club.aurorapvp.AuroraDuels;
import club.aurorapvp.commands.PluginCommands;
import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Queue;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import java.util.ArrayList;
import java.util.List;

public class Commands {
  public static PaperCommandManager MANAGER = new PaperCommandManager(AuroraDuels.INSTANCE);

  public static void init() {
    MANAGER = new PaperCommandManager(AuroraDuels.INSTANCE);

    MANAGER.registerCommand(new PluginCommands());
  }

  public static void registerCompletions() {
    CommandCompletions<BukkitCommandCompletionContext> commandCompletions =
        MANAGER.getCommandCompletions();

    commandCompletions.registerCompletion("queues", c -> {
      List<String> queueNames = new ArrayList<>();

      for (Queue queue : Queue.getQueues()) {
        queueNames.add(queue.getName());
      }

      return queueNames;
    });

    commandCompletions.registerCompletion("arenas", c -> {
      List<String> arenaNames = new ArrayList<>();

      for (Arena arena : Arena.getArenas()) {
        arenaNames.add(arena.getName());
      }

      return arenaNames;
    });
  }
}
