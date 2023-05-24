package club.aurorapvp.commands.duels;

import club.aurorapvp.modules.requests.Request;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;

public class DuelBaseCommand extends BaseCommand {

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
}