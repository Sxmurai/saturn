package net.minecraft.util;

import java.lang.reflect.Array;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadSafeBoundList<T>
{
    private final T[] field_152759_a;
    private final Class <? extends T > field_152760_b;
    private final ReadWriteLock field_152761_c = new ReentrantReadWriteLock();
    private int field_152762_d;
    private int field_152763_e;

    public ThreadSafeBoundList(Class <? extends T > p_i1126_1_, int p_i1126_2_)
    {
        field_152760_b = p_i1126_1_;
        field_152759_a = (T[])Array.newInstance(p_i1126_1_, p_i1126_2_);
    }

    public T func_152757_a(T p_152757_1_)
    {
        field_152761_c.writeLock().lock();
        field_152759_a[field_152763_e] = p_152757_1_;
        field_152763_e = (field_152763_e + 1) % func_152758_b();

        if (field_152762_d < func_152758_b())
        {
            ++field_152762_d;
        }

        field_152761_c.writeLock().unlock();
        return p_152757_1_;
    }

    public int func_152758_b()
    {
        field_152761_c.readLock().lock();
        int i = field_152759_a.length;
        field_152761_c.readLock().unlock();
        return i;
    }

    public T[] func_152756_c()
    {
        T[] at = (T[]) Array.newInstance(field_152760_b, field_152762_d);
        field_152761_c.readLock().lock();

        for (int i = 0; i < field_152762_d; ++i)
        {
            int j = (field_152763_e - field_152762_d + i) % func_152758_b();

            if (j < 0)
            {
                j += func_152758_b();
            }

            at[i] = field_152759_a[j];
        }

        field_152761_c.readLock().unlock();
        return at;
    }
}
