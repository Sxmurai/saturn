package net.minecraft.client.resources.data;

import net.minecraft.util.IChatComponent;

public class PackMetadataSection implements IMetadataSection
{
    private final IChatComponent packDescription;
    private final int packFormat;

    public PackMetadataSection(IChatComponent p_i1034_1_, int p_i1034_2_)
    {
        packDescription = p_i1034_1_;
        packFormat = p_i1034_2_;
    }

    public IChatComponent getPackDescription()
    {
        return packDescription;
    }

    public int getPackFormat()
    {
        return packFormat;
    }
}
