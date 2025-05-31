package club.aurorapvp.auroraduels.commands;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.commands.duels.RematchCommand;
import club.aurorapvp.auroraduels.commands.duels.matchmaking.DuelsCommand;
import club.aurorapvp.auroraduels.commands.duels.matchmaking.RankedCommands;
import club.aurorapvp.auroraduels.commands.duels.matchmaking.UnrankedCommands;
import club.aurorapvp.auroraduels.commands.duels.requests.DuelBaseCommands;
import club.aurorapvp.auroraduels.commands.duels.requests.FFADuelCommands;
import club.aurorapvp.auroraduels.commands.duels.requests.StandardDuelCommands;
import club.aurorapvp.auroraduels.commands.duels.requests.TeamDuelCommands;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.DuelQueue;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {

  public static PaperCommandManager MANAGER = new PaperCommandManager(AuroraDuels.getInstance());

  public static void init() {
    MANAGER.registerCommand(new ArenaCommands());
    MANAGER.registerCommand(new DuelBaseCommands());
    MANAGER.registerCommand(new StandardDuelCommands());
    MANAGER.registerCommand(new TeamDuelCommands());
    MANAGER.registerCommand(new UnrankedCommands());
    MANAGER.registerCommand(new RankedCommands());
    MANAGER.registerCommand(new PluginCommands());
    MANAGER.registerCommand(new QueueCommands());
    MANAGER.registerCommand(new RematchCommand());
    MANAGER.registerCommand(new FFADuelCommands());
    MANAGER.registerCommand(new DuelsCommand());

    CommandCompletions<BukkitCommandCompletionContext> commandCompletions =
        MANAGER.getCommandCompletions();

    commandCompletions.registerCompletion(
        "queues",
        c -> {
          List<String> queueNames = new ArrayList<>();

          for (DuelQueue duelQueue : DuelQueue.getQueues()) {
            queueNames.add(duelQueue.getName());
          }

          return queueNames;
        });

    commandCompletions.registerCompletion(
        "arenas",
        c -> {
          List<String> arenaNames = new ArrayList<>();

          for (Arena arena : Arena.getArenas()) {
            arenaNames.add(arena.getName());
          }

          return arenaNames;
        });

    commandCompletions.registerCompletion(
        "all-int", c -> List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"));

    commandCompletions.registerCompletion(
        "odd-int", c -> List.of("1", "3", "5", "7", "9"));

    if (AuroraDuels.getInstance().isAuroraKitsInstalled()) {
      commandCompletions.registerCompletion(
          "kits",
          c -> {
            List<String> kitNames = new ArrayList<>();

            for (Kit kit : Kit.getKits()) {
              kitNames.add(kit.getName());
            }

            return kitNames;
          });
    }
  }
}
