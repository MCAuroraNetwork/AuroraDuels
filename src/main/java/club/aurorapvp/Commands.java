package club.aurorapvp;


import club.aurorapvp.commands.ArenaCommands;
import club.aurorapvp.commands.RematchCommand;
import club.aurorapvp.commands.duels.StandardDuelCommands;
import club.aurorapvp.commands.duels.PartyDuelCommands;
import club.aurorapvp.commands.duels.TeamDuelCommands;
import club.aurorapvp.commands.findmatch.RankedCommands;
import club.aurorapvp.commands.findmatch.UnrankedCommands;
import club.aurorapvp.commands.PluginCommands;
import club.aurorapvp.commands.QueueCommands;
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
    MANAGER.registerCommand(new ArenaCommands());
    MANAGER.registerCommand(new StandardDuelCommands());
    MANAGER.registerCommand(new TeamDuelCommands());
    MANAGER.registerCommand(new UnrankedCommands());
    MANAGER.registerCommand(new RankedCommands());
    MANAGER.registerCommand(new PartyDuelCommands());
    MANAGER.registerCommand(new PluginCommands());
    MANAGER.registerCommand(new QueueCommands());
    MANAGER.registerCommand(new RematchCommand());

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
