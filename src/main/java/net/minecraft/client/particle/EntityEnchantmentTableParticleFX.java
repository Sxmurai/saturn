package net.minecraft.client.particle;

import net.minecraft.world.World;

public class EntityEnchantmentTableParticleFX extends EntityFX
{
    private final float field_70565_a;
    private final double coordX;
    private final double coordY;
    private final double coordZ;

    protected EntityEnchantmentTableParticleFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        motionX = xSpeedIn;
        motionY = ySpeedIn;
        motionZ = zSpeedIn;
        coordX = xCoordIn;
        coordY = yCoordIn;
        coordZ = zCoordIn;
        posX = prevPosX = xCoordIn + xSpeedIn;
        posY = prevPosY = yCoordIn + ySpeedIn;
        posZ = prevPosZ = zCoordIn + zSpeedIn;
        float f = rand.nextFloat() * 0.6F + 0.4F;
        field_70565_a = particleScale = rand.nextFloat() * 0.5F + 0.2F;
        particleRed = particleGreen = particleBlue = 1.0F * f;
        particleGreen *= 0.9F;
        particleRed *= 0.9F;
        particleMaxAge = (int)(Math.random() * 10.0D) + 30;
        noClip = true;
        setParticleTextureIndex((int)(Math.random() * 26.0D + 1.0D + 224.0D));
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
        f1 = f1 * f1;
        f1 = f1 * f1;
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
        f = 1.0F - f;
        float f1 = 1.0F - f;
        f1 = f1 * f1;
        f1 = f1 * f1;
        posX = coordX + motionX * (double)f;
        posY = coordY + motionY * (double)f - (double)(f1 * 1.2F);
        posZ = coordZ + motionZ * (double)f;

        if (particleAge++ >= particleMaxAge)
        {
            setDead();
        }
    }

    public static class EnchantmentTable implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_)
        {
            return new EntityEnchantmentTableParticleFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }
}
