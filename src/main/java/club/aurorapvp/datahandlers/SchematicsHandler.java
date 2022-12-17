package club.aurorapvp.datahandlers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static club.aurorapvp.AuroraDuels.DataFolder;

public class SchematicsHandler {

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

  public static void resetArenas() {
    File arenas = new File(DataFolder, "/arenas/");
    Clipboard clipboard;

    for (File arenaData : arenas.listFiles()) {
      World world =
          BukkitAdapter.adapt(
              YamlConfiguration.loadConfiguration(arenaData)
                  .getLocation("arena.center")
                  .getWorld());
      ClipboardFormat format = ClipboardFormats.findByFile(arenaData);

      try (ClipboardReader reader = format.getReader(new FileInputStream(arenaData))) {
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
  }
}
