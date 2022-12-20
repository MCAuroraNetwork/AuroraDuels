package club.aurorapvp.wand;

import java.util.EventListener;

public interface WandListener extends EventListener {
    void onWandEvent(WandEvent event);
}
