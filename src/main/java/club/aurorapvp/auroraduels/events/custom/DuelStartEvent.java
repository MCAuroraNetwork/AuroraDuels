package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import java.util.Timer;
import java.util.TimerTask;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* Event that is fired when a duel starts */
// TODO make this cancellable
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

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                if (duel.isActive()) {
                  for (Player player : duel.getPlayers()) {
                    player.setGlowing(true);

                    player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("halfway"));
                  }
                  duel.draw();
                }
                this.cancel();
              }
            },
            AuroraDuels.getInstance().getConfig().getInt("duels.max-duration") / 2);

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                if (duel.isActive()) {
                  for (Player player : duel.getPlayers()) {
                    player.showTitle(
                        Title.title(
                            AuroraDuels.getInstance().getLang().getComponent("one-minute-left"),
                            Component.text("")));
                  }
                  duel.draw();
                }
                this.cancel();
              }
            },
            AuroraDuels.getInstance().getConfig().getInt("duels.max-duration") - 60000);

    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                if (duel.isActive()) {
                  for (Player player : duel.getPlayers()) {
                    player.sendMessage(
                        AuroraDuels.getInstance().getLang().getComponent("duel-time-expired"));
                  }
                  duel.draw();
                }
                this.cancel();
              }
            },
            AuroraDuels.getInstance().getConfig().getInt("duels.max-duration"));

    duel.setActive(true);
    duel.getArena().setOccupied(true);

    duel.checkBounds.runTaskTimer(AuroraDuels.getInstance(), 200L, 200L);
    duel.bossbarTask.runTaskTimer(AuroraDuels.getInstance(), 0L, 1L);
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
