package net.minecraft.nbt;

import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

public class NBTTagCompound extends NBTBase
{
    private final Map<String, NBTBase> tagMap = Maps.newHashMap();

    /**
     * Write the actual data contents of the tag, implemented in NBT extension classes
     */
    void write(DataOutput output) throws IOException
    {
        for (String s : tagMap.keySet())
        {
            NBTBase nbtbase = tagMap.get(s);
            NBTTagCompound.writeEntry(s, nbtbase, output);
        }

        output.writeByte(0);
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        sizeTracker.read(384L);

        if (depth > 512)
        {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        }
        else
        {
            tagMap.clear();
            byte b0;

            while ((b0 = NBTTagCompound.readType(input, sizeTracker)) != 0)
            {
                String s = NBTTagCompound.readKey(input, sizeTracker);
                sizeTracker.read(224 + 16 * s.length());
                NBTBase nbtbase = NBTTagCompound.readNBT(b0, s, input, depth + 1, sizeTracker);

                if (tagMap.put(s, nbtbase) != null)
                {
                    sizeTracker.read(288L);
                }
            }
        }
    }

    public Set<String> getKeySet()
    {
        return tagMap.keySet();
    }

    /**
     * Gets the type byte for the tag.
     */
    public byte getId()
    {
        return (byte)10;
    }

    /**
     * Stores the given tag into the map with the given string key. This is mostly used to store tag lists.
     */
    public void setTag(String key, NBTBase value)
    {
        tagMap.put(key, value);
    }

    /**
     * Stores a new NBTTagByte with the given byte value into the map with the given string key.
     */
    public void setByte(String key, byte value)
    {
        tagMap.put(key, new NBTTagByte(value));
    }

    /**
     * Stores a new NBTTagShort with the given short value into the map with the given string key.
     */
    public void setShort(String key, short value)
    {
        tagMap.put(key, new NBTTagShort(value));
    }

    /**
     * Stores a new NBTTagInt with the given integer value into the map with the given string key.
     */
    public void setInteger(String key, int value)
    {
        tagMap.put(key, new NBTTagInt(value));
    }

    /**
     * Stores a new NBTTagLong with the given long value into the map with the given string key.
     */
    public void setLong(String key, long value)
    {
        tagMap.put(key, new NBTTagLong(value));
    }

    /**
     * Stores a new NBTTagFloat with the given float value into the map with the given string key.
     */
    public void setFloat(String key, float value)
    {
        tagMap.put(key, new NBTTagFloat(value));
    }

    /**
     * Stores a new NBTTagDouble with the given double value into the map with the given string key.
     */
    public void setDouble(String key, double value)
    {
        tagMap.put(key, new NBTTagDouble(value));
    }

    /**
     * Stores a new NBTTagString with the given string value into the map with the given string key.
     */
    public void setString(String key, String value)
    {
        tagMap.put(key, new NBTTagString(value));
    }

    /**
     * Stores a new NBTTagByteArray with the given array as data into the map with the given string key.
     */
    public void setByteArray(String key, byte[] value)
    {
        tagMap.put(key, new NBTTagByteArray(value));
    }

    /**
     * Stores a new NBTTagIntArray with the given array as data into the map with the given string key.
     */
    public void setIntArray(String key, int[] value)
    {
        tagMap.put(key, new NBTTagIntArray(value));
    }

    /**
     * Stores the given boolean value as a NBTTagByte, storing 1 for true and 0 for false, using the given string key.
     */
    public void setBoolean(String key, boolean value)
    {
        setByte(key, (byte)(value ? 1 : 0));
    }

    /**
     * gets a generic tag with the specified name
     */
    public NBTBase getTag(String key)
    {
        return tagMap.get(key);
    }

    /**
     * Gets the ID byte for the given tag key
     */
    public byte getTagId(String key)
    {
        NBTBase nbtbase = tagMap.get(key);
        return nbtbase != null ? nbtbase.getId() : 0;
    }

    /**
     * Returns whether the given string has been previously stored as a key in the map.
     */
    public boolean hasKey(String key)
    {
        return tagMap.containsKey(key);
    }

