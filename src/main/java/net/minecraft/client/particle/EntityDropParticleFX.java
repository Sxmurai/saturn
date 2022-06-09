package net.minecraft.client.particle;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityDropParticleFX extends EntityFX
{
    /** the material type for dropped items/blocks */
    private final Material materialType;

    /** The height of the current bob */
    private int bobTimer;

    protected EntityDropParticleFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, Material p_i1203_8_)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        motionX = motionY = motionZ = 0.0D;

        if (p_i1203_8_ == Material.water)
        {
            particleRed = 0.0F;
            particleGreen = 0.0F;
            particleBlue = 1.0F;
        }
        else
        {
            particleRed = 1.0F;
            particleGreen = 0.0F;
            particleBlue = 0.0F;
        }

        setParticleTextureIndex(113);
        setSize(0.01F, 0.01F);
        particleGravity = 0.06F;
        materialType = p_i1203_8_;
        bobTimer = 40;
        particleMaxAge = (int)(64.0D / (Math.random() * 0.8D + 0.2D));
        motionX = motionY = motionZ = 0.0D;
    }

    public int getBrightnessForRender(float partialTicks)
    {
        return materialType == Material.water ? super.getBrightnessForRender(partialTicks) : 257;
    }

    /**
     * Gets how bright this entity is.
     */
    public float getBrightness(float partialTicks)
    {
        return materialType == Material.water ? super.getBrightness(partialTicks) : 1.0F;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        if (materialType == Material.water)
        {
            particleRed = 0.2F;
            particleGreen = 0.3F;
            particleBlue = 1.0F;
        }
        else
        {
            particleRed = 1.0F;
            particleGreen = 16.0F / (float)(40 - bobTimer + 16);
            particleBlue = 4.0F / (float)(40 - bobTimer + 8);
        }

        motionY -= particleGravity;

        if (bobTimer-- > 0)
        {
            motionX *= 0.02D;
            motionY *= 0.02D;
            motionZ *= 0.02D;
            setParticleTextureIndex(113);
        }
        else
        {
            setParticleTextureIndex(112);
        }

        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.9800000190734863D;
        motionY *= 0.9800000190734863D;
        motionZ *= 0.9800000190734863D;

        if (particleMaxAge-- <= 0)
        {
            setDead();
        }

        if (onGround)
        {
            if (materialType == Material.water)
            {
                setDead();
                worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
            }
            else
            {
                setParticleTextureIndex(114);
            }

            motionX *= 0.699999988079071D;
            motionZ *= 0.699999988079071D;
        }

        BlockPos blockpos = new BlockPos(this);
        IBlockState iblockstate = worldObj.getBlockState(blockpos);
        Material material = iblockstate.getBlock().getMaterial();

        if (material.isLiquid() || material.isSolid())
        {
            double d0 = 0.0D;

            if (iblockstate.getBlock() instanceof BlockLiquid)
            {
                d0 = BlockLiquid.getLiquidHeightPercent(iblockstate.getValue(BlockLiquid.LEVEL).intValue());
            }

            double d1 = (double)(MathHelper.floor_double(posY) + 1) - d0;

            if (posY < d1)
            {
                setDead();
            }
        }
    }

    public static class LavaFactory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_)
        {
            return new EntityDropParticleFX(worldIn, xCoordIn, yCoordIn, zCoordIn, Material.lava);
        }
    }

    public static class WaterFactory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_)
        {
            return new EntityDropParticleFX(worldIn, xCoordIn, yCoordIn, zCoordIn, Material.water);
        }
    }
}
