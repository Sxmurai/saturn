package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelEnderman extends ModelBiped
{
    /** Is the enderman carrying a block? */
    public boolean isCarrying;

    /** Is the enderman attacking an entity? */
    public boolean isAttacking;

    public ModelEnderman(float p_i46305_1_)
    {
        super(0.0F, -14.0F, 64, 32);
        float f = -14.0F;
        bipedHeadwear = new ModelRenderer(this, 0, 16);
        bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i46305_1_ - 0.5F);
        bipedHeadwear.setRotationPoint(0.0F, 0.0F + f, 0.0F);
        bipedBody = new ModelRenderer(this, 32, 16);
        bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, p_i46305_1_);
        bipedBody.setRotationPoint(0.0F, 0.0F + f, 0.0F);
        bipedRightArm = new ModelRenderer(this, 56, 0);
        bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 30, 2, p_i46305_1_);
        bipedRightArm.setRotationPoint(-3.0F, 2.0F + f, 0.0F);
        bipedLeftArm = new ModelRenderer(this, 56, 0);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 30, 2, p_i46305_1_);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F + f, 0.0F);
        bipedRightLeg = new ModelRenderer(this, 56, 0);
        bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 30, 2, p_i46305_1_);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F + f, 0.0F);
        bipedLeftLeg = new ModelRenderer(this, 56, 0);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 30, 2, p_i46305_1_);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F + f, 0.0F);
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn)
    {
        super.setRotationAngles(p_78087_1_, p_78087_2_, p_78087_3_, p_78087_4_, p_78087_5_, p_78087_6_, entityIn);
        bipedHead.showModel = true;
        float f = -14.0F;
        bipedBody.rotateAngleX = 0.0F;
        bipedBody.rotationPointY = f;
        bipedBody.rotationPointZ = -0.0F;
        bipedRightLeg.rotateAngleX -= 0.0F;
        bipedLeftLeg.rotateAngleX -= 0.0F;
        bipedRightArm.rotateAngleX = (float)((double) bipedRightArm.rotateAngleX * 0.5D);
        bipedLeftArm.rotateAngleX = (float)((double) bipedLeftArm.rotateAngleX * 0.5D);
        bipedRightLeg.rotateAngleX = (float)((double) bipedRightLeg.rotateAngleX * 0.5D);
        bipedLeftLeg.rotateAngleX = (float)((double) bipedLeftLeg.rotateAngleX * 0.5D);
        float f1 = 0.4F;

        if (bipedRightArm.rotateAngleX > f1)
        {
            bipedRightArm.rotateAngleX = f1;
        }

        if (bipedLeftArm.rotateAngleX > f1)
        {
            bipedLeftArm.rotateAngleX = f1;
        }

        if (bipedRightArm.rotateAngleX < -f1)
        {
            bipedRightArm.rotateAngleX = -f1;
        }

        if (bipedLeftArm.rotateAngleX < -f1)
        {
            bipedLeftArm.rotateAngleX = -f1;
        }

        if (bipedRightLeg.rotateAngleX > f1)
        {
            bipedRightLeg.rotateAngleX = f1;
        }

        if (bipedLeftLeg.rotateAngleX > f1)
        {
            bipedLeftLeg.rotateAngleX = f1;
        }

        if (bipedRightLeg.rotateAngleX < -f1)
        {
            bipedRightLeg.rotateAngleX = -f1;
        }

        if (bipedLeftLeg.rotateAngleX < -f1)
        {
            bipedLeftLeg.rotateAngleX = -f1;
        }

        if (isCarrying)
        {
            bipedRightArm.rotateAngleX = -0.5F;
            bipedLeftArm.rotateAngleX = -0.5F;
            bipedRightArm.rotateAngleZ = 0.05F;
            bipedLeftArm.rotateAngleZ = -0.05F;
        }

        bipedRightArm.rotationPointZ = 0.0F;
        bipedLeftArm.rotationPointZ = 0.0F;
        bipedRightLeg.rotationPointZ = 0.0F;
        bipedLeftLeg.rotationPointZ = 0.0F;
        bipedRightLeg.rotationPointY = 9.0F + f;
        bipedLeftLeg.rotationPointY = 9.0F + f;
        bipedHead.rotationPointZ = -0.0F;
        bipedHead.rotationPointY = f + 1.0F;
        bipedHeadwear.rotationPointX = bipedHead.rotationPointX;
        bipedHeadwear.rotationPointY = bipedHead.rotationPointY;
        bipedHeadwear.rotationPointZ = bipedHead.rotationPointZ;
        bipedHeadwear.rotateAngleX = bipedHead.rotateAngleX;
        bipedHeadwear.rotateAngleY = bipedHead.rotateAngleY;
        bipedHeadwear.rotateAngleZ = bipedHead.rotateAngleZ;

        if (isAttacking)
        {
            float f2 = 1.0F;
            bipedHead.rotationPointY -= f2 * 5.0F;
        }
    }
}