    public boolean hasKey(String key, int type)
    {
        int i = getTagId(key);

        if (i == type)
        {
            return true;
        }
        else if (type != 99)
        {
            if (i > 0)
            {
            }

            return false;
        }
        else
        {
            return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
        }
    }

    /**
     * Retrieves a byte value using the specified key, or 0 if no such key was stored.
     */
    public byte getByte(String key)
    {
        try
        {
            return !hasKey(key, 99) ? 0 : ((NBTBase.NBTPrimitive) tagMap.get(key)).getByte();
        }
        catch (ClassCastException var3)
        {
            return (byte)0;
        }
    }

    /**
     * Retrieves a short value using the specified key, or 0 if no such key was stored.
     */
    public short getShort(String key)
    {
        try
        {
            return !hasKey(key, 99) ? 0 : ((NBTBase.NBTPrimitive) tagMap.get(key)).getShort();
        }
        catch (ClassCastException var3)
        {
            return (short)0;
        }
    }

    /**
     * Retrieves an integer value using the specified key, or 0 if no such key was stored.
     */
    public int getInteger(String key)
    {
        try
        {
            return !hasKey(key, 99) ? 0 : ((NBTBase.NBTPrimitive) tagMap.get(key)).getInt();
        }
        catch (ClassCastException var3)
        {
            return 0;
        }
    }

    /**
     * Retrieves a long value using the specified key, or 0 if no such key was stored.
     */
    public long getLong(String key)
    {
        try
        {
            return !hasKey(key, 99) ? 0L : ((NBTBase.NBTPrimitive) tagMap.get(key)).getLong();
        }
        catch (ClassCastException var3)
        {
            return 0L;
        }
    }

    /**
     * Retrieves a float value using the specified key, or 0 if no such key was stored.
     */
    public float getFloat(String key)
    {
        try
        {
            return !hasKey(key, 99) ? 0.0F : ((NBTBase.NBTPrimitive) tagMap.get(key)).getFloat();
        }
        catch (ClassCastException var3)
        {
            return 0.0F;
        }
    }

    /**
     * Retrieves a double value using the specified key, or 0 if no such key was stored.
     */
    public double getDouble(String key)
    {
        try
        {
            return !hasKey(key, 99) ? 0.0D : ((NBTBase.NBTPrimitive) tagMap.get(key)).getDouble();
        }
        catch (ClassCastException var3)
        {
            return 0.0D;
        }
    }

    /**
     * Retrieves a string value using the specified key, or an empty string if no such key was stored.
     */
    public String getString(String key)
    {
        try
        {
            return !hasKey(key, 8) ? "" : tagMap.get(key).getString();
        }
        catch (ClassCastException var3)
        {
            return "";
        }
    }

    /**
     * Retrieves a byte array using the specified key, or a zero-length array if no such key was stored.
     */
    public byte[] getByteArray(String key)
    {
        try
        {
            return !hasKey(key, 7) ? new byte[0] : ((NBTTagByteArray) tagMap.get(key)).getByteArray();
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(createCrashReport(key, 7, classcastexception));
        }
    }

    /**
     * Retrieves an int array using the specified key, or a zero-length array if no such key was stored.
     */
    public int[] getIntArray(String key)
    {
        try
        {
            return !hasKey(key, 11) ? new int[0] : ((NBTTagIntArray) tagMap.get(key)).getIntArray();
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(createCrashReport(key, 11, classcastexception));
        }
    }

    /**
     * Retrieves a NBTTagCompound subtag matching the specified key, or a new empty NBTTagCompound if no such key was
     * stored.
     */
    public NBTTagCompound getCompoundTag(String key)
    {
        try
        {
            return !hasKey(key, 10) ? new NBTTagCompound() : (NBTTagCompound) tagMap.get(key);
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(createCrashReport(key, 10, classcastexception));
        }
    }

    /**
     * Gets the NBTTagList object with the given name. Args: name, NBTBase type
     */
    public NBTTagList getTagList(String key, int type)
    {
        try
        {
            if (getTagId(key) != 9)
            {
                return new NBTTagList();
            }
            else
            {
                NBTTagList nbttaglist = (NBTTagList) tagMap.get(key);
                return nbttaglist.tagCount() > 0 && nbttaglist.getTagType() != type ? new NBTTagList() : nbttaglist;
            }
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(createCrashReport(key, 9, classcastexception));
        }
    }

