package club.aurorapvp.commands;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.commands.arenas.ArenaList;
import club.aurorapvp.commands.arenas.CreateArena;
import club.aurorapvp.commands.arenas.DeleteArena;
import club.aurorapvp.commands.arenas.ResetArena;
import club.aurorapvp.commands.duels.DuelConfirmation;
import club.aurorapvp.commands.duels.DuelRequest;
import club.aurorapvp.commands.duels.FindMatch;
import club.aurorapvp.commands.plugin.Reload;
import club.aurorapvp.commands.queues.CreateQueue;

public class Commands {
  public Commands() {
    AuroraDuels.INSTANCE.getCommand("createqueue").setExecutor(new CreateQueue());
    AuroraDuels.INSTANCE.getCommand("auroraduels").setExecutor(new Reload());
    AuroraDuels.INSTANCE.getCommand("findmatch").setExecutor(new FindMatch());
    AuroraDuels.INSTANCE.getCommand("duel").setExecutor(new DuelRequest());
    AuroraDuels.INSTANCE.getCommand("duel").setExecutor(new DuelConfirmation());
    AuroraDuels.INSTANCE.getCommand("resetarena").setExecutor(new ResetArena());
    AuroraDuels.INSTANCE.getCommand("deletearena").setExecutor(new DeleteArena());
    AuroraDuels.INSTANCE.getCommand("createarena").setExecutor(new CreateArena());
    AuroraDuels.INSTANCE.getCommand("arenalist").setExecutor(new ArenaList());
  }
}
