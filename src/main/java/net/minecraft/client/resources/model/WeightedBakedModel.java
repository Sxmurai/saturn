package net.minecraft.client.resources.model;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandom;

public class WeightedBakedModel implements IBakedModel
{
    private final int totalWeight;
    private final List<WeightedBakedModel.MyWeighedRandomItem> models;
    private final IBakedModel baseModel;

    public WeightedBakedModel(List<WeightedBakedModel.MyWeighedRandomItem> p_i46073_1_)
    {
        models = p_i46073_1_;
        totalWeight = WeightedRandom.getTotalWeight(p_i46073_1_);
        baseModel = p_i46073_1_.get(0).model;
    }

    public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_)
    {
        return baseModel.getFaceQuads(p_177551_1_);
    }

    public List<BakedQuad> getGeneralQuads()
    {
        return baseModel.getGeneralQuads();
    }

    public boolean isAmbientOcclusion()
    {
        return baseModel.isAmbientOcclusion();
    }

    public boolean isGui3d()
    {
        return baseModel.isGui3d();
    }

    public boolean isBuiltInRenderer()
    {
        return baseModel.isBuiltInRenderer();
    }

    public TextureAtlasSprite getParticleTexture()
    {
        return baseModel.getParticleTexture();
    }

    public ItemCameraTransforms getItemCameraTransforms()
    {
        return baseModel.getItemCameraTransforms();
    }

    public IBakedModel getAlternativeModel(long p_177564_1_)
    {
        return WeightedRandom.getRandomItem(models, Math.abs((int)p_177564_1_ >> 16) % totalWeight).model;
    }

    public static class Builder
    {
        private final List<WeightedBakedModel.MyWeighedRandomItem> listItems = Lists.newArrayList();

        public WeightedBakedModel.Builder add(IBakedModel p_177677_1_, int p_177677_2_)
        {
            listItems.add(new WeightedBakedModel.MyWeighedRandomItem(p_177677_1_, p_177677_2_));
            return this;
        }

        public WeightedBakedModel build()
        {
            Collections.sort(listItems);
            return new WeightedBakedModel(listItems);
        }

        public IBakedModel first()
        {
            return listItems.get(0).model;
        }
    }

    static class MyWeighedRandomItem extends WeightedRandom.Item implements Comparable<WeightedBakedModel.MyWeighedRandomItem>
    {
        protected final IBakedModel model;

        public MyWeighedRandomItem(IBakedModel p_i46072_1_, int p_i46072_2_)
        {
            super(p_i46072_2_);
            model = p_i46072_1_;
        }

        public int compareTo(WeightedBakedModel.MyWeighedRandomItem p_compareTo_1_)
        {
            return ComparisonChain.start().compare(p_compareTo_1_.itemWeight, itemWeight).compare(getCountQuads(), p_compareTo_1_.getCountQuads()).result();
        }

        protected int getCountQuads()
        {
            int i = model.getGeneralQuads().size();

            for (EnumFacing enumfacing : EnumFacing.values())
            {
                i += model.getFaceQuads(enumfacing).size();
            }

            return i;
        }

        public String toString()
        {
            return "MyWeighedRandomItem{weight=" + itemWeight + ", model=" + model + '}';
        }
    }
}
