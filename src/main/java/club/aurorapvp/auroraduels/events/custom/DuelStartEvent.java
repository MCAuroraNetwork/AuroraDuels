package club.aurorapvp.auroraduels.events.custom;

import club.aurorapvp.aurorachat.modules.ChatGroup;
import club.aurorapvp.auroracombat.modules.BlockFallDamage;
import club.aurorapvp.auroracombat.modules.CombatTag;
import club.aurorapvp.auroracombat.modules.Rating;
import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.DuelStats;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public class DuelStartEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Duel duel;

  public DuelStartEvent(Duel duel) {
    this.duel = duel;

    for (Player player : duel.getPlayers()) {
      player.setHealth(20);
      player.setFoodLevel(20);
      player.setSaturation(5f);
      player.setExhaustion(0f);
      player.setAbsorptionAmount(0f);

      player.setRemainingAir(player.getMaximumAir());

      for (PotionEffect effect : player.getActivePotionEffects()) {
        player.removePotionEffect(effect.getType());
      }
    }

    Kit kit = duel.getKit();

    if (kit != null) {
      for (Player player : duel.getPlayers()) {
        kit.giveKit(player);
      }
    } else if (duel.getCurrentRound() != 1) {
      for (Player player : duel.getPlayers()) {
        Kit lastUsedKit = Kit.getLastUsedKit(player);

        if (lastUsedKit != null) {
          lastUsedKit.giveKit(player);
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

    duel.getArena().setOccupied(true);

    for (Player player : duel.getPlayers()) {
      DuelStats.Stats stats =
              new DuelStats.Stats(
                      player.getUniqueId(),
                      duel.getArena().getName(),
                      Rating.getRating("overall_duels").getScore(player).getPoints(),
                      System.currentTimeMillis());

      duel.getStats().put(player.getUniqueId(), stats);
      DuelStats.get(player).addStats(stats);
    }

    if (!AuroraDuels.getInstance().isAuroraCombatInstalled()) {
      return;
    }

    for (Player player : duel.getPlayers()) {
      BlockFallDamage.setVulnerable(player);
    }

    Rating rating = Rating.getRating("overall");

    for (Player player : duel.getPlayers()) {
      rating.setPlayerEnabled(player, false);

      CombatTag.setTaggable(player, false);
    }

    // Save current chat state and override with duel chat
    for (Player player : duel.getPlayers()) {
      ChatGroup group = ChatGroup.getChatGroup(player);
      Duel.ChatState state = new Duel.ChatState(group.getMode(), group.getAllowedGroup());
      duel.getSavedChatStates().put(player, state);
      group.setChatTarget(duel);
      player.sendMessage(AuroraDuels.getInstance().getLang().getComponent("duel-chat-enabled"));
    }
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