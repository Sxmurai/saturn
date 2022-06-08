package wtf.saturn.feature.impl.modules.visuals;

import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;
import wtf.saturn.feature.impl.setting.Setting;

public class SmoothZoom extends Module {
    public SmoothZoom() {
        super("Smooth Zoom", ModuleCategory.VISUALS, "Makes the optifine zoom smooth");
    }

    public final Setting<Double> speed = new Setting<>("Speed", 1.0, 1.0, 5.0);
    public final Setting<Integer> zoom = new Setting<>("Zoom", 4, 1, 10);

    public float hookZoom(float f) {
        // TODO
        return f / 4.0f;
    }
}
