package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired when a duel starts */
public class DuelStartEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Duel duel;

  public DuelStartEvent(Duel duel) {
    this.duel = duel;

    Kit kit = duel.getKit();

    if (kit != null) {
      for (Player player : duel.getPlayers()) {
        kit.giveKit(player);
      }
    } else {
      for (Player player : duel.getPlayers()) {
        Kit duelKit = Duel.getDuelKit(player);
        if (duelKit != null) {
          duelKit.giveKit(player);
        }
      }
    }

    new Timer().schedule(new TimerTask() {
                           @Override
                           public void run() {
                             if (duel.isActive()) {
                               for (Player player : duel.getPlayers()) {
                                 player.sendMessage(Lang.getComponent("duel-time-expired"));
                                 player.sendMessage(Lang.getComponent("duel-time-expired"));
                               }
                               duel.draw();
                             }
                             this.cancel();
                           }
                         },
        Config.get().etInt("duels.max-duration"));

    duel.setActive(true);
    duel.getArena().setOccupied(true);

    duel.checkBounds.runTaskTimer(AuroraDuels.INSTANCE, 200L, 200L);
    duel.bossbarTask.runTaskTimer(AuroraDuels.INSTANCE, 0L, 1L);
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
}
