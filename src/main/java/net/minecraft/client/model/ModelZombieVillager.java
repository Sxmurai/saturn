package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelZombieVillager extends ModelBiped
{
    public ModelZombieVillager()
    {
        this(0.0F, 0.0F, false);
    }

    public ModelZombieVillager(float p_i1165_1_, float p_i1165_2_, boolean p_i1165_3_)
    {
        super(p_i1165_1_, 0.0F, 64, p_i1165_3_ ? 32 : 64);

        if (p_i1165_3_)
        {
            bipedHead = new ModelRenderer(this, 0, 0);
            bipedHead.addBox(-4.0F, -10.0F, -4.0F, 8, 8, 8, p_i1165_1_);
            bipedHead.setRotationPoint(0.0F, 0.0F + p_i1165_2_, 0.0F);
        }
        else
        {
            bipedHead = new ModelRenderer(this);
            bipedHead.setRotationPoint(0.0F, 0.0F + p_i1165_2_, 0.0F);
            bipedHead.setTextureOffset(0, 32).addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, p_i1165_1_);
            bipedHead.setTextureOffset(24, 32).addBox(-1.0F, -3.0F, -6.0F, 2, 4, 2, p_i1165_1_);
        }
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
