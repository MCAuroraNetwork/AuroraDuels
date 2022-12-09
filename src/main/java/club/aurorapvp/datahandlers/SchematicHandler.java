package club.aurorapvp.datahandlers;

import static club.aurorapvp.AuroraDuels.DataFolder;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
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
import org.bukkit.Location;

public class SchematicHandler {

  public static void saveArenaSchematic(String arenaName, World world, BlockVector3 pos1,
                                        BlockVector3 pos2) throws WorldEditException, IOException {
    EditSession session = WorldEdit.getInstance().newEditSessionBuilder().build();
    File file = new File(DataFolder, "/arena_schematics/" + arenaName + ".schem");

    // Copies region file to clipboard

    CuboidRegion region = new CuboidRegion(world, pos1, pos2);
    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
    ForwardExtentCopy forwardExtentCopy =
        new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());

    Operations.complete(forwardExtentCopy);

    // Writes clipboard to schematic file

    ClipboardWriter writer =
        BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file));
    writer.write(clipboard);
  }

  public static void pasteArenaSchematic(String arenaName, Location loc)
      throws IOException, WorldEditException {

    File file = new File(DataFolder, "/arena_schematics/" + arenaName + ".schem");
    BlockVector3 center = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

    // Loads then pastes schematic

    ClipboardFormat format = ClipboardFormats.findByFile(file);
    ClipboardReader reader = format.getReader(new FileInputStream(file));
    Clipboard clipboard = reader.read();

    EditSession session = WorldEdit.getInstance().newEditSessionBuilder().build();
    Operation operation = new ClipboardHolder(clipboard)
        .createPaste(session)
        .to(center)
        .ignoreAirBlocks(false)
        .build();
    Operations.complete(operation);
  }
}
