package club.aurorapvp.auroraduels.modules.duels;

import com.google.common.collect.Iterables;
import java.util.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RandomTeamDuel extends TeamDuel {
    public RandomTeamDuel(Set<Player> players, Location respawnLocation, boolean ranked) {
        super(calculateTeams(players).first(), calculateTeams(players).second(), respawnLocation, ranked);
    }

    private static Pair<Player[], Player[]> calculateTeams(Set<Player> players) {
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

        Player[] temp = new HashSet<>(partitions.iterator().next()).toArray(new Player[0]);

        if (extra != null) {
            temp = Arrays.copyOf(temp, temp.length + 1);
            temp[temp.length - 1] = extra;
        }

        Player[] team1 = temp;
        Player[] team2 = new HashSet<>(Iterables.getLast(partitions)).toArray(new Player[0]);

        return new Pair<>(team1, team2);
    }
}

record Pair<T, U>(T first,U second) {}

