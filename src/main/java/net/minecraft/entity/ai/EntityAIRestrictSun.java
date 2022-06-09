package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.PathNavigateGround;

public class EntityAIRestrictSun extends EntityAIBase
{
    private final EntityCreature theEntity;

    public EntityAIRestrictSun(EntityCreature p_i1652_1_)
    {
        theEntity = p_i1652_1_;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        return theEntity.worldObj.isDaytime();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        ((PathNavigateGround) theEntity.getNavigator()).setAvoidSun(true);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        ((PathNavigateGround) theEntity.getNavigator()).setAvoidSun(false);
    }
}
