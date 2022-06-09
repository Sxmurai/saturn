package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowOwner extends EntityAIBase
{
    private final EntityTameable thePet;
    private EntityLivingBase theOwner;
    World theWorld;
    private final double followSpeed;
    private final PathNavigate petPathfinder;
    private int field_75343_h;
    float maxDist;
    float minDist;
    private boolean field_75344_i;

    public EntityAIFollowOwner(EntityTameable thePetIn, double followSpeedIn, float minDistIn, float maxDistIn)
    {
        thePet = thePetIn;
        theWorld = thePetIn.worldObj;
        followSpeed = followSpeedIn;
        petPathfinder = thePetIn.getNavigator();
        minDist = minDistIn;
        maxDist = maxDistIn;
        setMutexBits(3);

        if (!(thePetIn.getNavigator() instanceof PathNavigateGround))
        {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = thePet.getOwner();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer)entitylivingbase).isSpectator())
        {
            return false;
        }
        else if (thePet.isSitting())
        {
            return false;
        }
        else if (thePet.getDistanceSqToEntity(entitylivingbase) < (double)(minDist * minDist))
        {
            return false;
        }
        else
        {
            theOwner = entitylivingbase;
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !petPathfinder.noPath() && thePet.getDistanceSqToEntity(theOwner) > (double)(maxDist * maxDist) && !thePet.isSitting();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        field_75343_h = 0;
        field_75344_i = ((PathNavigateGround) thePet.getNavigator()).getAvoidsWater();
        ((PathNavigateGround) thePet.getNavigator()).setAvoidsWater(false);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        theOwner = null;
        petPathfinder.clearPathEntity();
        ((PathNavigateGround) thePet.getNavigator()).setAvoidsWater(true);
    }

    private boolean func_181065_a(BlockPos p_181065_1_)
    {
        IBlockState iblockstate = theWorld.getBlockState(p_181065_1_);
        Block block = iblockstate.getBlock();
        return block == Blocks.air || !block.isFullCube();
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        thePet.getLookHelper().setLookPositionWithEntity(theOwner, 10.0F, (float) thePet.getVerticalFaceSpeed());

        if (!thePet.isSitting())
        {
            if (--field_75343_h <= 0)
            {
                field_75343_h = 10;

                if (!petPathfinder.tryMoveToEntityLiving(theOwner, followSpeed))
                {
                    if (!thePet.getLeashed())
                    {
                        if (thePet.getDistanceSqToEntity(theOwner) >= 144.0D)
                        {
                            int i = MathHelper.floor_double(theOwner.posX) - 2;
                            int j = MathHelper.floor_double(theOwner.posZ) - 2;
                            int k = MathHelper.floor_double(theOwner.getEntityBoundingBox().minY);

                            for (int l = 0; l <= 4; ++l)
                            {
                                for (int i1 = 0; i1 <= 4; ++i1)
                                {
                                    if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && World.doesBlockHaveSolidTopSurface(theWorld, new BlockPos(i + l, k - 1, j + i1)) && func_181065_a(new BlockPos(i + l, k, j + i1)) && func_181065_a(new BlockPos(i + l, k + 1, j + i1)))
                                    {
                                        thePet.setLocationAndAngles((float)(i + l) + 0.5F, k, (float)(j + i1) + 0.5F, thePet.rotationYaw, thePet.rotationPitch);
                                        petPathfinder.clearPathEntity();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
