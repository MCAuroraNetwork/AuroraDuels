package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.config.Config;
import club.aurorapvp.auroraduels.config.Lang;
import club.aurorapvp.auroraduels.events.custom.DuelEndEvent;
import club.aurorapvp.auroraduels.events.custom.DuelStartEvent;
import club.aurorapvp.auroraduels.modules.Arena;
import club.aurorapvp.auroraduels.modules.queues.Queue;
import club.aurorapvp.aurorakits.modules.kits.Kit;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
  private final Map<Player, Boolean> team1Dead = new HashMap<>();
  private final Map<Player, Boolean> team2Dead = new HashMap<>();
  private final Player[] team1;
  private final Player[] team2;

  public PartyDuel(Set<Player> players, boolean ranked) {
    super(players, ranked);

    int size = this.getPlayers().size();
    Player extra = null;

    if (size % 2 == 1) {
      Iterator<Player> iterator = this.getPlayers().iterator();
      for (int i = 0; i < size - 1; i++) {
        iterator.next();
      }
      extra = iterator.next();
      iterator.remove();
      size--;
    }

    Iterable<List<Player>>
        partitions = Iterables.partition(this.getPlayers(), (size / 2));

    Player[] temp = new HashSet<>(partitions.iterator().next()).toArray(new Player[0]);

    if (extra != null) {
      temp = Arrays.copyOf(temp, temp.length + 1);
      temp[temp.length - 1] = extra;
    }

    this.team1 = temp;
    this.team2 = new HashSet<>(Iterables.getLast(partitions)).toArray(new Player[0]);

    if (extra != null) {
      this.getPlayers().add(extra);
    }

    PARTY_DUELS.add(this);
  }

  public PartyDuel(Set<Player> players, Queue queue, Kit kit, boolean ranked) {
    super(players, queue, kit, ranked);

    int size = this.getPlayers().size();
    Player extra = null;

    if (size % 2 == 1) {
      Iterator<Player> iterator = this.getPlayers().iterator();
      for (int i = 0; i < size - 1; i++) {
        iterator.next();
      }
      extra = iterator.next();
      iterator.remove();
      size--;
    }

    Iterable<List<Player>>
        partitions = Iterables.partition(this.getPlayers(), (size / 2));

    Player[] temp = new HashSet<>(partitions.iterator().next()).toArray(new Player[0]);

    if (extra != null) {
      temp = Arrays.copyOf(temp, temp.length + 1);
      temp[temp.length - 1] = extra;
    }

    this.team1 = temp;
    this.team2 = new HashSet<>(Iterables.getLast(partitions)).toArray(new Player[0]);

    if (extra != null) {
      this.getPlayers().add(extra);
    }

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

  public Player[] getTeammates(Player p) {
    for (Player player : team1) {
      if (player == p) {
        return team1;
      }
    }
    return team2;
  }

  public void setDead(Player p) {
    if (team1Dead.containsKey(p)) {
      team1Dead.put(p, true);
    } else {
      team2Dead.put(p, true);
    }
  }

  public boolean teamDead(Player p) {
    if (team1Dead.containsKey(p)) {
      for (boolean dead : team1Dead.values()) {
        if (!dead) {
          return false;
        }
      }
      return true;
    } else {
      for (boolean dead : team2Dead.values()) {
        if (!dead) {
          return false;
        }
      }
      return true;
    }
  }

  public void startDuel(Arena arena) {
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
      team1Dead.put(p, false);
      p.teleportAsync(spawn1);
    }

    for (Player p : team2) {
      team2Dead.put(p, false);
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
          thisDuel.draw();
        }
        this.cancel();
      }
    }, Config.get().getInt("duels.max-duration"));

    for (Player p : this.getPlayers()) {
      Objects.requireNonNull(Queue.getQueue(p)).exitDuelQueue(p);
      break;
    }

    arena.setOccupied(true);
    timeStarted = System.currentTimeMillis();

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelStartEvent(thisDuel));
      }
    }.runTask(AuroraDuels.INSTANCE);
  }

  public void endDuel(Player[] winners, Player[] losers) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < winners.length; i++) {
      sb.append(winners[i].getName());
      if (i < winners.length - 1) {
        sb.append(", ");
      }
    }
    String winnerNames = sb.toString();

    sb = new StringBuilder();
    for (int i = 0; i < losers.length; i++) {
      sb.append(losers[i].getName());
      if (i < losers.length - 1) {
        sb.append(", ");
      }
    }
    String loserNames = sb.toString();

    for (Player p : winners) {
      p.showTitle(
          Title.title(Lang.getComponent("you-won"), Component.text("Opponents: " + loserNames)));
    }

    for (Player p : losers) {
      p.showTitle(
          Title.title(Lang.getComponent("you-lost"), Component.text("Opponents: " + winnerNames)));
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

    this.setActive(false);

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager()
            .callEvent(new DuelEndEvent(thisDuel, winners, losers));
      }
    }.runTask(AuroraDuels.INSTANCE);

    PARTY_DUELS.remove(this);
  }

  @Override
  public void draw() {
    for (Player p : this.getPlayers()) {
      p.sendMessage(Lang.getComponent("draw"));
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

    this.setActive(false);

    new BukkitRunnable() {
      @Override
      public void run() {
        Bukkit.getServer().getPluginManager().callEvent(new DuelEndEvent(thisDuel, null, null));
      }
    }.runTask(AuroraDuels.INSTANCE);

    PARTY_DUELS.remove(this);
  }

  @SuppressWarnings("unused")
  public static Set<PartyDuel> getPartyDuels() {
    return PARTY_DUELS;
  }
}