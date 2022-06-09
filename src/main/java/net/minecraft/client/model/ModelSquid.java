package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelSquid extends ModelBase
{
    /** The squid's body */
    ModelRenderer squidBody;

    /** The squid's tentacles */
    ModelRenderer[] squidTentacles = new ModelRenderer[8];

    public ModelSquid()
    {
        int i = -16;
        squidBody = new ModelRenderer(this, 0, 0);
        squidBody.addBox(-6.0F, -8.0F, -6.0F, 12, 16, 12);
        squidBody.rotationPointY += (float)(24 + i);

        for (int j = 0; j < squidTentacles.length; ++j)
        {
            squidTentacles[j] = new ModelRenderer(this, 48, 0);
            double d0 = (double)j * Math.PI * 2.0D / (double) squidTentacles.length;
            float f = (float)Math.cos(d0) * 5.0F;
            float f1 = (float)Math.sin(d0) * 5.0F;
            squidTentacles[j].addBox(-1.0F, 0.0F, -1.0F, 2, 18, 2);
            squidTentacles[j].rotationPointX = f;
            squidTentacles[j].rotationPointZ = f1;
            squidTentacles[j].rotationPointY = (float)(31 + i);
            d0 = (double)j * Math.PI * -2.0D / (double) squidTentacles.length + (Math.PI / 2D);
            squidTentacles[j].rotateAngleY = (float)d0;
        }
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn)
    {
        for (ModelRenderer modelrenderer : squidTentacles)
        {
            modelrenderer.rotateAngleX = p_78087_3_;
        }
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale)
    {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        squidBody.render(scale);

        for (int i = 0; i < squidTentacles.length; ++i)
        {
            squidTentacles[i].render(scale);
        }
    }
}
