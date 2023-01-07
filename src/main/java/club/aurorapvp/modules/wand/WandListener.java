package club.aurorapvp.modules.wand;

import java.util.EventListener;

public interface WandListener extends EventListener {
    void onWandEvent(WandEvent event);
}
