package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelPlayer extends ModelBiped
{
    public ModelRenderer bipedLeftArmwear;
    public ModelRenderer bipedRightArmwear;
    public ModelRenderer bipedLeftLegwear;
    public ModelRenderer bipedRightLegwear;
    public ModelRenderer bipedBodyWear;
    private final ModelRenderer bipedCape;
    private final ModelRenderer bipedDeadmau5Head;
    private final boolean smallArms;
    private static final String __OBFID = "CL_00002626";

    public ModelPlayer(float p_i46304_1_, boolean p_i46304_2_)
    {
        super(p_i46304_1_, 0.0F, 64, 64);
        smallArms = p_i46304_2_;
        bipedDeadmau5Head = new ModelRenderer(this, 24, 0);
        bipedDeadmau5Head.addBox(-3.0F, -6.0F, -1.0F, 6, 6, 1, p_i46304_1_);
        bipedCape = new ModelRenderer(this, 0, 0);
        bipedCape.setTextureSize(64, 32);
        bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, p_i46304_1_);

        if (p_i46304_2_)
        {
            bipedLeftArm = new ModelRenderer(this, 32, 48);
            bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, p_i46304_1_);
            bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
            bipedRightArm = new ModelRenderer(this, 40, 16);
            bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, p_i46304_1_);
            bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
            bipedLeftArmwear = new ModelRenderer(this, 48, 48);
            bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, p_i46304_1_ + 0.25F);
            bipedLeftArmwear.setRotationPoint(5.0F, 2.5F, 0.0F);
            bipedRightArmwear = new ModelRenderer(this, 40, 32);
            bipedRightArmwear.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, p_i46304_1_ + 0.25F);
            bipedRightArmwear.setRotationPoint(-5.0F, 2.5F, 10.0F);
        }
        else
        {
            bipedLeftArm = new ModelRenderer(this, 32, 48);
            bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, p_i46304_1_);
            bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            bipedLeftArmwear = new ModelRenderer(this, 48, 48);
            bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, p_i46304_1_ + 0.25F);
            bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
            bipedRightArmwear = new ModelRenderer(this, 40, 32);
            bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, p_i46304_1_ + 0.25F);
            bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);
        }

        bipedLeftLeg = new ModelRenderer(this, 16, 48);
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, p_i46304_1_);
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        bipedLeftLegwear = new ModelRenderer(this, 0, 48);
        bipedLeftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, p_i46304_1_ + 0.25F);
        bipedLeftLegwear.setRotationPoint(1.9F, 12.0F, 0.0F);
        bipedRightLegwear = new ModelRenderer(this, 0, 32);
        bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, p_i46304_1_ + 0.25F);
        bipedRightLegwear.setRotationPoint(-1.9F, 12.0F, 0.0F);
        bipedBodyWear = new ModelRenderer(this, 16, 32);
        bipedBodyWear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, p_i46304_1_ + 0.25F);
        bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale)
    {
        super.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);
        GlStateManager.pushMatrix();

        if (isChild)
        {
            float f = 2.0F;
            GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            bipedLeftLegwear.render(scale);
            bipedRightLegwear.render(scale);
            bipedLeftArmwear.render(scale);
            bipedRightArmwear.render(scale);
            bipedBodyWear.render(scale);
        }
        else
        {
            if (entityIn.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            bipedLeftLegwear.render(scale);
            bipedRightLegwear.render(scale);
            bipedLeftArmwear.render(scale);
            bipedRightArmwear.render(scale);
            bipedBodyWear.render(scale);
        }

        GlStateManager.popMatrix();
    }

    public void renderDeadmau5Head(float p_178727_1_)
    {
        ModelBase.copyModelAngles(bipedHead, bipedDeadmau5Head);
        bipedDeadmau5Head.rotationPointX = 0.0F;
        bipedDeadmau5Head.rotationPointY = 0.0F;
        bipedDeadmau5Head.render(p_178727_1_);
    }

    public void renderCape(float p_178728_1_)
    {
        bipedCape.render(p_178728_1_);
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn)
    {
        super.setRotationAngles(p_78087_1_, p_78087_2_, p_78087_3_, p_78087_4_, p_78087_5_, p_78087_6_, entityIn);
        ModelBase.copyModelAngles(bipedLeftLeg, bipedLeftLegwear);
        ModelBase.copyModelAngles(bipedRightLeg, bipedRightLegwear);
        ModelBase.copyModelAngles(bipedLeftArm, bipedLeftArmwear);
        ModelBase.copyModelAngles(bipedRightArm, bipedRightArmwear);
        ModelBase.copyModelAngles(bipedBody, bipedBodyWear);
    }

    public void renderRightArm()
    {
        bipedRightArm.render(0.0625F);
        bipedRightArmwear.render(0.0625F);
    }

    public void renderLeftArm()
    {
        bipedLeftArm.render(0.0625F);
        bipedLeftArmwear.render(0.0625F);
    }

    public void setInvisible(boolean invisible)
    {
        super.setInvisible(invisible);
        bipedLeftArmwear.showModel = invisible;
        bipedRightArmwear.showModel = invisible;
        bipedLeftLegwear.showModel = invisible;
        bipedRightLegwear.showModel = invisible;
        bipedBodyWear.showModel = invisible;
        bipedCape.showModel = invisible;
        bipedDeadmau5Head.showModel = invisible;
    }

    public void postRenderArm(float scale)
    {
        if (smallArms)
        {
            ++bipedRightArm.rotationPointX;
            bipedRightArm.postRender(scale);
            --bipedRightArm.rotationPointX;
        }
        else
        {
            bipedRightArm.postRender(scale);
        }
    }
}
