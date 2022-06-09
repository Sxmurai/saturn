package net.minecraft.world.gen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class FlatLayerInfo
{
    private final int field_175902_a;
    private IBlockState field_175901_b;

    /** Amount of layers for this set of layers. */
    private int layerCount;
    private int layerMinimumY;

    public FlatLayerInfo(int p_i45467_1_, Block p_i45467_2_)
    {
        this(3, p_i45467_1_, p_i45467_2_);
    }

    public FlatLayerInfo(int p_i45627_1_, int p_i45627_2_, Block p_i45627_3_)
    {
        layerCount = 1;
        field_175902_a = p_i45627_1_;
        layerCount = p_i45627_2_;
        field_175901_b = p_i45627_3_.getDefaultState();
    }

    public FlatLayerInfo(int p_i45628_1_, int p_i45628_2_, Block p_i45628_3_, int p_i45628_4_)
    {
        this(p_i45628_1_, p_i45628_2_, p_i45628_3_);
        field_175901_b = p_i45628_3_.getStateFromMeta(p_i45628_4_);
    }

    /**
     * Return the amount of layers for this set of layers.
     */
    public int getLayerCount()
    {
        return layerCount;
    }

    public IBlockState func_175900_c()
    {
        return field_175901_b;
    }

    private Block func_151536_b()
    {
        return field_175901_b.getBlock();
    }

    /**
     * Return the block metadata used on this set of layers.
     */
    private int getFillBlockMeta()
    {
        return field_175901_b.getBlock().getMetaFromState(field_175901_b);
    }

    /**
     * Return the minimum Y coordinate for this layer, set during generation.
     */
    public int getMinY()
    {
        return layerMinimumY;
    }

    /**
     * Set the minimum Y coordinate for this layer.
     */
    public void setMinY(int p_82660_1_)
    {
        layerMinimumY = p_82660_1_;
    }

    public String toString()
    {
        String s;

        if (field_175902_a >= 3)
        {
            ResourceLocation resourcelocation = Block.blockRegistry.getNameForObject(func_151536_b());
            s = resourcelocation == null ? "null" : resourcelocation.toString();

            if (layerCount > 1)
            {
                s = layerCount + "*" + s;
            }
        }
        else
        {
            s = Integer.toString(Block.getIdFromBlock(func_151536_b()));

            if (layerCount > 1)
            {
                s = layerCount + "x" + s;
            }
        }

        int i = getFillBlockMeta();

        if (i > 0)
        {
            s = s + ":" + i;
        }

        return s;
    }
}
