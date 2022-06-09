package net.minecraft.world;

import net.minecraft.nbt.NBTTagCompound;

public class LockCode
{
    public static final LockCode EMPTY_CODE = new LockCode("");
    private final String lock;

    public LockCode(String code)
    {
        lock = code;
    }

    public boolean isEmpty()
    {
        return lock == null || lock.isEmpty();
    }

    public String getLock()
    {
        return lock;
    }

    public void toNBT(NBTTagCompound nbt)
    {
        nbt.setString("Lock", lock);
    }

    public static LockCode fromNBT(NBTTagCompound nbt)
    {
        if (nbt.hasKey("Lock", 8))
        {
            String s = nbt.getString("Lock");
            return new LockCode(s);
        }
        else
        {
            return LockCode.EMPTY_CODE;
        }
    }
}
