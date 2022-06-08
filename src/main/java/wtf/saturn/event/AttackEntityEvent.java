package wtf.saturn.event;

import lombok.Getter;
import me.bush.eventbus.event.Event;
import net.minecraft.entity.Entity;

@Getter
public class AttackEntityEvent extends Event {
    private final Entity entity;

    public AttackEntityEvent(Entity entity) {
        this.entity = entity;
    }

    @Override
    protected boolean isCancellable() {
        return false;
    }
}
