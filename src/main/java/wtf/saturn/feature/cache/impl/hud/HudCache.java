package wtf.saturn.feature.cache.impl.hud;

import me.bush.eventbus.annotation.EventListener;
import wtf.saturn.event.RenderHUDEvent;
import wtf.saturn.feature.cache.BaseCache;
import wtf.saturn.feature.cache.Caches;
import wtf.saturn.gui.mods.GuiScreenModManagement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class HudCache extends BaseCache<HUDModule> {
    private final Map<String, HUDModule> modulesByName = new HashMap<>();
    private final List<HUDModule> modules = new CopyOnWriteArrayList<>();

    @Override
    public void init() {

    }

    @EventListener
    public void onRenderHUD(RenderHUDEvent event) {
        if (mc.currentScreen instanceof GuiScreenModManagement || mc.gameSettings.showDebugProfilerChart) {
            return;
        }

        for (HUDModule module : modules) {
            if (module.isToggled()) {
                module.render(event.getRes());
            }
        }
    }

    public void addModule(HUDModule module) {
        modulesByName.put(module.getName(), module);
        modules.add(module);
    }

    public static HudCache get() {
        return Caches.getCache(HudCache.class);
    }
}
