package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelQuadruped extends ModelBase
{
    public ModelRenderer head = new ModelRenderer(this, 0, 0);
    public ModelRenderer body;
    public ModelRenderer leg1;
    public ModelRenderer leg2;
    public ModelRenderer leg3;
    public ModelRenderer leg4;
    protected float childYOffset = 8.0F;
    protected float childZOffset = 4.0F;

    public ModelQuadruped(int p_i1154_1_, float p_i1154_2_)
    {
        head.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, p_i1154_2_);
        head.setRotationPoint(0.0F, (float)(18 - p_i1154_1_), -6.0F);
        body = new ModelRenderer(this, 28, 8);
        body.addBox(-5.0F, -10.0F, -7.0F, 10, 16, 8, p_i1154_2_);
        body.setRotationPoint(0.0F, (float)(17 - p_i1154_1_), 2.0F);
        leg1 = new ModelRenderer(this, 0, 16);
        leg1.addBox(-2.0F, 0.0F, -2.0F, 4, p_i1154_1_, 4, p_i1154_2_);
        leg1.setRotationPoint(-3.0F, (float)(24 - p_i1154_1_), 7.0F);
        leg2 = new ModelRenderer(this, 0, 16);
        leg2.addBox(-2.0F, 0.0F, -2.0F, 4, p_i1154_1_, 4, p_i1154_2_);
        leg2.setRotationPoint(3.0F, (float)(24 - p_i1154_1_), 7.0F);
        leg3 = new ModelRenderer(this, 0, 16);
        leg3.addBox(-2.0F, 0.0F, -2.0F, 4, p_i1154_1_, 4, p_i1154_2_);
        leg3.setRotationPoint(-3.0F, (float)(24 - p_i1154_1_), -5.0F);
        leg4 = new ModelRenderer(this, 0, 16);
        leg4.addBox(-2.0F, 0.0F, -2.0F, 4, p_i1154_1_, 4, p_i1154_2_);
        leg4.setRotationPoint(3.0F, (float)(24 - p_i1154_1_), -5.0F);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale)
    {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);

        if (isChild)
        {
            float f = 2.0F;
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, childYOffset * scale, childZOffset * scale);
            head.render(scale);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            body.render(scale);
            leg1.render(scale);
            leg2.render(scale);
            leg3.render(scale);
            leg4.render(scale);
            GlStateManager.popMatrix();
        }
        else
        {
            head.render(scale);
            body.render(scale);
            leg1.render(scale);
            leg2.render(scale);
            leg3.render(scale);
            leg4.render(scale);
        }
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn)
    {
        float f = (180F / (float)Math.PI);
        head.rotateAngleX = p_78087_5_ / (180F / (float)Math.PI);
        head.rotateAngleY = p_78087_4_ / (180F / (float)Math.PI);
        body.rotateAngleX = ((float)Math.PI / 2F);
        leg1.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_;
        leg2.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float)Math.PI) * 1.4F * p_78087_2_;
        leg3.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float)Math.PI) * 1.4F * p_78087_2_;
        leg4.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_;
    }
}
