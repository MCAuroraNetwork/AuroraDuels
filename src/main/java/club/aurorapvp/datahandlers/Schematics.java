package club.aurorapvp.datahandlers;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.modules.arena.Arena;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class Schematics {

  public static void save(String arenaName, World world, BlockVector3 pos1,
                          BlockVector3 pos2) throws WorldEditException, IOException {
    EditSession session = WorldEdit.getInstance().newEditSession(world);
    File file = new File(AuroraDuels.DATAFOLDER, "/arenas/schematics/" + arenaName + ".schem");

    CuboidRegion region = new CuboidRegion(world, pos1, pos2);
    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
    ForwardExtentCopy forwardExtentCopy =
        new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());

    Operations.complete(forwardExtentCopy);

    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }

    try (ClipboardWriter writer =
             BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
      writer.write(clipboard);
    }
  }

  public static void paste(Arena arena) {
    File file = new File(AuroraDuels.DATAFOLDER, "/arenas/schematics/" + arena.getName() + ".schem");

    Clipboard clipboard;

    World world =
        BukkitAdapter.adapt(
            Bukkit.getWorld(YamlConfiguration.loadConfiguration(ArenaData.get(arena))
                .getString("arena.world")));
    ClipboardFormat format = ClipboardFormats.findByFile(file);

    try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
      clipboard = reader.read();

      try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
        Operation operation =
            new ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(clipboard.getOrigin())
                .build();
        Operations.complete(operation);
      }
    } catch (WorldEditException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static File get(Arena arena) {
    File file = new File(AuroraDuels.DATAFOLDER, "/arenas/schematics/" + arena.getName() + ".schem");
    if (file.exists()) {
      return file;
    } else {
      return null;
    }
  }
}
