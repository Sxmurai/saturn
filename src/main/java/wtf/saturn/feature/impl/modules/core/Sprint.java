package wtf.saturn.feature.impl.modules.core;

import me.bush.eventbus.annotation.EventListener;
import wtf.saturn.event.TickEvent;
import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", ModuleCategory.CORE, "Automatically sprints for you");
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (!mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(!mc.thePlayer.isSneaking() &&
                    !mc.thePlayer.isUsingItem() &&
                    mc.thePlayer.getFoodStats().getFoodLevel() > 6 &&
                    !mc.thePlayer.isCollidedHorizontally &&
                    mc.thePlayer.movementInput.moveForward > 0.0f);
        }
    }
}
