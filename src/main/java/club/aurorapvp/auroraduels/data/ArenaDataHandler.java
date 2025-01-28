package club.aurorapvp.auroraduels.data;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.sk89q.worldedit.math.BlockVector3;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class ArenaDataHandler {

  private final MongoCollection<Document> collection;
  private static Arena arena;
  private final String arenaName;

  public ArenaDataHandler(Arena arena) {
    ArenaDataHandler.arena = arena;
    this.collection = AuroraDuels.getInstance().getDatabase().getCollection("arenas");
    this.arenaName = arena.getName();
  }

  public World getWorld() {
    Document arenaData = collection.find(Filters.eq("_id", arenaName)).first();
    if (arenaData == null || !arenaData.containsKey("world")) {
      return null;
    }
    return Bukkit.getWorld(arenaData.getString("world"));
  }

  public BlockVector3 getFirstSpawn() {
    Document arenaData = collection.find(Filters.eq("_id", arenaName)).first();
    if (arenaData == null || !arenaData.containsKey("spawns.spawn1")) {
      return null;
    }
    Document spawn = (Document) arenaData.get("spawns.spawn1");
    return BlockVector3.at(spawn.getInteger("x"), spawn.getInteger("y"), spawn.getInteger("z"));
  }

  public BlockVector3 getSecondSpawn() {
    Document arenaData = collection.find(Filters.eq("_id", arenaName)).first();
    if (arenaData == null || !arenaData.containsKey("spawns.spawn2")) {
      return null;
    }
    Document spawn = (Document) arenaData.get("spawns.spawn2");
    return BlockVector3.at(spawn.getInteger("x"), spawn.getInteger("y"), spawn.getInteger("z"));
  }

  public Location getCornerOne() {
    Document arenaData = collection.find(Filters.eq("_id", arenaName)).first();
    if (arenaData == null || !arenaData.containsKey("corners.pos1")) {
      return null;
    }
    Document corner = (Document) arenaData.get("corners.pos1");
    World world = getWorld();
    return new Location(world, corner.getDouble("x"), corner.getDouble("y"), corner.getDouble("z"));
  }

  public Location getCornerTwo() {
    Document arenaData = collection.find(Filters.eq("_id", arenaName)).first();
    if (arenaData == null || !arenaData.containsKey("corners.pos2")) {
      return null;
    }
    Document corner = (Document) arenaData.get("corners.pos2");
    World world = getWorld();
    return new Location(world, corner.getDouble("x"), corner.getDouble("y"), corner.getDouble("z"));
  }

  public boolean exists() {
    return collection.find(Filters.eq("_id", arenaName)).first() != null;
  }

  public void delete() {
    collection.deleteOne(Filters.eq("_id", arenaName));
  }

  public void create() {
    Document spawns = new Document()
            .append("spawn1", new Document()
                    .append("x", arena.getFirstSpawn().x())
                    .append("y", arena.getFirstSpawn().y())
                    .append("z", arena.getFirstSpawn().z()))
            .append("spawn2", new Document()
                    .append("x", arena.getSecondSpawn().x())
                    .append("y", arena.getSecondSpawn().y())
                    .append("z", arena.getSecondSpawn().z()));

    Document corners = new Document()
            .append("pos1", new Document()
                    .append("x", arena.getFirstPosition().x())
                    .append("y", arena.getFirstPosition().y())
                    .append("z", arena.getFirstPosition().z()))
            .append("pos2", new Document()
                    .append("x", arena.getSecondPosition().x())
                    .append("y", arena.getSecondPosition().y())
                    .append("z", arena.getSecondPosition().z()));

    Document arenaData = new Document()
            .append("_id", arenaName)
            .append("world", arena.getWorld().getName())
            .append("spawns", spawns)
            .append("corners", corners);

    collection.updateOne(
            Filters.eq("_id", arenaName),
            new Document("$set", arenaData),
            new com.mongodb.client.model.UpdateOptions().upsert(true)
    );
  }
}