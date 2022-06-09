package net.minecraft.client.renderer.block.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.IVertexProducer;
import optifine.Config;
import optifine.Reflector;

public class BakedQuad implements IVertexProducer
{
    /**
     * Joined 4 vertex records, each has 7 fields (x, y, z, shadeColor, u, v, <unused>), see
     * FaceBakery.storeVertexData()
     */
    protected int[] vertexData;
    protected final int tintIndex;
    protected final EnumFacing face;
    private static final String __OBFID = "CL_00002512";
    private TextureAtlasSprite sprite = null;
    private int[] vertexDataSingle = null;

    public BakedQuad(int[] p_i9_1_, int p_i9_2_, EnumFacing p_i9_3_, TextureAtlasSprite p_i9_4_)
    {
        vertexData = p_i9_1_;
        tintIndex = p_i9_2_;
        face = p_i9_3_;
        sprite = p_i9_4_;
        fixVertexData();
    }

    public TextureAtlasSprite getSprite()
    {
        if (sprite == null)
        {
            sprite = BakedQuad.getSpriteByUv(getVertexData());
        }

        return sprite;
    }

    public String toString()
    {
        return "vertex: " + vertexData.length / 7 + ", tint: " + tintIndex + ", facing: " + face + ", sprite: " + sprite;
    }

    public BakedQuad(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn)
    {
        vertexData = vertexDataIn;
        tintIndex = tintIndexIn;
        face = faceIn;
        fixVertexData();
    }

    public int[] getVertexData()
    {
        fixVertexData();
        return vertexData;
    }

    public boolean hasTintIndex()
    {
        return tintIndex != -1;
    }

    public int getTintIndex()
    {
        return tintIndex;
    }

    public EnumFacing getFace()
    {
        return face;
    }

    public int[] getVertexDataSingle()
    {
        if (vertexDataSingle == null)
        {
            vertexDataSingle = BakedQuad.makeVertexDataSingle(getVertexData(), getSprite());
        }

        return vertexDataSingle;
    }

    private static int[] makeVertexDataSingle(int[] p_makeVertexDataSingle_0_, TextureAtlasSprite p_makeVertexDataSingle_1_)
    {
        int[] aint = p_makeVertexDataSingle_0_.clone();
        int i = p_makeVertexDataSingle_1_.sheetWidth / p_makeVertexDataSingle_1_.getIconWidth();
        int j = p_makeVertexDataSingle_1_.sheetHeight / p_makeVertexDataSingle_1_.getIconHeight();
        int k = aint.length / 4;

        for (int l = 0; l < 4; ++l)
        {
            int i1 = l * k;
            float f = Float.intBitsToFloat(aint[i1 + 4]);
            float f1 = Float.intBitsToFloat(aint[i1 + 4 + 1]);
            float f2 = p_makeVertexDataSingle_1_.toSingleU(f);
            float f3 = p_makeVertexDataSingle_1_.toSingleV(f1);
            aint[i1 + 4] = Float.floatToRawIntBits(f2);
            aint[i1 + 4 + 1] = Float.floatToRawIntBits(f3);
        }

        return aint;
    }

    public void pipe(IVertexConsumer p_pipe_1_)
    {
        Reflector.callVoid(Reflector.LightUtil_putBakedQuad, p_pipe_1_, this);
    }

    private static TextureAtlasSprite getSpriteByUv(int[] p_getSpriteByUv_0_)
    {
        float f = 1.0F;
        float f1 = 1.0F;
        float f2 = 0.0F;
        float f3 = 0.0F;
        int i = p_getSpriteByUv_0_.length / 4;

        for (int j = 0; j < 4; ++j)
        {
            int k = j * i;
            float f4 = Float.intBitsToFloat(p_getSpriteByUv_0_[k + 4]);
            float f5 = Float.intBitsToFloat(p_getSpriteByUv_0_[k + 4 + 1]);
            f = Math.min(f, f4);
            f1 = Math.min(f1, f5);
            f2 = Math.max(f2, f4);
            f3 = Math.max(f3, f5);
        }

        float f6 = (f + f2) / 2.0F;
        float f7 = (f1 + f3) / 2.0F;
        TextureAtlasSprite textureatlassprite = Minecraft.getMinecraft().getTextureMapBlocks().getIconByUV(f6, f7);
        return textureatlassprite;
    }

    private void fixVertexData()
    {
        if (Config.isShaders())
        {
            if (vertexData.length == 28)
            {
                vertexData = BakedQuad.expandVertexData(vertexData);
            }
        }
        else if (vertexData.length == 56)
        {
            vertexData = BakedQuad.compactVertexData(vertexData);
        }
    }

    private static int[] expandVertexData(int[] p_expandVertexData_0_)
    {
        int i = p_expandVertexData_0_.length / 4;
        int j = i * 2;
        int[] aint = new int[j * 4];

        for (int k = 0; k < 4; ++k)
        {
            System.arraycopy(p_expandVertexData_0_, k * i, aint, k * j, i);
        }

        return aint;
    }

    private static int[] compactVertexData(int[] p_compactVertexData_0_)
    {
        int i = p_compactVertexData_0_.length / 4;
        int j = i / 2;
        int[] aint = new int[j * 4];

        for (int k = 0; k < 4; ++k)
        {
            System.arraycopy(p_compactVertexData_0_, k * i, aint, k * j, j);
        }

        return aint;
    }
}
