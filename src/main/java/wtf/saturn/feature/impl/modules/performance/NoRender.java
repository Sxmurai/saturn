package wtf.saturn.feature.impl.modules.performance;

import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;
import wtf.saturn.feature.impl.setting.Setting;

public class NoRender extends Module {
    public NoRender() {
        super("No Render", ModuleCategory.PERFORMANCE, "Prevents things from rendering");
    }

    public final Setting<Boolean> hurtcam = new Setting<>("Hurtcam", false);
    public final Setting<Boolean> fire = new Setting<>("Fire", false);
    public final Setting<Boolean> particles = new Setting<>("Particles", true);
}
