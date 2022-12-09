package club.aurorapvp.modules;

import static club.aurorapvp.config.LangHandler.getLangComponent;
import static club.aurorapvp.modules.WandModule.WandModule.wandActive;

import org.bukkit.entity.Player;

public class ArenaModule {
  public static void createArena(Player p) {
    wandActive = true;

    p.sendMessage(getLangComponent("set-center"));


  }
}