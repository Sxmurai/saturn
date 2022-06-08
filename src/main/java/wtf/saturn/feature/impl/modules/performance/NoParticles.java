package wtf.saturn.feature.impl.modules.performance;

import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;

public class NoParticles extends Module {
    public NoParticles() {
        super("No Particles", ModuleCategory.PERFORMANCE, "Prevents particles from spawning at all");
    }
}
