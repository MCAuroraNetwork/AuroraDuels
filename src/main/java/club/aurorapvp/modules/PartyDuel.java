package club.aurorapvp.modules;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.configs.Config;
import club.aurorapvp.configs.Lang;
import club.aurorapvp.events.custom.DuelEndEvent;
import club.aurorapvp.events.custom.DuelStartEvent;
import com.google.common.collect.Iterables;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PartyDuel extends Duel {
  private static final Set<PartyDuel> PARTY_DUELS = new HashSet<>();
  private final Player[] team1;
  private final Player[] team2;

  public PartyDuel(Set<Player> players, boolean ranked) {
    super(players, ranked);

    Iterable<List<Player>>
        partitions = Iterables.partition(this.getPlayers(), (this.getPlayers().size() / 2));

    this.team1 = new HashSet<>(partitions.iterator().next()).toArray(new Player[0]);
    this.team2 = new HashSet<>(Iterables.getLast(partitions)).toArray(new Player[0]);

    PARTY_DUELS.add(this);
  }

  public Player[] getOpponents(Player p) {
    for (Player player : team1) {
      if (player == p) {
        return team2;
      }
    }
    return team1;
  }

  @Override
  public void startDuel(Arena arena, Kit kit) {
    this.arena = arena;

    if (kit != null) {
      for (Player p : this.getPlayers()) {
        Kit.giveKit(kit, p);
      }
    } else {
      for (Player p : this.getPlayers()) {
        Kit playerKit = Duel.getDuelKit(p);

        if (playerKit != null) {
          Kit.giveKit(playerKit, p);
        }
      }
    }

    Location spawn1 =
        new Location(arena.getWorld(),
            arena.getFirstSpawn().getX(), arena.getFirstSpawn().getY(),
            arena.getFirstSpawn().getZ());
    Location spawn2 =
        new Location(arena.getWorld(),
            arena.getSecondSpawn().getX(), arena.getSecondSpawn().getY(),
            arena.getSecondSpawn().getZ());

    for (Player p : team1) {
      p.teleportAsync(spawn1);
    }

    for (Player p : team2) {
      p.teleportAsync(spawn2);
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < team2.length; i++) {
      sb.append(team2[i].getName());
      if (i < team2.length - 1) {
        sb.append(", ");
      }
    }
    String names = sb.toString();

    final Title title1 =
        Title.title(Lang.getComponent("duel-started"),
            Component.text("Opponents: " + names));

    sb = new StringBuilder();
    for (int i = 0; i < team1.length; i++) {
      sb.append(team1[i].getName());
      if (i < team1.length - 1) {
        sb.append(", ");
      }
    }
    names = sb.toString();

    final Title title2 =
        Title.title(Lang.getComponent("duel-started"),
            Component.text("Opponents: " + names));

    for (Player p : team1) {
      p.showTitle(title1);
    }

    for (Player p : team2) {
      p.showTitle(title2);
    }

    PartyDuel thisDuel = this;

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        if (thisDuel.isActive()) {
          for (Player p : thisDuel.getPlayers()) {
            p.sendMessage(Lang.getComponent("duel-time-expired"));
            p.sendMessage(Lang.getComponent("duel-time-expired"));
          }
          thisDuel.endDuel();
        }
        this.cancel();
      }
    }, Config.get().getInt("duels.max-duration"));

    for (Player p : this.getPlayers()) {
      Queue.getQueue(p).exitDuelQueue(p);
      break;
    }

    arena.setOccupied(true);
    timeStarted = System.currentTimeMillis();
    active = true;

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelStartEvent(thisDuel));
      }
    }.runTask(AuroraDuels.INSTANCE);
  }

  @Override
  public void endDuel() {
    Set<Player> aliveTeam1Players = new HashSet<>();
    for (Player p : team1) {
      if (!p.isDead()) {
        aliveTeam1Players.add(p);
      }
    }

    Set<Player> aliveTeam2Players = new HashSet<>();
    for (Player p : team2) {
      if (!p.isDead()) {
        aliveTeam2Players.add(p);
      }
    }

    if (aliveTeam2Players.size() > 0 && aliveTeam2Players.size() > 0) {
      for (Player p : this.getPlayers()) {
        p.sendMessage(Lang.getComponent("draw"));
      }

    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < team2.length; i++) {
      sb.append(team2[i].getName());
      if (i < team2.length - 1) {
        sb.append(", ");
      }
    }
    String team1Names = sb.toString();

    sb = new StringBuilder();
    for (int i = 0; i < team1.length; i++) {
      sb.append(team1[i].getName());
      if (i < team1.length - 1) {
        sb.append(", ");
      }
    }
    String team2Names = sb.toString();

    if (aliveTeam1Players.size() > 0) {
      for (Player p : team1) {
        p.showTitle(Title.title(Lang.getComponent("you-won"), Component.text("Opponents: " + team2Names)));
      }
      for (Player p : team2) {
        p.showTitle(Title.title(Lang.getComponent("you-lost"), Component.text("Opponents: " + team1Names)));
      }
    } else {
      for (Player p : team1) {
        p.showTitle(Title.title(Lang.getComponent("you-lost"), Component.text("Opponents: " + team2Names)));
      }
      for (Player p : team2) {
        p.showTitle(Title.title(Lang.getComponent("you-won"), Component.text("Opponents: " + team1Names)));
      }
    }

    Duel thisDuel = this;

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        for (Player p : thisDuel.getPlayers()) {
          p.teleportAsync(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
        }
        arena.reset();
        arena.setOccupied(false);
        this.cancel();
      }
    }, 3000);

    this.active = false;

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelEndEvent(thisDuel));
      }
    }.runTask(AuroraDuels.INSTANCE);

    PARTY_DUELS.remove(this);
  }
}