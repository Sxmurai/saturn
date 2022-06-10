package wtf.saturn.event;

import lombok.Getter;
import me.bush.eventbus.event.Event;
import net.minecraft.client.gui.ScaledResolution;

@Getter
public class RenderHUDEvent extends Event {
    private final ScaledResolution res;

    public RenderHUDEvent(ScaledResolution res) {
        this.res = res;
    }

    @Override
    protected boolean isCancellable() {
        return false;
    }
}
