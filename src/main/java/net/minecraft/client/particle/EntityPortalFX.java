package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityPortalFX extends EntityFX
{
    private final float portalParticleScale;
    private final double portalPosX;
    private final double portalPosY;
    private final double portalPosZ;

    protected EntityPortalFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        motionX = xSpeedIn;
        motionY = ySpeedIn;
        motionZ = zSpeedIn;
        portalPosX = posX = xCoordIn;
        portalPosY = posY = yCoordIn;
        portalPosZ = posZ = zCoordIn;
        float f = rand.nextFloat() * 0.6F + 0.4F;
        portalParticleScale = particleScale = rand.nextFloat() * 0.2F + 0.5F;
        particleRed = particleGreen = particleBlue = 1.0F * f;
        particleGreen *= 0.3F;
        particleRed *= 0.9F;
        particleMaxAge = (int)(Math.random() * 10.0D) + 40;
        noClip = true;
        setParticleTextureIndex((int)(Math.random() * 8.0D));
    }

    /**
     * Renders the particle
     */
    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_)
    {
        float f = ((float) particleAge + partialTicks) / (float) particleMaxAge;
        f = 1.0F - f;
        f = f * f;
        f = 1.0F - f;
        particleScale = portalParticleScale * f;
        super.renderParticle(worldRendererIn, entityIn, partialTicks, p_180434_4_, p_180434_5_, p_180434_6_, p_180434_7_, p_180434_8_);
    }

    public int getBrightnessForRender(float partialTicks)
    {
        int i = super.getBrightnessForRender(partialTicks);
        float f = (float) particleAge / (float) particleMaxAge;
        f = f * f;
        f = f * f;
        int j = i & 255;
        int k = i >> 16 & 255;
        k = k + (int)(f * 15.0F * 16.0F);

        if (k > 240)
        {
            k = 240;
        }

        return j | k << 16;
    }

    /**
     * Gets how bright this entity is.
     */
    public float getBrightness(float partialTicks)
    {
        float f = super.getBrightness(partialTicks);
        float f1 = (float) particleAge / (float) particleMaxAge;
        f1 = f1 * f1 * f1 * f1;
        return f * (1.0F - f1) + f1;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        float f = (float) particleAge / (float) particleMaxAge;
        f = -f + f * f * 2.0F;
        f = 1.0F - f;
        posX = portalPosX + motionX * (double)f;
        posY = portalPosY + motionY * (double)f + (double)(1.0F - f);
        posZ = portalPosZ + motionZ * (double)f;

        if (particleAge++ >= particleMaxAge)
        {
            setDead();
        }
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_)
        {
            return new EntityPortalFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }
}
