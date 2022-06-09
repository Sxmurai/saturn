package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.util.MathHelper;

public class ModelRabbit extends ModelBase
{
    /** The Rabbit's Left Foot */
    ModelRenderer rabbitLeftFoot;

    /** The Rabbit's Right Foot */
    ModelRenderer rabbitRightFoot;

    /** The Rabbit's Left Thigh */
    ModelRenderer rabbitLeftThigh;

    /** The Rabbit's Right Thigh */
    ModelRenderer rabbitRightThigh;

    /** The Rabbit's Body */
    ModelRenderer rabbitBody;

    /** The Rabbit's Left Arm */
    ModelRenderer rabbitLeftArm;

    /** The Rabbit's Right Arm */
    ModelRenderer rabbitRightArm;

    /** The Rabbit's Head */
    ModelRenderer rabbitHead;

    /** The Rabbit's Right Ear */
    ModelRenderer rabbitRightEar;

    /** The Rabbit's Left Ear */
    ModelRenderer rabbitLeftEar;

    /** The Rabbit's Tail */
    ModelRenderer rabbitTail;

    /** The Rabbit's Nose */
    ModelRenderer rabbitNose;
    private float field_178701_m = 0.0F;
    private final float field_178699_n = 0.0F;

    public ModelRabbit()
    {
        setTextureOffset("head.main", 0, 0);
        setTextureOffset("head.nose", 0, 24);
        setTextureOffset("head.ear1", 0, 10);
        setTextureOffset("head.ear2", 6, 10);
        rabbitLeftFoot = new ModelRenderer(this, 26, 24);
        rabbitLeftFoot.addBox(-1.0F, 5.5F, -3.7F, 2, 1, 7);
        rabbitLeftFoot.setRotationPoint(3.0F, 17.5F, 3.7F);
        rabbitLeftFoot.mirror = true;
        setRotationOffset(rabbitLeftFoot, 0.0F, 0.0F, 0.0F);
        rabbitRightFoot = new ModelRenderer(this, 8, 24);
        rabbitRightFoot.addBox(-1.0F, 5.5F, -3.7F, 2, 1, 7);
        rabbitRightFoot.setRotationPoint(-3.0F, 17.5F, 3.7F);
        rabbitRightFoot.mirror = true;
        setRotationOffset(rabbitRightFoot, 0.0F, 0.0F, 0.0F);
        rabbitLeftThigh = new ModelRenderer(this, 30, 15);
        rabbitLeftThigh.addBox(-1.0F, 0.0F, 0.0F, 2, 4, 5);
        rabbitLeftThigh.setRotationPoint(3.0F, 17.5F, 3.7F);
        rabbitLeftThigh.mirror = true;
        setRotationOffset(rabbitLeftThigh, -0.34906584F, 0.0F, 0.0F);
        rabbitRightThigh = new ModelRenderer(this, 16, 15);
        rabbitRightThigh.addBox(-1.0F, 0.0F, 0.0F, 2, 4, 5);
        rabbitRightThigh.setRotationPoint(-3.0F, 17.5F, 3.7F);
        rabbitRightThigh.mirror = true;
        setRotationOffset(rabbitRightThigh, -0.34906584F, 0.0F, 0.0F);
        rabbitBody = new ModelRenderer(this, 0, 0);
        rabbitBody.addBox(-3.0F, -2.0F, -10.0F, 6, 5, 10);
        rabbitBody.setRotationPoint(0.0F, 19.0F, 8.0F);
        rabbitBody.mirror = true;
        setRotationOffset(rabbitBody, -0.34906584F, 0.0F, 0.0F);
        rabbitLeftArm = new ModelRenderer(this, 8, 15);
        rabbitLeftArm.addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2);
        rabbitLeftArm.setRotationPoint(3.0F, 17.0F, -1.0F);
        rabbitLeftArm.mirror = true;
        setRotationOffset(rabbitLeftArm, -0.17453292F, 0.0F, 0.0F);
        rabbitRightArm = new ModelRenderer(this, 0, 15);
        rabbitRightArm.addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2);
        rabbitRightArm.setRotationPoint(-3.0F, 17.0F, -1.0F);
        rabbitRightArm.mirror = true;
        setRotationOffset(rabbitRightArm, -0.17453292F, 0.0F, 0.0F);
        rabbitHead = new ModelRenderer(this, 32, 0);
        rabbitHead.addBox(-2.5F, -4.0F, -5.0F, 5, 4, 5);
        rabbitHead.setRotationPoint(0.0F, 16.0F, -1.0F);
        rabbitHead.mirror = true;
        setRotationOffset(rabbitHead, 0.0F, 0.0F, 0.0F);
        rabbitRightEar = new ModelRenderer(this, 52, 0);
        rabbitRightEar.addBox(-2.5F, -9.0F, -1.0F, 2, 5, 1);
        rabbitRightEar.setRotationPoint(0.0F, 16.0F, -1.0F);
        rabbitRightEar.mirror = true;
        setRotationOffset(rabbitRightEar, 0.0F, -0.2617994F, 0.0F);
        rabbitLeftEar = new ModelRenderer(this, 58, 0);
        rabbitLeftEar.addBox(0.5F, -9.0F, -1.0F, 2, 5, 1);
        rabbitLeftEar.setRotationPoint(0.0F, 16.0F, -1.0F);
        rabbitLeftEar.mirror = true;
        setRotationOffset(rabbitLeftEar, 0.0F, 0.2617994F, 0.0F);
        rabbitTail = new ModelRenderer(this, 52, 6);
        rabbitTail.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 2);
        rabbitTail.setRotationPoint(0.0F, 20.0F, 7.0F);
        rabbitTail.mirror = true;
        setRotationOffset(rabbitTail, -0.3490659F, 0.0F, 0.0F);
        rabbitNose = new ModelRenderer(this, 32, 9);
        rabbitNose.addBox(-0.5F, -2.5F, -5.5F, 1, 1, 1);
        rabbitNose.setRotationPoint(0.0F, 16.0F, -1.0F);
        rabbitNose.mirror = true;
        setRotationOffset(rabbitNose, 0.0F, 0.0F, 0.0F);
    }

    private void setRotationOffset(ModelRenderer p_178691_1_, float p_178691_2_, float p_178691_3_, float p_178691_4_)
    {
        p_178691_1_.rotateAngleX = p_178691_2_;
        p_178691_1_.rotateAngleY = p_178691_3_;
        p_178691_1_.rotateAngleZ = p_178691_4_;
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
            GlStateManager.translate(0.0F, 5.0F * scale, 2.0F * scale);
            rabbitHead.render(scale);
            rabbitLeftEar.render(scale);
            rabbitRightEar.render(scale);
            rabbitNose.render(scale);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            rabbitLeftFoot.render(scale);
            rabbitRightFoot.render(scale);
            rabbitLeftThigh.render(scale);
            rabbitRightThigh.render(scale);
            rabbitBody.render(scale);
            rabbitLeftArm.render(scale);
            rabbitRightArm.render(scale);
            rabbitTail.render(scale);
            GlStateManager.popMatrix();
        }
        else
        {
            rabbitLeftFoot.render(scale);
            rabbitRightFoot.render(scale);
            rabbitLeftThigh.render(scale);
            rabbitRightThigh.render(scale);
            rabbitBody.render(scale);
            rabbitLeftArm.render(scale);
            rabbitRightArm.render(scale);
            rabbitHead.render(scale);
            rabbitRightEar.render(scale);
            rabbitLeftEar.render(scale);
            rabbitTail.render(scale);
            rabbitNose.render(scale);
        }
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn)
    {
        float f = p_78087_3_ - (float)entityIn.ticksExisted;
        EntityRabbit entityrabbit = (EntityRabbit)entityIn;
        rabbitNose.rotateAngleX = rabbitHead.rotateAngleX = rabbitRightEar.rotateAngleX = rabbitLeftEar.rotateAngleX = p_78087_5_ * 0.017453292F;
        rabbitNose.rotateAngleY = rabbitHead.rotateAngleY = p_78087_4_ * 0.017453292F;
        rabbitRightEar.rotateAngleY = rabbitNose.rotateAngleY - 0.2617994F;
        rabbitLeftEar.rotateAngleY = rabbitNose.rotateAngleY + 0.2617994F;
        field_178701_m = MathHelper.sin(entityrabbit.func_175521_o(f) * (float)Math.PI);
        rabbitLeftThigh.rotateAngleX = rabbitRightThigh.rotateAngleX = (field_178701_m * 50.0F - 21.0F) * 0.017453292F;
        rabbitLeftFoot.rotateAngleX = rabbitRightFoot.rotateAngleX = field_178701_m * 50.0F * 0.017453292F;
        rabbitLeftArm.rotateAngleX = rabbitRightArm.rotateAngleX = (field_178701_m * -40.0F - 11.0F) * 0.017453292F;
    }

    /**
     * Used for easily adding entity-dependent animations. The second and third float params here are the same second
     * and third as in the setRotationAngles method.
     */
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_, float partialTickTime)
    {
    }
}
