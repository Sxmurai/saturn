package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNavigateGround;

public class EntityAISwimming extends EntityAIBase
{
    private final EntityLiving theEntity;

    public EntityAISwimming(EntityLiving entitylivingIn)
    {
        theEntity = entitylivingIn;
        setMutexBits(4);
        ((PathNavigateGround)entitylivingIn.getNavigator()).setCanSwim(true);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        return theEntity.isInWater() || theEntity.isInLava();
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        if (theEntity.getRNG().nextFloat() < 0.8F)
        {
            theEntity.getJumpHelper().setJumping();
        }
    }
}
