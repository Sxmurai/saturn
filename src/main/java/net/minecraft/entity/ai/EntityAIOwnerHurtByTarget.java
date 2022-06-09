package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAIOwnerHurtByTarget extends EntityAITarget
{
    EntityTameable theDefendingTameable;
    EntityLivingBase theOwnerAttacker;
    private int field_142051_e;

    public EntityAIOwnerHurtByTarget(EntityTameable theDefendingTameableIn)
    {
        super(theDefendingTameableIn, false);
        theDefendingTameable = theDefendingTameableIn;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (!theDefendingTameable.isTamed())
        {
            return false;
        }
        else
        {
            EntityLivingBase entitylivingbase = theDefendingTameable.getOwner();

            if (entitylivingbase == null)
            {
                return false;
            }
            else
            {
                theOwnerAttacker = entitylivingbase.getAITarget();
                int i = entitylivingbase.getRevengeTimer();
                return i != field_142051_e && isSuitableTarget(theOwnerAttacker, false) && theDefendingTameable.shouldAttackEntity(theOwnerAttacker, entitylivingbase);
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        taskOwner.setAttackTarget(theOwnerAttacker);
        EntityLivingBase entitylivingbase = theDefendingTameable.getOwner();

        if (entitylivingbase != null)
        {
            field_142051_e = entitylivingbase.getRevengeTimer();
        }

        super.startExecuting();
    }
}
