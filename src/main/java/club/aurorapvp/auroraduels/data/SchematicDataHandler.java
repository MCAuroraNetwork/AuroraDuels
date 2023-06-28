package club.aurorapvp.auroraduels.data;

import club.aurorapvp.auroraduels.AuroraDuels;
import club.aurorapvp.auroraduels.modules.Arena;
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

public class SchematicDataHandler {
  private static Arena arena;
  private final File FILE;

  public SchematicDataHandler(Arena arena) {
    SchematicDataHandler.arena = arena;

    FILE = new File(AuroraDuels.INSTANCE.getDataFolder(),
        "/arenas/schematics/" + arena.getName() + ".schem");
  }

  public File getFile() {
    return FILE;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void create() {
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
      AuroraDuels.INSTANCE.getLogger().severe("Failed to copy arena schematic data");
    }

    if (!FILE.exists()) {
      try {
        FILE.getParentFile().mkdirs();
        FILE.createNewFile();
      } catch (IOException e) {
        AuroraDuels.INSTANCE.getLogger().severe("Failed to generate schematic file");
      }
    }

    try (ClipboardWriter writer =
             BuiltInClipboardFormat.FAST.getWriter(new FileOutputStream(FILE))) {
      writer.write(clipboard);
    } catch (IOException e) {
      AuroraDuels.INSTANCE.getLogger().severe("Failed to save schematic file");
    }
  }

  public void paste() {
    Clipboard clipboard;

    World world =
        BukkitAdapter.adapt(arena.getWorld());
    ClipboardFormat format = ClipboardFormats.findByFile(FILE);

    try {
      assert format != null;
      try (ClipboardReader reader = format.getReader(new FileInputStream(FILE))) {
        clipboard = reader.read();

        Bukkit.getScheduler().runTaskAsynchronously(AuroraDuels.INSTANCE, () -> {
          try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            Operation operation =
                new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(clipboard.getOrigin())
                    .build();

            Operations.complete(operation);
          } catch (WorldEditException e) {
            AuroraDuels.INSTANCE.getLogger().severe("Failed to paste schematic file");
          }
        });
      }
    } catch (IOException e) {
      AuroraDuels.INSTANCE.getLogger().severe("Failed to read schematic file");
    }
  }
}
