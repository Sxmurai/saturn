package wtf.saturn.feature.cache.impl.module.impl.annotations;

import wtf.saturn.feature.cache.impl.module.impl.Module;

/**
 * An annotation used above a module class to represent that this module is
 * incompatible with another module.
 *
 * @author aesthetical
 * @since 6/7/22
 */
public @interface Incompatible {
    Class<? extends Module>[] value();
}
