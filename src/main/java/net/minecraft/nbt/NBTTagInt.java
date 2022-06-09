package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagInt extends NBTBase.NBTPrimitive
{
    /** The integer value for the tag. */
    private int data;

    NBTTagInt()
    {
    }

    public NBTTagInt(int data)
    {
        this.data = data;
    }

    /**
     * Write the actual data contents of the tag, implemented in NBT extension classes
     */
    void write(DataOutput output) throws IOException
    {
        output.writeInt(data);
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        sizeTracker.read(96L);
        data = input.readInt();
    }

    /**
     * Gets the type byte for the tag.
     */
    public byte getId()
    {
        return (byte)3;
    }

    public String toString()
    {
        return "" + data;
    }

    /**
     * Creates a clone of the tag.
     */
    public NBTBase copy()
    {
        return new NBTTagInt(data);
    }

    public boolean equals(Object p_equals_1_)
    {
        if (super.equals(p_equals_1_))
        {
            NBTTagInt nbttagint = (NBTTagInt)p_equals_1_;
            return data == nbttagint.data;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return super.hashCode() ^ data;
    }

    public long getLong()
    {
        return data;
    }

    public int getInt()
    {
        return data;
    }

    public short getShort()
    {
        return (short)(data & 65535);
    }

    public byte getByte()
    {
        return (byte)(data & 255);
    }

    public double getDouble()
    {
        return data;
    }

    public float getFloat()
    {
        return (float) data;
    }
}
