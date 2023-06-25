package club.aurorapvp.auroraduels;


import club.aurorapvp.auroraduels.commands.ArenaCommands;
import club.aurorapvp.auroraduels.commands.RematchCommand;
import club.aurorapvp.auroraduels.commands.duels.DuelBaseCommands;
import club.aurorapvp.auroraduels.commands.duels.StandardDuelCommands;
import club.aurorapvp.auroraduels.commands.duels.PartyDuelCommands;
import club.aurorapvp.auroraduels.commands.duels.TeamDuelCommands;
import club.aurorapvp.auroraduels.commands.findmatch.RankedCommands;
import club.aurorapvp.auroraduels.commands.findmatch.UnrankedCommands;
import club.aurorapvp.auroraduels.commands.PluginCommands;
import club.aurorapvp.auroraduels.commands.QueueCommands;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.aurorakits.modules.Kit;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import java.util.ArrayList;
import java.util.List;

public class Commands {
  public static PaperCommandManager MANAGER = new PaperCommandManager(AuroraDuels.INSTANCE);

  public static void init() {
    MANAGER.registerCommand(new ArenaCommands());
    MANAGER.registerCommand(new DuelBaseCommands());
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

    commandCompletions.registerCompletion("types", c -> List.of("-k", "-all"));

    if (AuroraDuels.isAuroraKitsInstalled()) {
      commandCompletions.registerCompletion("kits", c -> {
        List<String> kitNames = new ArrayList<>();

        for (Kit kit : Kit.getKits()) {
          kitNames.add(kit.getName());
        }

        return kitNames;
      });
    }
  }
}
