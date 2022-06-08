package wtf.saturn.event;

import lombok.Getter;
import me.bush.eventbus.event.Event;
import net.minecraft.util.IChatComponent;

@Getter
public class AddChatMessageEvent extends Event {
    private final IChatComponent component;

    public AddChatMessageEvent(IChatComponent component) {
        this.component = component;
    }

    @Override
    protected boolean isCancellable() {
        return true;
    }
}
