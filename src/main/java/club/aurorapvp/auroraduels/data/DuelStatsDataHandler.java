package club.aurorapvp.auroraduels.data;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.DuelStats;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.UUID;

public class DuelStatsDataHandler {
  private final MongoCollection<Document> collection;
  private final DuelStats stats;

  public DuelStatsDataHandler(DuelStats stats) {
    this.stats = stats;
    this.collection = AuroraDuels.getInstance().getDatabase().getCollection("duel_stats");
  }

  public void save() {
    Document statsDocument = new Document()
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
            new com.mongodb.client.model.UpdateOptions().upsert(true)
    );
  }

  public static DuelStats deserializeDuelStats(UUID playerUuid, long timeStarted) {
    MongoCollection<Document> collection = AuroraDuels.getInstance().getDatabase().getCollection("duel_stats");
    Document statsDocument = collection.find(Filters.eq("_id", playerUuid.toString() + "-" + timeStarted)).first();

    if (statsDocument == null) {
      return null;
    }

    DuelStats stats = new DuelStats(
            UUID.fromString(statsDocument.getString("uuid")),
            statsDocument.getString("arenaName"),
            statsDocument.getInteger("elo"),
            statsDocument.getLong("timeStarted")
    );

    stats.setHealthRemaining(statsDocument.getInteger("healthRemaining"));
    stats.addDamageReceived(statsDocument.getInteger("damageReceived"));
    stats.addDamageDealt(statsDocument.getInteger("damageDealt"));

    for (int i = 0; i < statsDocument.getInteger("totemPops"); i++) {
      stats.addTotemPop();
    }

    return stats;
  }
}
