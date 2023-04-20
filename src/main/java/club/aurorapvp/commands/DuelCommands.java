package club.aurorapvp.commands;

import club.aurorapvp.modules.Duel;
import club.aurorapvp.modules.Kit;
import club.aurorapvp.modules.Request;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("duel")
public class DuelCommands extends BaseCommand {

  @Subcommand("accept")
  @Description("Accepts the duel request")
  public void onAccept(Player p) {
    Request.acceptDuelRequest(p);
  }

  @Subcommand("deny")
  @Description("Denies the duel request")
  public void onDeny(Player p) {
    Request.denyDuelRequest(p);
  }

  @Subcommand("kit")
  @Description("Sets the duel kit")
  public void onKitSet(Player p, @Optional String kitName) {
    if (kitName != null && Kit.getKit(p, kitName) != null) {
      Duel.setDuelKit(p, kitName);
    } else {
      Duel.setDuelKit(p, Kit.getLastUsedKit(p).getName());
    }
  }

  @Subcommand("spectate")
  @CommandCompletion("@players")
  @Syntax("[player]")
  @Description("Spectates the duel of a given player")
  public void onSetSpectator(Player p, String player) {
    Duel.getDuel(Bukkit.getPlayer(player)).setSpectator(p, true);
  }

  @Default
  @CommandCompletion("@players @queues")
  @Syntax("[player] [queue]")
  @Description("Sends a duel request to another player")
  public void onNewRquest(Player p, @Optional String[] args) {
    if (args.length > 1) {
      new Request(args[1], p, Bukkit.getPlayer(args[0]));
    } else {
      new Request("Default", p, Bukkit.getPlayer(args[0]));
    }
  }

  @CatchUnknown
  @Subcommand("help")
  public void onUnknown() {

  }
}
