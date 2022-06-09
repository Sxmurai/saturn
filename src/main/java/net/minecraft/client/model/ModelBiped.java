package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelBiped extends ModelBase
{
    public ModelRenderer bipedHead;

    /** The Biped's Headwear. Used for the outer layer of player skins. */
    public ModelRenderer bipedHeadwear;
    public ModelRenderer bipedBody;

    /** The Biped's Right Arm */
    public ModelRenderer bipedRightArm;

    /** The Biped's Left Arm */
    public ModelRenderer bipedLeftArm;

    /** The Biped's Right Leg */
    public ModelRenderer bipedRightLeg;

    /** The Biped's Left Leg */
    public ModelRenderer bipedLeftLeg;

    /**
     * Records whether the model should be rendered holding an item in the left hand, and if that item is a block.
     */
    public int heldItemLeft;

    /**
     * Records whether the model should be rendered holding an item in the right hand, and if that item is a block.
     */
    public int heldItemRight;
    public boolean isSneak;

    /** Records whether the model should be rendered aiming a bow. */
    public boolean aimedBow;

    public ModelBiped()
    {
        this(0.0F);
    }

    public ModelBiped(float modelSize)
    {
        this(modelSize, 0.0F, 64, 32);
    }

    public ModelBiped(float modelSize, float p_i1149_2_, int textureWidthIn, int textureHeightIn)
    {
        textureWidth = textureWidthIn;
        textureHeight = textureHeightIn;
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize);
        bipedHead.setRotationPoint(0.0F, 0.0F + p_i1149_2_, 0.0F);
        bipedHeadwear = new ModelRenderer(this, 32, 0);
        bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize + 0.5F);
        bipedHeadwear.setRotationPoint(0.0F, 0.0F + p_i1149_2_, 0.0F);
        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize);
        bipedBody.setRotationPoint(0.0F, 0.0F + p_i1149_2_, 0.0F);
        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F + p_i1149_2_, 0.0F);
        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F + p_i1149_2_, 0.0F);
        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F + p_i1149_2_, 0.0F);
        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F + p_i1149_2_, 0.0F);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale)
    {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        GlStateManager.pushMatrix();

        if (isChild)
        {
            float f = 2.0F;
            GlStateManager.scale(1.5F / f, 1.5F / f, 1.5F / f);
            GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
            bipedHead.render(scale);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            bipedBody.render(scale);
            bipedRightArm.render(scale);
            bipedLeftArm.render(scale);
            bipedRightLeg.render(scale);
            bipedLeftLeg.render(scale);
            bipedHeadwear.render(scale);
        }
        else
        {
            if (entityIn.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            bipedHead.render(scale);
            bipedBody.render(scale);
            bipedRightArm.render(scale);
            bipedLeftArm.render(scale);
            bipedRightLeg.render(scale);
            bipedLeftLeg.render(scale);
            bipedHeadwear.render(scale);
        }

        GlStateManager.popMatrix();
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn)
    {
        bipedHead.rotateAngleY = p_78087_4_ / (180F / (float)Math.PI);
        bipedHead.rotateAngleX = p_78087_5_ / (180F / (float)Math.PI);
        bipedRightArm.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float)Math.PI) * 2.0F * p_78087_2_ * 0.5F;
        bipedLeftArm.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 2.0F * p_78087_2_ * 0.5F;
        bipedRightArm.rotateAngleZ = 0.0F;
        bipedLeftArm.rotateAngleZ = 0.0F;
        bipedRightLeg.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_;
        bipedLeftLeg.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float)Math.PI) * 1.4F * p_78087_2_;
        bipedRightLeg.rotateAngleY = 0.0F;
        bipedLeftLeg.rotateAngleY = 0.0F;

        if (isRiding)
        {
            bipedRightArm.rotateAngleX += -((float)Math.PI / 5F);
            bipedLeftArm.rotateAngleX += -((float)Math.PI / 5F);
            bipedRightLeg.rotateAngleX = -((float)Math.PI * 2F / 5F);
            bipedLeftLeg.rotateAngleX = -((float)Math.PI * 2F / 5F);
            bipedRightLeg.rotateAngleY = ((float)Math.PI / 10F);
            bipedLeftLeg.rotateAngleY = -((float)Math.PI / 10F);
        }

        if (heldItemLeft != 0)
        {
            bipedLeftArm.rotateAngleX = bipedLeftArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F) * (float) heldItemLeft;
        }

        bipedRightArm.rotateAngleY = 0.0F;
        bipedRightArm.rotateAngleZ = 0.0F;

        switch (heldItemRight)
        {
            case 0:
            case 2:
            default:
                break;

            case 1:
                bipedRightArm.rotateAngleX = bipedRightArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F) * (float) heldItemRight;
                break;

            case 3:
                bipedRightArm.rotateAngleX = bipedRightArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F) * (float) heldItemRight;
                bipedRightArm.rotateAngleY = -0.5235988F;
        }

        bipedLeftArm.rotateAngleY = 0.0F;

        if (swingProgress > -9990.0F)
        {
            float f = swingProgress;
            bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f) * (float)Math.PI * 2.0F) * 0.2F;
            bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * 5.0F;
            bipedRightArm.rotationPointX = -MathHelper.cos(bipedBody.rotateAngleY) * 5.0F;
            bipedLeftArm.rotationPointZ = -MathHelper.sin(bipedBody.rotateAngleY) * 5.0F;
            bipedLeftArm.rotationPointX = MathHelper.cos(bipedBody.rotateAngleY) * 5.0F;
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY;
            bipedLeftArm.rotateAngleY += bipedBody.rotateAngleY;
            bipedLeftArm.rotateAngleX += bipedBody.rotateAngleY;
            f = 1.0F - swingProgress;
            f = f * f;
            f = f * f;
            f = 1.0F - f;
            float f1 = MathHelper.sin(f * (float)Math.PI);
            float f2 = MathHelper.sin(swingProgress * (float)Math.PI) * -(bipedHead.rotateAngleX - 0.7F) * 0.75F;
            bipedRightArm.rotateAngleX = (float)((double) bipedRightArm.rotateAngleX - ((double)f1 * 1.2D + (double)f2));
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY * 2.0F;
            bipedRightArm.rotateAngleZ += MathHelper.sin(swingProgress * (float)Math.PI) * -0.4F;
        }

        if (isSneak)
        {
            bipedBody.rotateAngleX = 0.5F;
            bipedRightArm.rotateAngleX += 0.4F;
            bipedLeftArm.rotateAngleX += 0.4F;
            bipedRightLeg.rotationPointZ = 4.0F;
            bipedLeftLeg.rotationPointZ = 4.0F;
            bipedRightLeg.rotationPointY = 9.0F;
            bipedLeftLeg.rotationPointY = 9.0F;
            bipedHead.rotationPointY = 1.0F;
        }
        else
        {
            bipedBody.rotateAngleX = 0.0F;
            bipedRightLeg.rotationPointZ = 0.1F;
            bipedLeftLeg.rotationPointZ = 0.1F;
            bipedRightLeg.rotationPointY = 12.0F;
            bipedLeftLeg.rotationPointY = 12.0F;
            bipedHead.rotationPointY = 0.0F;
        }

        bipedRightArm.rotateAngleZ += MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
        bipedLeftArm.rotateAngleZ -= MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;

        if (aimedBow)
        {
            float f3 = 0.0F;
            float f4 = 0.0F;
            bipedRightArm.rotateAngleZ = 0.0F;
            bipedLeftArm.rotateAngleZ = 0.0F;
            bipedRightArm.rotateAngleY = -(0.1F - f3 * 0.6F) + bipedHead.rotateAngleY;
            bipedLeftArm.rotateAngleY = 0.1F - f3 * 0.6F + bipedHead.rotateAngleY + 0.4F;
            bipedRightArm.rotateAngleX = -((float)Math.PI / 2F) + bipedHead.rotateAngleX;
            bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F) + bipedHead.rotateAngleX;
            bipedRightArm.rotateAngleX -= f3 * 1.2F - f4 * 0.4F;
            bipedLeftArm.rotateAngleX -= f3 * 1.2F - f4 * 0.4F;
            bipedRightArm.rotateAngleZ += MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
            bipedLeftArm.rotateAngleZ -= MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
            bipedRightArm.rotateAngleX += MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
            bipedLeftArm.rotateAngleX -= MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
        }

        ModelBase.copyModelAngles(bipedHead, bipedHeadwear);
    }

    public void setModelAttributes(ModelBase model)
    {
        super.setModelAttributes(model);

        if (model instanceof ModelBiped)
        {
            ModelBiped modelbiped = (ModelBiped)model;
            heldItemLeft = modelbiped.heldItemLeft;
            heldItemRight = modelbiped.heldItemRight;
            isSneak = modelbiped.isSneak;
            aimedBow = modelbiped.aimedBow;
        }
    }

    public void setInvisible(boolean invisible)
    {
        bipedHead.showModel = invisible;
        bipedHeadwear.showModel = invisible;
        bipedBody.showModel = invisible;
        bipedRightArm.showModel = invisible;
        bipedLeftArm.showModel = invisible;
        bipedRightLeg.showModel = invisible;
        bipedLeftLeg.showModel = invisible;
    }

    public void postRenderArm(float scale)
    {
        bipedRightArm.postRender(scale);
    }
}
