package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistrySimple<K, V> implements IRegistry<K, V>
{
    private static final Logger logger = LogManager.getLogger();
    protected final Map<K, V> registryObjects = createUnderlyingMap();

    protected Map<K, V> createUnderlyingMap()
    {
        return Maps.newHashMap();
    }

    public V getObject(K name)
    {
        return registryObjects.get(name);
    }

    /**
     * Register an object on this registry.
     */
    public void putObject(K p_82595_1_, V p_82595_2_)
    {
        Validate.notNull(p_82595_1_);
        Validate.notNull(p_82595_2_);

        if (registryObjects.containsKey(p_82595_1_))
        {
            RegistrySimple.logger.debug("Adding duplicate key '" + p_82595_1_ + "' to registry");
        }

        registryObjects.put(p_82595_1_, p_82595_2_);
    }

    public Set<K> getKeys()
    {
        return Collections.unmodifiableSet(registryObjects.keySet());
    }

    /**
     * Does this registry contain an entry for the given key?
     */
    public boolean containsKey(K p_148741_1_)
    {
        return registryObjects.containsKey(p_148741_1_);
    }

    public Iterator<V> iterator()
    {
        return registryObjects.values().iterator();
    }
}
