package net.minecraft.entity.effect;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityLightningBolt extends EntityWeatherEffect
{
    /**
     * Declares which state the lightning bolt is in. Whether it's in the air, hit the ground, etc.
     */
    private int lightningState;

    /**
     * A random long that is used to change the vertex of the lightning rendered in RenderLightningBolt
     */
    public long boltVertex;

    /**
     * Determines the time before the EntityLightningBolt is destroyed. It is a random integer decremented over time.
     */
    private int boltLivingTime;

    public EntityLightningBolt(World worldIn, double posX, double posY, double posZ)
    {
        super(worldIn);
        setLocationAndAngles(posX, posY, posZ, 0.0F, 0.0F);
        lightningState = 2;
        boltVertex = rand.nextLong();
        boltLivingTime = rand.nextInt(3) + 1;
        BlockPos blockpos = new BlockPos(this);

        if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doFireTick") && (worldIn.getDifficulty() == EnumDifficulty.NORMAL || worldIn.getDifficulty() == EnumDifficulty.HARD) && worldIn.isAreaLoaded(blockpos, 10))
        {
            if (worldIn.getBlockState(blockpos).getBlock().getMaterial() == Material.air && Blocks.fire.canPlaceBlockAt(worldIn, blockpos))
            {
                worldIn.setBlockState(blockpos, Blocks.fire.getDefaultState());
            }

            for (int i = 0; i < 4; ++i)
            {
                BlockPos blockpos1 = blockpos.add(rand.nextInt(3) - 1, rand.nextInt(3) - 1, rand.nextInt(3) - 1);

                if (worldIn.getBlockState(blockpos1).getBlock().getMaterial() == Material.air && Blocks.fire.canPlaceBlockAt(worldIn, blockpos1))
                {
                    worldIn.setBlockState(blockpos1, Blocks.fire.getDefaultState());
                }
            }
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (lightningState == 2)
        {
            worldObj.playSoundEffect(posX, posY, posZ, "ambient.weather.thunder", 10000.0F, 0.8F + rand.nextFloat() * 0.2F);
            worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 2.0F, 0.5F + rand.nextFloat() * 0.2F);
        }

        --lightningState;

        if (lightningState < 0)
        {
            if (boltLivingTime == 0)
            {
                setDead();
            }
            else if (lightningState < -rand.nextInt(10))
            {
                --boltLivingTime;
                lightningState = 1;
                boltVertex = rand.nextLong();
                BlockPos blockpos = new BlockPos(this);

                if (!worldObj.isRemote && worldObj.getGameRules().getBoolean("doFireTick") && worldObj.isAreaLoaded(blockpos, 10) && worldObj.getBlockState(blockpos).getBlock().getMaterial() == Material.air && Blocks.fire.canPlaceBlockAt(worldObj, blockpos))
                {
                    worldObj.setBlockState(blockpos, Blocks.fire.getDefaultState());
                }
            }
        }

        if (lightningState >= 0)
        {
            if (worldObj.isRemote)
            {
                worldObj.setLastLightningBolt(2);
            }
            else
            {
                double d0 = 3.0D;
                List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(posX - d0, posY - d0, posZ - d0, posX + d0, posY + 6.0D + d0, posZ + d0));

                for (int i = 0; i < list.size(); ++i)
                {
                    Entity entity = list.get(i);
                    entity.onStruckByLightning(this);
                }
            }
        }
    }

    protected void entityInit()
    {
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound tagCompund)
    {
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound tagCompound)
    {
    }
}
