package wtf.saturn.feature.impl.modules.performance;

import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;

public class ItemStacking extends Module {
    public ItemStacking() {
        super("Item Stacking", ModuleCategory.PERFORMANCE, "Stacks dropped items to prevent client-side lag");
    }
}
