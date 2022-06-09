package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.Vec3;

public class EntityAIAvoidEntity<T extends Entity> extends EntityAIBase
{
    private final Predicate<Entity> canBeSeenSelector;

    /** The entity we are attached to */
    protected EntityCreature theEntity;
    private final double farSpeed;
    private final double nearSpeed;
    protected T closestLivingEntity;
    private final float avoidDistance;

    /** The PathEntity of our entity */
    private PathEntity entityPathEntity;

    /** The PathNavigate of our entity */
    private final PathNavigate entityPathNavigate;
    private final Class<T> field_181064_i;
    private final Predicate <? super T > avoidTargetSelector;

    public EntityAIAvoidEntity(EntityCreature p_i46404_1_, Class<T> p_i46404_2_, float p_i46404_3_, double p_i46404_4_, double p_i46404_6_)
    {
        this(p_i46404_1_, p_i46404_2_, Predicates.alwaysTrue(), p_i46404_3_, p_i46404_4_, p_i46404_6_);
    }

    public EntityAIAvoidEntity(EntityCreature p_i46405_1_, Class<T> p_i46405_2_, Predicate <? super T > p_i46405_3_, float p_i46405_4_, double p_i46405_5_, double p_i46405_7_)
    {
        canBeSeenSelector = new Predicate<Entity>()
        {
            public boolean apply(Entity p_apply_1_)
            {
                return p_apply_1_.isEntityAlive() && theEntity.getEntitySenses().canSee(p_apply_1_);
            }
        };
        theEntity = p_i46405_1_;
        field_181064_i = p_i46405_2_;
        avoidTargetSelector = p_i46405_3_;
        avoidDistance = p_i46405_4_;
        farSpeed = p_i46405_5_;
        nearSpeed = p_i46405_7_;
        entityPathNavigate = p_i46405_1_.getNavigator();
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        List<T> list = theEntity.worldObj.getEntitiesWithinAABB(field_181064_i, theEntity.getEntityBoundingBox().expand(avoidDistance, 3.0D, avoidDistance), Predicates.and(new Predicate[] {EntitySelectors.NOT_SPECTATING, canBeSeenSelector, avoidTargetSelector}));

        if (list.isEmpty())
        {
            return false;
        }
        else
        {
            closestLivingEntity = list.get(0);
            Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(theEntity, 16, 7, new Vec3(closestLivingEntity.posX, closestLivingEntity.posY, closestLivingEntity.posZ));

            if (vec3 == null)
            {
                return false;
            }
            else if (closestLivingEntity.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord) < closestLivingEntity.getDistanceSqToEntity(theEntity))
            {
                return false;
            }
            else
            {
                entityPathEntity = entityPathNavigate.getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
                return entityPathEntity != null && entityPathEntity.isDestinationSame(vec3);
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !entityPathNavigate.noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        entityPathNavigate.setPath(entityPathEntity, farSpeed);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        closestLivingEntity = null;
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        if (theEntity.getDistanceSqToEntity(closestLivingEntity) < 49.0D)
        {
            theEntity.getNavigator().setSpeed(nearSpeed);
        }
        else
        {
            theEntity.getNavigator().setSpeed(farSpeed);
        }
    }
}
