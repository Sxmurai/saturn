package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityHugeExplodeFX extends EntityFX
{
    private int timeSinceStart;

    /** the maximum time for the explosion */
    private final int maximumTime = 8;

    protected EntityHugeExplodeFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double p_i1214_8_, double p_i1214_10_, double p_i1214_12_)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
    }

    /**
     * Renders the particle
     */
    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_)
    {
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        for (int i = 0; i < 6; ++i)
        {
            double d0 = posX + (rand.nextDouble() - rand.nextDouble()) * 4.0D;
            double d1 = posY + (rand.nextDouble() - rand.nextDouble()) * 4.0D;
            double d2 = posZ + (rand.nextDouble() - rand.nextDouble()) * 4.0D;
            worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, d0, d1, d2, (float) timeSinceStart / (float) maximumTime, 0.0D, 0.0D);
        }

        ++timeSinceStart;

        if (timeSinceStart == maximumTime)
        {
            setDead();
        }
    }

    public int getFXLayer()
    {
        return 1;
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_)
        {
            return new EntityHugeExplodeFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }
}
