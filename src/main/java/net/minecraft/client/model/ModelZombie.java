package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelZombie extends ModelBiped
{
    public ModelZombie()
    {
        this(0.0F, false);
    }

    protected ModelZombie(float modelSize, float p_i1167_2_, int textureWidthIn, int textureHeightIn)
    {
        super(modelSize, p_i1167_2_, textureWidthIn, textureHeightIn);
    }

    public ModelZombie(float modelSize, boolean p_i1168_2_)
    {
        super(modelSize, 0.0F, 64, p_i1168_2_ ? 32 : 64);
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn)
    {
        super.setRotationAngles(p_78087_1_, p_78087_2_, p_78087_3_, p_78087_4_, p_78087_5_, p_78087_6_, entityIn);
        float f = MathHelper.sin(swingProgress * (float)Math.PI);
        float f1 = MathHelper.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress)) * (float)Math.PI);
        bipedRightArm.rotateAngleZ = 0.0F;
        bipedLeftArm.rotateAngleZ = 0.0F;
        bipedRightArm.rotateAngleY = -(0.1F - f * 0.6F);
        bipedLeftArm.rotateAngleY = 0.1F - f * 0.6F;
        bipedRightArm.rotateAngleX = -((float)Math.PI / 2F);
        bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F);
        bipedRightArm.rotateAngleX -= f * 1.2F - f1 * 0.4F;
        bipedLeftArm.rotateAngleX -= f * 1.2F - f1 * 0.4F;
        bipedRightArm.rotateAngleZ += MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
        bipedLeftArm.rotateAngleZ -= MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
    }
}
