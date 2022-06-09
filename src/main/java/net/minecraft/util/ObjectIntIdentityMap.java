package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

public class ObjectIntIdentityMap implements IObjectIntIterable
{
    private final IdentityHashMap identityMap = new IdentityHashMap(512);
    private final List objectList = Lists.newArrayList();
    private static final String __OBFID = "CL_00001203";

    public void put(Object key, int value)
    {
        identityMap.put(key, Integer.valueOf(value));

        while (objectList.size() <= value)
        {
            objectList.add(null);
        }

        objectList.set(value, key);
    }

    public int get(Object key)
    {
        Integer integer = (Integer) identityMap.get(key);
        return integer == null ? -1 : integer.intValue();
    }

    public final Object getByValue(int value)
    {
        return value >= 0 && value < objectList.size() ? objectList.get(value) : null;
    }

    public Iterator iterator()
    {
        return Iterators.filter(objectList.iterator(), Predicates.notNull());
    }

    public List getObjectList()
    {
        return objectList;
    }
}
