package net.minecraft.util;

import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;

public class Rotations
{
    /** Rotation on the X axis */
    protected final float x;

    /** Rotation on the Y axis */
    protected final float y;

    /** Rotation on the Z axis */
    protected final float z;

    public Rotations(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Rotations(NBTTagList nbt)
    {
        x = nbt.getFloatAt(0);
        y = nbt.getFloatAt(1);
        z = nbt.getFloatAt(2);
    }

    public NBTTagList writeToNBT()
    {
        NBTTagList nbttaglist = new NBTTagList();
        nbttaglist.appendTag(new NBTTagFloat(x));
        nbttaglist.appendTag(new NBTTagFloat(y));
        nbttaglist.appendTag(new NBTTagFloat(z));
        return nbttaglist;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (!(p_equals_1_ instanceof Rotations))
        {
            return false;
        }
        else
        {
            Rotations rotations = (Rotations)p_equals_1_;
            return x == rotations.x && y == rotations.y && z == rotations.z;
        }
    }

    /**
     * Gets the X axis rotation
     */
    public float getX()
    {
        return x;
    }

    /**
     * Gets the Y axis rotation
     */
    public float getY()
    {
        return y;
    }

    /**
     * Gets the Z axis rotation
     */
    public float getZ()
    {
        return z;
    }
}
