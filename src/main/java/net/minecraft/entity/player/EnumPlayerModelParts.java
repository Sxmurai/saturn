package net.minecraft.entity.player;

import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public enum EnumPlayerModelParts
{
    CAPE(0, "cape"),
    JACKET(1, "jacket"),
    LEFT_SLEEVE(2, "left_sleeve"),
    RIGHT_SLEEVE(3, "right_sleeve"),
    LEFT_PANTS_LEG(4, "left_pants_leg"),
    RIGHT_PANTS_LEG(5, "right_pants_leg"),
    HAT(6, "hat");

    private final int partId;
    private final int partMask;
    private final String partName;
    private final IChatComponent field_179339_k;

    private EnumPlayerModelParts(int partIdIn, String partNameIn)
    {
        partId = partIdIn;
        partMask = 1 << partIdIn;
        partName = partNameIn;
        field_179339_k = new ChatComponentTranslation("options.modelPart." + partNameIn);
    }

    public int getPartMask()
    {
        return partMask;
    }

    public int getPartId()
    {
        return partId;
    }

    public String getPartName()
    {
        return partName;
    }

    public IChatComponent func_179326_d()
    {
        return field_179339_k;
    }
}
