package club.aurorapvp.listeners;

import club.aurorapvp.modules.Arena;
import club.aurorapvp.modules.Queue;
import club.aurorapvp.modules.Request;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor, Listener {
  private Player p;

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (sender instanceof Player) {
      p = (Player) sender;
    }

    switch (command.getName()) {
      case "auroraduels" -> {
        // TODO
        if (args[0].equals("reload")) {

        }
      }
      case "duel" -> {
        if (args[0].equals("accept")) {
          Request.acceptDuelRequest(p);
        } else if (args[0].equals("deny")) {
          Request.denyDuelRequest(p);
        } else {
          if (args.length > 1) {
            new Request(args[1], p, Bukkit.getPlayer(args[0]));
          } else {
            new Request("Default", p, Bukkit.getPlayer(args[0]));
          }
        }
      }
      case "createarena" -> new Arena(args[0]).create(p);
      case "resetarena" -> Arena.getArena(args[0]).reset(p);
      case "deletearena" -> Arena.getArena(args[0]).delete(p);
      case "arenalist" -> {
        // TODO clean up or remove
        StringBuilder str = new StringBuilder("Arenas:");
        for (Arena arena : Arena.getArenas()) {
          str.append("\n").append(arena.getName());
        }
        p.sendMessage(Component.text(str.toString()));
      }
      case "findmatch" -> Queue.findMatch(p, args[0]);
      case "createqueue" -> {
        if (args.length > 1) {
          new Queue(args[0]).create(p, args[1]);
        } else {
          new Queue(args[0]).create(p);
        }
      }
    }
    return true;
  }

  @EventHandler
  public void onTabComplete(TabCompleteEvent event) {
    String[] args = event.getBuffer().split(" ");

    if (args[0].equals("aurorakits")) {
      event.setCompletions(Collections.singletonList("reload"));
    }

    if (args[0].equals("deletearena")) {
      List<String> options = new ArrayList<>();

      for (Arena arena : Arena.getArenas()) {
        options.add(arena.getName());
      }

      event.setCompletions(options);
    }

    if (args[0].equals("findmatch")) {
      List<String> options = new ArrayList<>();

      for (Queue queue : Queue.getQueues()) {
        options.add(queue.getName());
      }

      event.setCompletions(options);
    }

    if (args.length == 2) {
      if (args[0].equals("createqueue")) {
        List<String> options = new ArrayList<>();

        for (Arena arena : Arena.getArenas()) {
          options.add(arena.getName());
        }

        event.setCompletions(options);
      }
    }

  }
}
