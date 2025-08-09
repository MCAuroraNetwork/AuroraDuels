package club.aurorapvp.auroraduels.data;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.DuelStats;
import club.aurorapvp.auroraduels.modules.DuelStats.Stats;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatsDataHandler {
  private static final MongoCollection<Document> collection =
      AuroraDuels.getInstance().getDatabase().getCollection("duel_stats");

  public static void save(Stats stats) {
    Document statsDocument =
        new Document()
            .append("_id", stats.getUuid().toString() + "-" + stats.getTimeStarted())
            .append("uuid", stats.getUuid().toString())
            .append("arenaName", stats.getArenaName())
            .append("elo", stats.getElo())
            .append("timeStarted", stats.getTimeStarted())
            .append("totemPops", stats.getTotemPops())
            .append("damageReceived", stats.getDamageReceived())
            .append("damageDealt", stats.getDamageDealt())
            .append("healthRemaining", stats.getHealthRemaining());

    collection.updateOne(
        Filters.eq("_id", stats.getUuid().toString() + "-" + stats.getTimeStarted()),
        new Document("$set", statsDocument),
        new com.mongodb.client.model.UpdateOptions().upsert(true));
  }

  public static Stats load(UUID playerUuid, long timeStarted) {
    Document statsDocument =
        collection.find(Filters.eq("_id", playerUuid.toString() + "-" + timeStarted)).first();

    if (statsDocument == null) {
      return null;
    }

    Stats stats =
        new DuelStats.Stats(
            UUID.fromString(statsDocument.getString("uuid")),
            statsDocument.getString("arenaName"),
            statsDocument.getInteger("elo"),
            statsDocument.getLong("timeStarted"));

    stats.setHealthRemaining(statsDocument.getInteger("healthRemaining"));
    stats.addDamageReceived(statsDocument.getInteger("damageReceived"));
    stats.addDamageDealt(statsDocument.getInteger("damageDealt"));

    for (int i = 0; i < statsDocument.getInteger("totemPops"); i++) {
      stats.addTotemPop();
    }

    return stats;
  }

  public static List<Stats> loadAll(UUID playerUuid) {
    List<Stats> list = new ArrayList<>();

    var filter = Filters.eq("uuid", playerUuid.toString());
    var sort = Sorts.descending("timeStarted");

    try (var cursor = collection.find(filter).sort(sort).limit(100).iterator()) {
      while (cursor.hasNext()) {
        Document doc = cursor.next();
        Stats stats =
            new Stats(
                UUID.fromString(doc.getString("uuid")),
                doc.getString("arenaName"),
                doc.getInteger("elo"),
                doc.getLong("timeStarted"));
        stats.setHealthRemaining(doc.getInteger("healthRemaining"));
        stats.addDamageReceived(doc.getInteger("damageReceived"));
        stats.addDamageDealt(doc.getInteger("damageDealt"));
        for (int i = 0; i < doc.getInteger("totemPops"); i++) {
          stats.addTotemPop();
        }
        list.add(stats);
      }
    }

    return list;
  }
}
