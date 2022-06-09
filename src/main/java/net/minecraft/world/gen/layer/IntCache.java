package net.minecraft.world.gen.layer;

import com.google.common.collect.Lists;
import java.util.List;

public class IntCache
{
    private static int intCacheSize = 256;
    private static final List<int[]> freeSmallArrays = Lists.newArrayList();
    private static final List<int[]> inUseSmallArrays = Lists.newArrayList();
    private static final List<int[]> freeLargeArrays = Lists.newArrayList();
    private static final List<int[]> inUseLargeArrays = Lists.newArrayList();

    public static synchronized int[] getIntCache(int p_76445_0_)
    {
        if (p_76445_0_ <= 256)
        {
            if (IntCache.freeSmallArrays.isEmpty())
            {
                int[] aint4 = new int[256];
                IntCache.inUseSmallArrays.add(aint4);
                return aint4;
            }
            else
            {
                int[] aint3 = IntCache.freeSmallArrays.remove(IntCache.freeSmallArrays.size() - 1);
                IntCache.inUseSmallArrays.add(aint3);
                return aint3;
            }
        }
        else if (p_76445_0_ > IntCache.intCacheSize)
        {
            IntCache.intCacheSize = p_76445_0_;
            IntCache.freeLargeArrays.clear();
            IntCache.inUseLargeArrays.clear();
            int[] aint2 = new int[IntCache.intCacheSize];
            IntCache.inUseLargeArrays.add(aint2);
            return aint2;
        }
        else if (IntCache.freeLargeArrays.isEmpty())
        {
            int[] aint1 = new int[IntCache.intCacheSize];
            IntCache.inUseLargeArrays.add(aint1);
            return aint1;
        }
        else
        {
            int[] aint = IntCache.freeLargeArrays.remove(IntCache.freeLargeArrays.size() - 1);
            IntCache.inUseLargeArrays.add(aint);
            return aint;
        }
    }

    /**
     * Mark all pre-allocated arrays as available for re-use by moving them to the appropriate free lists.
     */

    public static synchronized void resetIntCache()
    {
        if (!IntCache.freeLargeArrays.isEmpty())
        {
            IntCache.freeLargeArrays.remove(IntCache.freeLargeArrays.size() - 1);
        }

        if (!IntCache.freeSmallArrays.isEmpty())
        {
            IntCache.freeSmallArrays.remove(IntCache.freeSmallArrays.size() - 1);
        }

        IntCache.freeLargeArrays.addAll(IntCache.inUseLargeArrays);
        IntCache.freeSmallArrays.addAll(IntCache.inUseSmallArrays);
        IntCache.inUseLargeArrays.clear();
        IntCache.inUseSmallArrays.clear();
    }

    /**
     * Gets a human-readable string that indicates the sizes of all the cache fields.  Basically a synchronized static
     * toString.
     */

    public static synchronized String getCacheSizes()
    {
        return "cache: " + IntCache.freeLargeArrays.size() + ", tcache: " + IntCache.freeSmallArrays.size() + ", allocated: " + IntCache.inUseLargeArrays.size() + ", tallocated: " + IntCache.inUseSmallArrays.size();
    }
}
