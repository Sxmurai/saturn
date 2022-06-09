package net.minecraft.entity.monster;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntitySpider extends EntityMob
{
    public EntitySpider(World worldIn)
    {
        super(worldIn);
        setSize(1.4F, 0.9F);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(3, new EntityAILeapAtTarget(this, 0.4F));
        tasks.addTask(4, new EntitySpider.AISpiderAttack(this, EntityPlayer.class));
        tasks.addTask(4, new EntitySpider.AISpiderAttack(this, EntityIronGolem.class));
        tasks.addTask(5, new EntityAIWander(this, 0.8D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(6, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntitySpider.AISpiderTarget(this, EntityPlayer.class));
        targetTasks.addTask(3, new EntitySpider.AISpiderTarget(this, EntityIronGolem.class));
    }

    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
    public double getMountedYOffset()
    {
        return height * 0.5F;
    }

    /**
     * Returns new PathNavigateGround instance
     */
    protected PathNavigate getNewNavigator(World worldIn)
    {
        return new PathNavigateClimber(this, worldIn);
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(16, new Byte((byte)0));
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (!worldObj.isRemote)
        {
            setBesideClimbableBlock(isCollidedHorizontally);
        }
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(16.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return "mob.spider.say";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.spider.say";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.spider.death";
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        playSound("mob.spider.step", 0.15F, 1.0F);
    }

    protected Item getDropItem()
    {
        return Items.string;
    }

    /**
     * Drop 0-2 items of this living's type
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
        super.dropFewItems(p_70628_1_, p_70628_2_);

        if (p_70628_1_ && (rand.nextInt(3) == 0 || rand.nextInt(1 + p_70628_2_) > 0))
        {
            dropItem(Items.spider_eye, 1);
        }
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    public boolean isOnLadder()
    {
        return isBesideClimbableBlock();
    }

    /**
     * Sets the Entity inside a web block.
     */
    public void setInWeb()
    {
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    public boolean isPotionApplicable(PotionEffect potioneffectIn)
    {
        return potioneffectIn.getPotionID() != Potion.poison.id && super.isPotionApplicable(potioneffectIn);
    }

    /**
     * Returns true if the WatchableObject (Byte) is 0x01 otherwise returns false. The WatchableObject is updated using
     * setBesideClimableBlock.
     */
    public boolean isBesideClimbableBlock()
    {
        return (dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    /**
     * Updates the WatchableObject (Byte) created in entityInit(), setting it to 0x01 if par1 is true or 0x00 if it is
     * false.
     */
    public void setBesideClimbableBlock(boolean p_70839_1_)
    {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (p_70839_1_)
        {
            b0 = (byte)(b0 | 1);
        }
        else
        {
            b0 = (byte)(b0 & -2);
        }

        dataWatcher.updateObject(16, Byte.valueOf(b0));
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata)
    {
        livingdata = super.onInitialSpawn(difficulty, livingdata);

        if (worldObj.rand.nextInt(100) == 0)
        {
            EntitySkeleton entityskeleton = new EntitySkeleton(worldObj);
            entityskeleton.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);
            entityskeleton.onInitialSpawn(difficulty, null);
            worldObj.spawnEntityInWorld(entityskeleton);
            entityskeleton.mountEntity(this);
        }

        if (livingdata == null)
        {
            livingdata = new EntitySpider.GroupData();

            if (worldObj.getDifficulty() == EnumDifficulty.HARD && worldObj.rand.nextFloat() < 0.1F * difficulty.getClampedAdditionalDifficulty())
            {
                ((EntitySpider.GroupData)livingdata).func_111104_a(worldObj.rand);
            }
        }

        if (livingdata instanceof EntitySpider.GroupData)
        {
            int i = ((EntitySpider.GroupData)livingdata).potionEffectId;

            if (i > 0 && Potion.potionTypes[i] != null)
            {
                addPotionEffect(new PotionEffect(i, Integer.MAX_VALUE));
            }
        }

        return livingdata;
    }

    public float getEyeHeight()
    {
        return 0.65F;
    }

    static class AISpiderAttack extends EntityAIAttackOnCollide
    {
        public AISpiderAttack(EntitySpider p_i45819_1_, Class <? extends Entity > targetClass)
        {
            super(p_i45819_1_, targetClass, 1.0D, true);
        }

        public boolean continueExecuting()
        {
            float f = attacker.getBrightness(1.0F);

            if (f >= 0.5F && attacker.getRNG().nextInt(100) == 0)
            {
                attacker.setAttackTarget(null);
                return false;
            }
            else
            {
                return super.continueExecuting();
            }
        }

        protected double func_179512_a(EntityLivingBase attackTarget)
        {
            return 4.0F + attackTarget.width;
        }
    }

    static class AISpiderTarget<T extends EntityLivingBase> extends EntityAINearestAttackableTarget
    {
        public AISpiderTarget(EntitySpider p_i45818_1_, Class<T> classTarget)
        {
            super(p_i45818_1_, classTarget, true);
        }

        public boolean shouldExecute()
        {
            float f = taskOwner.getBrightness(1.0F);
            return !(f >= 0.5F) && super.shouldExecute();
        }
    }

    public static class GroupData implements IEntityLivingData
    {
        public int potionEffectId;

        public void func_111104_a(Random rand)
        {
            int i = rand.nextInt(5);

            if (i <= 1)
            {
                potionEffectId = Potion.moveSpeed.id;
            }
            else if (i <= 2)
            {
                potionEffectId = Potion.damageBoost.id;
            }
            else if (i <= 3)
            {
                potionEffectId = Potion.regeneration.id;
            }
            else if (i <= 4)
            {
                potionEffectId = Potion.invisibility.id;
            }
        }
    }
}
