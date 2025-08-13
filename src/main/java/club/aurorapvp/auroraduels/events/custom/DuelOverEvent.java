package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.aurorachat.modules.ChatGroup;
import club.aurorapvp.auroracombat.modules.CombatTag;
import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public abstract class DuelOverEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  protected final Duel duel;
  protected final Arena arena;

  public DuelOverEvent(Duel duel, Arena arena) {
    this.duel = duel;
    this.arena = arena;

    new Timer()
            .schedule(
                    new TimerTask() {
                      @Override
                      public void run() {
                        for (Player player : duel.getPlayers()) {
                          player.teleportAsync(duel.getRespawnLocation());
                        }
                        arena.reset();
                        arena.setOccupied(false);
                        this.cancel();
                      }
                    },
                    3000);

    for (Player player : duel.getPlayers()) {
      Duel.setLastDuel(player, duel);

      player.setGlowing(false);

      player.sendMessage(
              AuroraDuels.getInstance()
                      .getLang()
                      .getComponentCommand("rematch", "Click to Request Rematch", "/rematch"));
    }

    if (AuroraDuels.getInstance().isAuroraKitsInstalled()) {
      for (Player player : duel.getPlayers()) {
        Kit.refillKit(player);
      }
    }

    if (!AuroraDuels.getInstance().isAuroraCombatInstalled()) {
      return;
    }

    for (Player player : duel.getPlayers()) {
      Rating.getRating("overall").setPlayerEnabled(player, true);

      CombatTag.setTaggable(player, true);
    }

    for (Player player : duel.getPlayers()) {
      ChatGroup group = ChatGroup.getChatGroup(player);
      Duel.ChatState state = duel.getSavedChatStates().get(player);
      if (state != null) {
        group.setMode(state.mode());
        group.setChatTarget(state.target());
        player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("duel-chat-reverted"));
      }
    }

    duel.setActive(false);

    Duel.getDuels().remove(duel);
  }

  @SuppressWarnings("unused")
  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  public Duel getDuel() {
    return duel;
  }

  public Arena getArena() {
    return arena;
  }

  @SuppressWarnings("unused")
  public Long timeElapsed() {
    return duel.getElapsed();
  }
}