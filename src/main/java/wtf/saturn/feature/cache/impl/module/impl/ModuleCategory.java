package wtf.saturn.feature.cache.impl.module.impl;

import lombok.Getter;

/**
 * The category the module should be put into
 *
 * @author aesthetical
 * @since 6/7/22
 */
@Getter
public enum ModuleCategory {
    CORE("Core"),
    COMBAT("Combat"),
    PERFORMANCE("Performance"),
    SKYBLOCK("Skyblock"),
    VISUALS("Visuals"),
    HUD;

    public final String friendlyName;

    ModuleCategory() {
        friendlyName = name();
    }

    ModuleCategory(String friendlyName) {
        this.friendlyName = friendlyName;
    }
}
