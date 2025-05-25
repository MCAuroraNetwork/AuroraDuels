package club.aurorapvp.auroraduels.events.listeners;

import club.aurorapvp.auroracombat.events.custom.CombatTagEvent;
import club.aurorapvp.auroracombat.events.custom.EntityDamagedByEntityEvent;
import club.aurorapvp.auroraduels.modules.Request;
import club.aurorapvp.auroraduels.modules.duels.Duel;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

  @EventHandler
  public void onCombatTag(CombatTagEvent event) {
    if (Request.hasPendingRequest(event.getTagger())) {
      for (Request request : Request.getRequests(event.getTagger())) {
        request.deny();
      }
    }

    if (Request.hasPendingRequest(event.getTagged())) {
      for (Request request : Request.getRequests(event.getTagged())) {
        request.deny();
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
