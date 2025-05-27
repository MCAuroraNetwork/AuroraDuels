package club.aurorapvp.auroraduels.modules.duels;

import club.aurorapvp.auroraduels.modules.duels.teamduels.TeamDuel;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RandomTeamDuel extends TeamDuel {

  public RandomTeamDuel(Set<Player> players, Location respawnLocation, boolean ranked) {
    super(calculateTeams(players).first(), calculateTeams(players).second(), respawnLocation,
        ranked);
  }

  private static Pair<Collection<Player>, Collection<Player>> calculateTeams(Set<Player> players) {
    int size = players.size();
    Player extra = null;

    if (size % 2 == 1) {
      Iterator<Player> iterator = players.iterator();
      for (int i = 0; i < size - 1; i++) {
        iterator.next();
      }
      extra = iterator.next();
      iterator.remove();
      size--;
    }

    Iterable<List<Player>> partitions = Iterables.partition(players, (size / 2));

    Set<Player> team1 = new HashSet<>(partitions.iterator().next());

    if (extra != null) {
      team1.add(extra);
    }

    Set<Player> team2 = new HashSet<>(Iterables.getLast(partitions));

    return new Pair<>(team1, team2);
  }
}

record Pair<T, U>(T first, U second) {

}

