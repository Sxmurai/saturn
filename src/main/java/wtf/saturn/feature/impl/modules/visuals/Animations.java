package wtf.saturn.feature.impl.modules.visuals;

import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;

public class Animations extends Module {
    public Animations() {
        super("Animations", ModuleCategory.VISUALS, "Changes your sword animations");
    }

    public enum Mode {
        ONEDOTSEVEN {
            @Override
            public String toString() {
                return "1.7";
            }
        },
        // TODO: maybe add some hack client animations as they tend to look good?
    }
}
