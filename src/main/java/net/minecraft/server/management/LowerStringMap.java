package net.minecraft.server.management;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class LowerStringMap<V> implements Map<String, V>
{
    private final Map<String, V> internalMap = Maps.newLinkedHashMap();

    public int size()
    {
        return internalMap.size();
    }

    public boolean isEmpty()
    {
        return internalMap.isEmpty();
    }

    public boolean containsKey(Object p_containsKey_1_)
    {
        return internalMap.containsKey(p_containsKey_1_.toString().toLowerCase());
    }

    public boolean containsValue(Object p_containsValue_1_)
    {
        return internalMap.containsKey(p_containsValue_1_);
    }

    public V get(Object p_get_1_)
    {
        return internalMap.get(p_get_1_.toString().toLowerCase());
    }

    public V put(String p_put_1_, V p_put_2_)
    {
        return internalMap.put(p_put_1_.toLowerCase(), p_put_2_);
    }

    public V remove(Object p_remove_1_)
    {
        return internalMap.remove(p_remove_1_.toString().toLowerCase());
    }

    public void putAll(Map <? extends String, ? extends V > p_putAll_1_)
    {
        for (Entry <? extends String, ? extends V > entry : p_putAll_1_.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear()
    {
        internalMap.clear();
    }

    public Set<String> keySet()
    {
        return internalMap.keySet();
    }

    public Collection<V> values()
    {
        return internalMap.values();
    }

    public Set<Entry<String, V>> entrySet()
    {
        return internalMap.entrySet();
    }
}
