package wtf.saturn.feature.impl.modules.combat;

import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;
import wtf.saturn.feature.cache.impl.module.impl.annotations.Warning;

@Warning("You may be susceptible to a ban if you use this on servers which do not run on 1.7 natively.")
public class NoHitDelay extends Module {
    public NoHitDelay() {
        super("No Hit Delay", ModuleCategory.COMBAT, "Removes the hit delay added in 1.8");
    }
}
