package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.aurorakits.events.custom.KitGiveEvent;
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
        KitGiveEvent event = new KitGiveEvent(kit, player);

        event.callEvent();

        if (event.isCancelled()) {
          return;
        }

        KitGiveEvent kitGiveEvent = new KitGiveEvent(kit, event.getPlayer());

        kitGiveEvent.callEvent();

        if (kitGiveEvent.isCancelled()) {
          return;
        }

        kit.giveKit(event.getPlayer());
      }
    } else {
      for (Player player : duel.getPlayers()) {
        Kit playerKit = Duel.getDuelKit(player);

        if (playerKit != null) {
          KitGiveEvent event = new KitGiveEvent(playerKit, player);

          event.callEvent();

          if (event.isCancelled()) {
            return;
          }

          playerKit.giveKit(player);
        }
      }
    }

    new Timer()
        .schedule(
            new TimerTask() {
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
            Config.get().getInt("duels.max-duration"));

    duel.setActive(true);
    duel.getArena().setOccupied(true);
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
