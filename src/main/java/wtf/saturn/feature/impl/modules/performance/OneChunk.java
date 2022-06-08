package wtf.saturn.feature.impl.modules.performance;

import me.bush.eventbus.annotation.EventListener;
import wtf.saturn.event.TickEvent;
import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;

public class OneChunk extends Module {
    public OneChunk() {
        super("One Chunk", ModuleCategory.PERFORMANCE, "Changes your render distance to 1 chunk");
    }

    private int oldRenderDistance = 0;

    @Override
    protected void onActivated() {
        super.onActivated();
        oldRenderDistance = mc.gameSettings.renderDistanceChunks;
    }

    @Override
    protected void onDeactivated() {
        super.onDeactivated();
        mc.gameSettings.renderDistanceChunks = oldRenderDistance;
    }

    @EventListener
    public void onTick(TickEvent event) {
        mc.gameSettings.renderDistanceChunks = 1;
    }
}
