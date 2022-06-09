package net.minecraft.entity;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityFlying extends EntityLiving
{
    public EntityFlying(World worldIn)
    {
        super(worldIn);
    }

    public void fall(float distance, float damageMultiplier)
    {
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos)
    {
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    public void moveEntityWithHeading(float strafe, float forward)
    {
        if (isInWater())
        {
            moveFlying(strafe, forward, 0.02F);
            moveEntity(motionX, motionY, motionZ);
            motionX *= 0.800000011920929D;
            motionY *= 0.800000011920929D;
            motionZ *= 0.800000011920929D;
        }
        else if (isInLava())
        {
            moveFlying(strafe, forward, 0.02F);
            moveEntity(motionX, motionY, motionZ);
            motionX *= 0.5D;
            motionY *= 0.5D;
            motionZ *= 0.5D;
        }
        else
        {
            float f = 0.91F;

            if (onGround)
            {
                f = worldObj.getBlockState(new BlockPos(MathHelper.floor_double(posX), MathHelper.floor_double(getEntityBoundingBox().minY) - 1, MathHelper.floor_double(posZ))).getBlock().slipperiness * 0.91F;
            }

            float f1 = 0.16277136F / (f * f * f);
            moveFlying(strafe, forward, onGround ? 0.1F * f1 : 0.02F);
            f = 0.91F;

            if (onGround)
            {
                f = worldObj.getBlockState(new BlockPos(MathHelper.floor_double(posX), MathHelper.floor_double(getEntityBoundingBox().minY) - 1, MathHelper.floor_double(posZ))).getBlock().slipperiness * 0.91F;
            }

            moveEntity(motionX, motionY, motionZ);
            motionX *= f;
            motionY *= f;
            motionZ *= f;
        }

        prevLimbSwingAmount = limbSwingAmount;
        double d1 = posX - prevPosX;
        double d0 = posZ - prevPosZ;
        float f2 = MathHelper.sqrt_double(d1 * d1 + d0 * d0) * 4.0F;

        if (f2 > 1.0F)
        {
            f2 = 1.0F;
        }

        limbSwingAmount += (f2 - limbSwingAmount) * 0.4F;
        limbSwing += limbSwingAmount;
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    public boolean isOnLadder()
    {
        return false;
    }
}
