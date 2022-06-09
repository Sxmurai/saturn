package net.minecraft.client.resources.data;

public class FontMetadataSection implements IMetadataSection
{
    private final float[] charWidths;
    private final float[] charLefts;
    private final float[] charSpacings;

    public FontMetadataSection(float[] p_i1310_1_, float[] p_i1310_2_, float[] p_i1310_3_)
    {
        charWidths = p_i1310_1_;
        charLefts = p_i1310_2_;
        charSpacings = p_i1310_3_;
    }
}
