package wtf.saturn.feature.cache;

import java.util.HashMap;
import java.util.Map;

import static wtf.saturn.launch.Launcher.BUS;

/**
 * Where all caches are managed
 *
 * @author aesthetical
 * @since 6/7/22
 */
public class Caches {
    private static final Map<Class<? extends BaseCache>, BaseCache> caches = new HashMap<>();

    /**
     * Registers a cache
     * @param cache the cache instance
     */
    public static void registerCache(BaseCache cache) {
        if (cache == null) {
            throw new IllegalArgumentException("Cache cannot be null!");
        }

        BUS.subscribe(cache);
        cache.init();
        caches.put(cache.getClass(), cache);
    }

    /**
     * Gets a cache
     * @param klass the cache class
     * @param <T> the type of cache casted to in the method
     * @return the cache or null
     */
    public static <T extends BaseCache> T getCache(Class<? extends BaseCache> klass) {
        return (T) caches.getOrDefault(klass, null);
    }
}