    /**
     * Retrieves a boolean value using the specified key, or false if no such key was stored. This uses the getByte
     * method.
     */
    public boolean getBoolean(String key)
    {
        return getByte(key) != 0;
    }

    /**
     * Remove the specified tag.
     */
    public void removeTag(String key)
    {
        tagMap.remove(key);
    }

    public String toString()
    {
        StringBuilder stringbuilder = new StringBuilder("{");

        for (Map.Entry<String, NBTBase> entry : tagMap.entrySet())
        {
            if (stringbuilder.length() != 1)
            {
                stringbuilder.append(',');
            }

            stringbuilder.append(entry.getKey()).append(':').append(entry.getValue());
        }

        return stringbuilder.append('}').toString();
    }

    /**
     * Return whether this compound has no tags.
     */
    public boolean hasNoTags()
    {
        return tagMap.isEmpty();
    }

    /**
     * Create a crash report which indicates a NBT read error.
     */
    private CrashReport createCrashReport(final String key, final int expectedType, ClassCastException ex)
    {
        CrashReport crashreport = CrashReport.makeCrashReport(ex, "Reading NBT data");
        CrashReportCategory crashreportcategory = crashreport.makeCategoryDepth("Corrupt NBT tag", 1);
        crashreportcategory.addCrashSectionCallable("Tag type found", new Callable<String>()
        {
            public String call() throws Exception
            {
                return NBTBase.NBT_TYPES[tagMap.get(key).getId()];
            }
        });
        crashreportcategory.addCrashSectionCallable("Tag type expected", new Callable<String>()
        {
            public String call() throws Exception
            {
                return NBTBase.NBT_TYPES[expectedType];
            }
        });
        crashreportcategory.addCrashSection("Tag name", key);
        return crashreport;
    }

    /**
     * Creates a clone of the tag.
     */
    public NBTBase copy()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        for (String s : tagMap.keySet())
        {
            nbttagcompound.setTag(s, tagMap.get(s).copy());
        }

        return nbttagcompound;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (super.equals(p_equals_1_))
        {
            NBTTagCompound nbttagcompound = (NBTTagCompound)p_equals_1_;
            return tagMap.entrySet().equals(nbttagcompound.tagMap.entrySet());
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return super.hashCode() ^ tagMap.hashCode();
    }

    private static void writeEntry(String name, NBTBase data, DataOutput output) throws IOException
    {
        output.writeByte(data.getId());

        if (data.getId() != 0)
        {
            output.writeUTF(name);
            data.write(output);
        }
    }

    private static byte readType(DataInput input, NBTSizeTracker sizeTracker) throws IOException
    {
        return input.readByte();
    }

    private static String readKey(DataInput input, NBTSizeTracker sizeTracker) throws IOException
    {
        return input.readUTF();
    }

    static NBTBase readNBT(byte id, String key, DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        NBTBase nbtbase = NBTBase.createNewByType(id);

        try
        {
            nbtbase.read(input, depth, sizeTracker);
            return nbtbase;
        }
        catch (IOException ioexception)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
            crashreportcategory.addCrashSection("Tag name", key);
            crashreportcategory.addCrashSection("Tag type", Byte.valueOf(id));
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Merges this NBTTagCompound with the given compound. Any sub-compounds are merged using the same methods, other
     * types of tags are overwritten from the given compound.
     */
    public void merge(NBTTagCompound other)
    {
        for (String s : other.tagMap.keySet())
        {
            NBTBase nbtbase = other.tagMap.get(s);

            if (nbtbase.getId() == 10)
            {
                if (hasKey(s, 10))
                {
                    NBTTagCompound nbttagcompound = getCompoundTag(s);
                    nbttagcompound.merge((NBTTagCompound)nbtbase);
                }
                else
                {
                    setTag(s, nbtbase.copy());
                }
            }
            else
            {
                setTag(s, nbtbase.copy());
            }
        }
    }
}
