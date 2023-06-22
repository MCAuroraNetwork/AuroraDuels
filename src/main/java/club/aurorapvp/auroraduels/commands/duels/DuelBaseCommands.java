package club.aurorapvp.auroraduels.commands.duels;

import club.aurorapvp.auroraduels.configs.Lang;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.auroraduels.modules.requests.Request;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("duel|teamduel|partyduel")
public class DuelBaseCommands extends BaseCommand {
  @Subcommand("accept")
  @Description("Accepts the duel request")
  @SuppressWarnings("unused")
  public void onAccept(Player p) {
    Request.acceptDuelRequest(p);
  }

  @Subcommand("deny")
  @Description("Denies the duel request")
  @SuppressWarnings("unused")
  public void onDeny(Player p) {
    Request.denyDuelRequest(p);
  }

  @Subcommand("spectate")
  @CommandCompletion("@players")
  @Syntax("[player]")
  @Description("Spectates the duel of a given player")
  @SuppressWarnings("unused")
  public void onSetSpectator(Player p, String player) {
    Duel duel = Duel.getDuel(Bukkit.getPlayer(player));

    if (!Duel.inDuel(p) && duel != null) {
      duel.setSpectator(p, true);
    } else {
      p.sendMessage(Lang.getComponent("unknown-duel"));
    }
  }

  @CatchUnknown
  @Subcommand("help")
  @SuppressWarnings("unused")
  public void onUnknown(Player p) {
    p.sendMessage(Lang.getComponent("unknown-duel-command"));
  }
}