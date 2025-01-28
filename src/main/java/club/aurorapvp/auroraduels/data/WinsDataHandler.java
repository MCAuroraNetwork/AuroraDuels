package club.aurorapvp.auroraduels.data;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.duels.Winstracker;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class WinsDataHandler {

  private final MongoCollection<Document> collection;
  private final Winstracker tracker;
  private final String playerId;

  public WinsDataHandler(Winstracker tracker) {
    this.tracker = tracker;
    this.collection = AuroraDuels.getInstance().getDatabase().getCollection("wins_data");
    this.playerId = tracker.getPlayer().getUniqueId().toString();
  }

  public int getWinstreak() {
    Document playerData = collection.find(Filters.eq("_id", playerId)).first();
    return playerData != null ? playerData.getInteger("winstreak", -1) : -1;
  }

  public int getWins() {
    Document playerData = collection.find(Filters.eq("_id", playerId)).first();
    return playerData != null ? playerData.getInteger("wins", -1) : -1;
  }

  public int getLosses() {
    Document playerData = collection.find(Filters.eq("_id", playerId)).first();
    return playerData != null ? playerData.getInteger("losses", -1) : -1;
  }

  public void save() {
    Document playerData = new Document()
            .append("_id", playerId)
            .append("winstreak", tracker.getWinstreak())
            .append("wins", tracker.getWins())
            .append("losses", tracker.getLosses());

    collection.updateOne(
            Filters.eq("_id", playerId),
            new Document("$set", playerData),
            new com.mongodb.client.model.UpdateOptions().upsert(true)
    );
  }

  public boolean exists() {
    return collection.find(Filters.eq("_id", playerId)).first() != null;
  }
}