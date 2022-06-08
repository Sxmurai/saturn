package wtf.saturn.event;

import lombok.Getter;
import me.bush.eventbus.event.Event;

@Getter
public class KeyPressEvent extends Event {
    private final int keyCode;
    private final boolean state;

    public KeyPressEvent(int keyCode, boolean state) {
        this.keyCode = keyCode;
        this.state = state;
    }

    @Override
    protected boolean isCancellable() {
        return false;
    }
}
