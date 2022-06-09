package net.minecraft.client.particle;

import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntitySuspendFX extends EntityFX
{
    protected EntitySuspendFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn - 0.125D, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        particleRed = 0.4F;
        particleGreen = 0.4F;
        particleBlue = 0.7F;
        setParticleTextureIndex(0);
        setSize(0.01F, 0.01F);
        particleScale *= rand.nextFloat() * 0.6F + 0.2F;
        motionX = xSpeedIn * 0.0D;
        motionY = ySpeedIn * 0.0D;
        motionZ = zSpeedIn * 0.0D;
        particleMaxAge = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        moveEntity(motionX, motionY, motionZ);

        if (worldObj.getBlockState(new BlockPos(this)).getBlock().getMaterial() != Material.water)
        {
            setDead();
        }

        if (particleMaxAge-- <= 0)
        {
            setDead();
        }
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_)
        {
            return new EntitySuspendFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }
}
