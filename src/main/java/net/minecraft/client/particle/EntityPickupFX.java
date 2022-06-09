package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityPickupFX extends EntityFX
{
    private final Entity field_174840_a;
    private final Entity field_174843_ax;
    private int age;
    private final int maxAge;
    private final float field_174841_aA;
    private final RenderManager field_174842_aB = Minecraft.getMinecraft().getRenderManager();

    public EntityPickupFX(World worldIn, Entity p_i1233_2_, Entity p_i1233_3_, float p_i1233_4_)
    {
        super(worldIn, p_i1233_2_.posX, p_i1233_2_.posY, p_i1233_2_.posZ, p_i1233_2_.motionX, p_i1233_2_.motionY, p_i1233_2_.motionZ);
        field_174840_a = p_i1233_2_;
        field_174843_ax = p_i1233_3_;
        maxAge = 3;
        field_174841_aA = p_i1233_4_;
    }

    /**
     * Renders the particle
     */
    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_)
    {
        float f = ((float) age + partialTicks) / (float) maxAge;
        f = f * f;
        double d0 = field_174840_a.posX;
        double d1 = field_174840_a.posY;
        double d2 = field_174840_a.posZ;
        double d3 = field_174843_ax.lastTickPosX + (field_174843_ax.posX - field_174843_ax.lastTickPosX) * (double)partialTicks;
        double d4 = field_174843_ax.lastTickPosY + (field_174843_ax.posY - field_174843_ax.lastTickPosY) * (double)partialTicks + (double) field_174841_aA;
        double d5 = field_174843_ax.lastTickPosZ + (field_174843_ax.posZ - field_174843_ax.lastTickPosZ) * (double)partialTicks;
        double d6 = d0 + (d3 - d0) * (double)f;
        double d7 = d1 + (d4 - d1) * (double)f;
        double d8 = d2 + (d5 - d2) * (double)f;
        int i = getBrightnessForRender(partialTicks);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        d6 = d6 - EntityFX.interpPosX;
        d7 = d7 - EntityFX.interpPosY;
        d8 = d8 - EntityFX.interpPosZ;
        field_174842_aB.renderEntityWithPosYaw(field_174840_a, (float)d6, (float)d7, (float)d8, field_174840_a.rotationYaw, partialTicks);
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        ++age;

        if (age == maxAge)
        {
            setDead();
        }
    }

    public int getFXLayer()
    {
        return 3;
    }
}
