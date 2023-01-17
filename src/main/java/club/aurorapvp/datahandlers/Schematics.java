package club.aurorapvp.datahandlers;

import club.aurorapvp.AuroraDuels;
import club.aurorapvp.modules.Arena;
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
  private static Arena arena;
  private static File file;
  public Schematics(Arena arena) {
    Schematics.arena = arena;
    if (!arena.exists()) {
      save();
    }

    file = new File(AuroraDuels.DATA_FOLDER, "/arenas/schematics/" + arena.getName() + ".schem");
  }

  public ClipboardFormat getFormat() {
    if (file.exists()) {
      return ClipboardFormats.findByFile(file);
    } else {
      return null;
    }
  }

  public File getFile() {
    if (file.exists()) {
      return file;
    } else {
      return null;
    }
  }

  private void save() {
    EditSession session =
        WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(arena.getWorld()));

    CuboidRegion region =
        new CuboidRegion(BukkitAdapter.adapt(arena.getWorld()), arena.getFirstPosition(),
            arena.getSecondPosition());
    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
    ForwardExtentCopy forwardExtentCopy =
        new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());

    try {
      Operations.complete(forwardExtentCopy);
    } catch (WorldEditException e) {
      throw new RuntimeException(e);
    }

    if (!file.exists()) {
      try {
        file.getParentFile().mkdirs();
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    try (ClipboardWriter writer =
             BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
      writer.write(clipboard);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void paste() {
    Clipboard clipboard;

    World world =
        BukkitAdapter.adapt(
            Bukkit.getWorld(YamlConfiguration
                .loadConfiguration(arena.getData().getFile())
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
}
