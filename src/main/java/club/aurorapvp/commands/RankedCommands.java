package club.aurorapvp.commands;

import club.aurorapvp.modules.Queue;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import java.util.Objects;
import org.bukkit.entity.Player;

@CommandAlias("ranked")
public class RankedCommands extends BaseCommand {
  @Default
  @CommandCompletion("@queues")
  @Syntax("[queue]")
  @Description("Joins a provided queue")
  public void onFindMatch(Player p, @Optional String name) {
    Queue.findMatch(p, Objects.requireNonNullElse(name, "Default"), true);
  }

  @CatchUnknown
  @Subcommand("help")
  public void onUnknown() {

  }
}
