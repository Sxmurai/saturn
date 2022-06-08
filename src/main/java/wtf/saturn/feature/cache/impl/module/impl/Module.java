package wtf.saturn.feature.cache.impl.module.impl;

import lombok.Getter;
import wtf.saturn.feature.impl.ConfigurableFeature;

/**
 * The base class for a module
 *
 * @author aesthetical
 * @since 6/7/22
 */
@Getter
public class Module extends ConfigurableFeature {
    private final ModuleCategory category;
    private final String description;

    public Module(String name, ModuleCategory category, String description) {
        super(name);

        this.category = category;
        this.description = description;
    }
}
