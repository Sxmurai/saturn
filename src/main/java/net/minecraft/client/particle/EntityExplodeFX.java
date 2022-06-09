package net.minecraft.client.particle;

import net.minecraft.world.World;

public class EntityExplodeFX extends EntityFX
{
    protected EntityExplodeFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        motionX = xSpeedIn + (Math.random() * 2.0D - 1.0D) * 0.05000000074505806D;
        motionY = ySpeedIn + (Math.random() * 2.0D - 1.0D) * 0.05000000074505806D;
        motionZ = zSpeedIn + (Math.random() * 2.0D - 1.0D) * 0.05000000074505806D;
        particleRed = particleGreen = particleBlue = rand.nextFloat() * 0.3F + 0.7F;
        particleScale = rand.nextFloat() * rand.nextFloat() * 6.0F + 1.0F;
        particleMaxAge = (int)(16.0D / ((double) rand.nextFloat() * 0.8D + 0.2D)) + 2;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        if (particleAge++ >= particleMaxAge)
        {
            setDead();
        }

        setParticleTextureIndex(7 - particleAge * 8 / particleMaxAge);
        motionY += 0.004D;
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.8999999761581421D;
        motionY *= 0.8999999761581421D;
        motionZ *= 0.8999999761581421D;

        if (onGround)
        {
            motionX *= 0.699999988079071D;
            motionZ *= 0.699999988079071D;
        }
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_)
        {
            return new EntityExplodeFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }
}
