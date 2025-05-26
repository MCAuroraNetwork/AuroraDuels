package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroracombat.events.custom.CombatTagEvent;
import club.aurorapvp.auroracombat.events.custom.EntityDamagedByEntityEvent;
import club.aurorapvp.auroraduels.modules.DuelRequest;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

public class CombatEventListener implements Listener {
  @EventHandler
  public void onDamage(EntityDamagedByEntityEvent event) {
    if (event.getDamaged() instanceof Player player) {
      if (Duel.inDuel(player)) {
        Objects.requireNonNull(Duel.getDuel(player)).addDamagedReceived(player, event.getDamage());
      }
    }

    if (event.getDamager() instanceof Player player) {
      if (Duel.inDuel(player)) {
        Objects.requireNonNull(Duel.getDuel(player)).addDamagedDealt(player, event.getDamage());
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onCombatTag(CombatTagEvent event) {
    if (DuelRequest.hasPendingRequest(event.getTagger())) {
      for (DuelRequest duelRequest : DuelRequest.getRequests(event.getTagger())) {
        duelRequest.deny();
      }
    }

    if (DuelRequest.hasPendingRequest(event.getTagged())) {
      for (DuelRequest duelRequest : DuelRequest.getRequests(event.getTagged())) {
        duelRequest.deny();
      }
    }
  }

  @EventHandler
  public void onTotemPop(EntityResurrectEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }

    if (!Duel.inDuel(player)) {
      return;
    }

    Objects.requireNonNull(Duel.getDuel(player)).addTotemPop(player);
  }
}
