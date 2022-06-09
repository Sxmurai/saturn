package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BreakingFour;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public class SimpleBakedModel implements IBakedModel
{
    protected final List<BakedQuad> generalQuads;
    protected final List<List<BakedQuad>> faceQuads;
    protected final boolean ambientOcclusion;
    protected final boolean gui3d;
    protected final TextureAtlasSprite texture;
    protected final ItemCameraTransforms cameraTransforms;

    public SimpleBakedModel(List<BakedQuad> p_i46077_1_, List<List<BakedQuad>> p_i46077_2_, boolean p_i46077_3_, boolean p_i46077_4_, TextureAtlasSprite p_i46077_5_, ItemCameraTransforms p_i46077_6_)
    {
        generalQuads = p_i46077_1_;
        faceQuads = p_i46077_2_;
        ambientOcclusion = p_i46077_3_;
        gui3d = p_i46077_4_;
        texture = p_i46077_5_;
        cameraTransforms = p_i46077_6_;
    }

    public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_)
    {
        return faceQuads.get(p_177551_1_.ordinal());
    }

    public List<BakedQuad> getGeneralQuads()
    {
        return generalQuads;
    }

    public boolean isAmbientOcclusion()
    {
        return ambientOcclusion;
    }

    public boolean isGui3d()
    {
        return gui3d;
    }

    public boolean isBuiltInRenderer()
    {
        return false;
    }

    public TextureAtlasSprite getParticleTexture()
    {
        return texture;
    }

    public ItemCameraTransforms getItemCameraTransforms()
    {
        return cameraTransforms;
    }

    public static class Builder
    {
        private final List<BakedQuad> builderGeneralQuads;
        private final List<List<BakedQuad>> builderFaceQuads;
        private final boolean builderAmbientOcclusion;
        private TextureAtlasSprite builderTexture;
        private final boolean builderGui3d;
        private final ItemCameraTransforms builderCameraTransforms;

        public Builder(ModelBlock p_i46074_1_)
        {
            this(p_i46074_1_.isAmbientOcclusion(), p_i46074_1_.isGui3d(), p_i46074_1_.func_181682_g());
        }

        public Builder(IBakedModel p_i46075_1_, TextureAtlasSprite p_i46075_2_)
        {
            this(p_i46075_1_.isAmbientOcclusion(), p_i46075_1_.isGui3d(), p_i46075_1_.getItemCameraTransforms());
            builderTexture = p_i46075_1_.getParticleTexture();

            for (EnumFacing enumfacing : EnumFacing.values())
            {
                addFaceBreakingFours(p_i46075_1_, p_i46075_2_, enumfacing);
            }

            addGeneralBreakingFours(p_i46075_1_, p_i46075_2_);
        }

        private void addFaceBreakingFours(IBakedModel p_177649_1_, TextureAtlasSprite p_177649_2_, EnumFacing p_177649_3_)
        {
            for (BakedQuad bakedquad : p_177649_1_.getFaceQuads(p_177649_3_))
            {
                addFaceQuad(p_177649_3_, new BreakingFour(bakedquad, p_177649_2_));
            }
        }

        private void addGeneralBreakingFours(IBakedModel p_177647_1_, TextureAtlasSprite p_177647_2_)
        {
            for (BakedQuad bakedquad : p_177647_1_.getGeneralQuads())
            {
                addGeneralQuad(new BreakingFour(bakedquad, p_177647_2_));
            }
        }

        private Builder(boolean p_i46076_1_, boolean p_i46076_2_, ItemCameraTransforms p_i46076_3_)
        {
            builderGeneralQuads = Lists.newArrayList();
            builderFaceQuads = Lists.newArrayListWithCapacity(6);

            for (EnumFacing enumfacing : EnumFacing.values())
            {
                builderFaceQuads.add(Lists.newArrayList());
            }

            builderAmbientOcclusion = p_i46076_1_;
            builderGui3d = p_i46076_2_;
            builderCameraTransforms = p_i46076_3_;
        }

        public SimpleBakedModel.Builder addFaceQuad(EnumFacing p_177650_1_, BakedQuad p_177650_2_)
        {
            builderFaceQuads.get(p_177650_1_.ordinal()).add(p_177650_2_);
            return this;
        }

        public SimpleBakedModel.Builder addGeneralQuad(BakedQuad p_177648_1_)
        {
            builderGeneralQuads.add(p_177648_1_);
            return this;
        }

        public SimpleBakedModel.Builder setTexture(TextureAtlasSprite p_177646_1_)
        {
            builderTexture = p_177646_1_;
            return this;
        }

        public IBakedModel makeBakedModel()
        {
            if (builderTexture == null)
            {
                throw new RuntimeException("Missing particle!");
            }
            else
            {
                return new SimpleBakedModel(builderGeneralQuads, builderFaceQuads, builderAmbientOcclusion, builderGui3d, builderTexture, builderCameraTransforms);
            }
        }
    }
}
