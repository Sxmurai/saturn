package net.minecraft.util;

public class IntegerCache
{
    private static final Integer[] field_181757_a = new Integer[65535];

    public static Integer func_181756_a(int p_181756_0_)
    {
        return p_181756_0_ > 0 && p_181756_0_ < IntegerCache.field_181757_a.length ? IntegerCache.field_181757_a[p_181756_0_] : Integer.valueOf(p_181756_0_);
    }

    static
    {
        int i = 0;

        for (int j = IntegerCache.field_181757_a.length; i < j; ++i)
        {
            IntegerCache.field_181757_a[i] = Integer.valueOf(i);
        }
    }
}
