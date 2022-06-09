package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

public class EntityAIMoveIndoors extends EntityAIBase
{
    private final EntityCreature entityObj;
    private VillageDoorInfo doorInfo;
    private int insidePosX = -1;
    private int insidePosZ = -1;

    public EntityAIMoveIndoors(EntityCreature entityObjIn)
    {
        entityObj = entityObjIn;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        BlockPos blockpos = new BlockPos(entityObj);

        if ((!entityObj.worldObj.isDaytime() || entityObj.worldObj.isRaining() && !entityObj.worldObj.getBiomeGenForCoords(blockpos).canSpawnLightningBolt()) && !entityObj.worldObj.provider.getHasNoSky())
        {
            if (entityObj.getRNG().nextInt(50) != 0)
            {
                return false;
            }
            else if (insidePosX != -1 && entityObj.getDistanceSq(insidePosX, entityObj.posY, insidePosZ) < 4.0D)
            {
                return false;
            }
            else
            {
                Village village = entityObj.worldObj.getVillageCollection().getNearestVillage(blockpos, 14);

                if (village == null)
                {
                    return false;
                }
                else
                {
                    doorInfo = village.getDoorInfo(blockpos);
                    return doorInfo != null;
                }
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !entityObj.getNavigator().noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        insidePosX = -1;
        BlockPos blockpos = doorInfo.getInsideBlockPos();
        int i = blockpos.getX();
        int j = blockpos.getY();
        int k = blockpos.getZ();

        if (entityObj.getDistanceSq(blockpos) > 256.0D)
        {
            Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(entityObj, 14, 3, new Vec3((double)i + 0.5D, j, (double)k + 0.5D));

            if (vec3 != null)
            {
                entityObj.getNavigator().tryMoveToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord, 1.0D);
            }
        }
        else
        {
            entityObj.getNavigator().tryMoveToXYZ((double)i + 0.5D, j, (double)k + 0.5D, 1.0D);
        }
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        insidePosX = doorInfo.getInsideBlockPos().getX();
        insidePosZ = doorInfo.getInsideBlockPos().getZ();
        doorInfo = null;
    }
}
