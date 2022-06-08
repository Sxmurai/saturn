package wtf.saturn.feature.cache;

import lombok.Getter;
import wtf.saturn.util.Globals;

import java.util.HashMap;
import java.util.Map;

/**
 * A base cache for all caches to be registered to the cache
 * @param <T> the cache type
 *
 * @author aesthetical
 * @since 6/7/22
 */
@Getter
public abstract class BaseCache<T> implements Globals {
    protected final Map<Class<?>, T> objects = new HashMap<>();

    public abstract void init();
}