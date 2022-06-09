package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;

public class EntityAIHurtByTarget extends EntityAITarget
{
    private final boolean entityCallsForHelp;

    /** Store the previous revengeTimer value */
    private int revengeTimerOld;
    private final Class[] targetClasses;

    public EntityAIHurtByTarget(EntityCreature creatureIn, boolean entityCallsForHelpIn, Class... targetClassesIn)
    {
        super(creatureIn, false);
        entityCallsForHelp = entityCallsForHelpIn;
        targetClasses = targetClassesIn;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        int i = taskOwner.getRevengeTimer();
        return i != revengeTimerOld && isSuitableTarget(taskOwner.getAITarget(), false);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        taskOwner.setAttackTarget(taskOwner.getAITarget());
        revengeTimerOld = taskOwner.getRevengeTimer();

        if (entityCallsForHelp)
        {
            double d0 = getTargetDistance();

            for (EntityCreature entitycreature : taskOwner.worldObj.getEntitiesWithinAABB(taskOwner.getClass(), (new AxisAlignedBB(taskOwner.posX, taskOwner.posY, taskOwner.posZ, taskOwner.posX + 1.0D, taskOwner.posY + 1.0D, taskOwner.posZ + 1.0D)).expand(d0, 10.0D, d0)))
            {
                if (taskOwner != entitycreature && entitycreature.getAttackTarget() == null && !entitycreature.isOnSameTeam(taskOwner.getAITarget()))
                {
                    boolean flag = false;

                    for (Class oclass : targetClasses)
                    {
                        if (entitycreature.getClass() == oclass)
                        {
                            flag = true;
                            break;
                        }
                    }

                    if (!flag)
                    {
                        setEntityAttackTarget(entitycreature, taskOwner.getAITarget());
                    }
                }
            }
        }

        super.startExecuting();
    }

    protected void setEntityAttackTarget(EntityCreature creatureIn, EntityLivingBase entityLivingBaseIn)
    {
        creatureIn.setAttackTarget(entityLivingBaseIn);
    }
}
