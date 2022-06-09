package net.minecraft.util;

public class Vec4b
{
    private final byte field_176117_a;
    private final byte field_176115_b;
    private final byte field_176116_c;
    private final byte field_176114_d;

    public Vec4b(byte p_i45555_1_, byte p_i45555_2_, byte p_i45555_3_, byte p_i45555_4_)
    {
        field_176117_a = p_i45555_1_;
        field_176115_b = p_i45555_2_;
        field_176116_c = p_i45555_3_;
        field_176114_d = p_i45555_4_;
    }

    public Vec4b(Vec4b p_i45556_1_)
    {
        field_176117_a = p_i45556_1_.field_176117_a;
        field_176115_b = p_i45556_1_.field_176115_b;
        field_176116_c = p_i45556_1_.field_176116_c;
        field_176114_d = p_i45556_1_.field_176114_d;
    }

    public byte func_176110_a()
    {
        return field_176117_a;
    }

    public byte func_176112_b()
    {
        return field_176115_b;
    }

    public byte func_176113_c()
    {
        return field_176116_c;
    }

    public byte func_176111_d()
    {
        return field_176114_d;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (!(p_equals_1_ instanceof Vec4b))
        {
            return false;
        }
        else
        {
            Vec4b vec4b = (Vec4b)p_equals_1_;
            return field_176117_a == vec4b.field_176117_a && (field_176114_d == vec4b.field_176114_d && (field_176115_b == vec4b.field_176115_b && field_176116_c == vec4b.field_176116_c));
        }
    }

    public int hashCode()
    {
        int i = field_176117_a;
        i = 31 * i + field_176115_b;
        i = 31 * i + field_176116_c;
        i = 31 * i + field_176114_d;
        return i;
    }
}
