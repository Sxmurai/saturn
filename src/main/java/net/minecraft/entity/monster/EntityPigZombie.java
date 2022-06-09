package net.minecraft.entity.monster;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityPigZombie extends EntityZombie
{
    private static final UUID ATTACK_SPEED_BOOST_MODIFIER_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
    private static final AttributeModifier ATTACK_SPEED_BOOST_MODIFIER = (new AttributeModifier(EntityPigZombie.ATTACK_SPEED_BOOST_MODIFIER_UUID, "Attacking speed boost", 0.05D, 0)).setSaved(false);

    /** Above zero if this PigZombie is Angry. */
    private int angerLevel;

    /** A random delay until this PigZombie next makes a sound. */
    private int randomSoundDelay;
    private UUID angerTargetUUID;

    public EntityPigZombie(World worldIn)
    {
        super(worldIn);
        isImmuneToFire = true;
    }

    public void setRevengeTarget(EntityLivingBase livingBase)
    {
        super.setRevengeTarget(livingBase);

        if (livingBase != null)
        {
            angerTargetUUID = livingBase.getUniqueID();
        }
    }

    protected void applyEntityAI()
    {
        targetTasks.addTask(1, new EntityPigZombie.AIHurtByAggressor(this));
        targetTasks.addTask(2, new EntityPigZombie.AITargetAggressor(this));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(EntityZombie.reinforcementChance).setBaseValue(0.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23000000417232513D);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(5.0D);
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();
    }

    protected void updateAITasks()
    {
        IAttributeInstance iattributeinstance = getEntityAttribute(SharedMonsterAttributes.movementSpeed);

        if (isAngry())
        {
            if (!isChild() && !iattributeinstance.hasModifier(EntityPigZombie.ATTACK_SPEED_BOOST_MODIFIER))
            {
                iattributeinstance.applyModifier(EntityPigZombie.ATTACK_SPEED_BOOST_MODIFIER);
            }

            --angerLevel;
        }
        else if (iattributeinstance.hasModifier(EntityPigZombie.ATTACK_SPEED_BOOST_MODIFIER))
        {
            iattributeinstance.removeModifier(EntityPigZombie.ATTACK_SPEED_BOOST_MODIFIER);
        }

        if (randomSoundDelay > 0 && --randomSoundDelay == 0)
        {
            playSound("mob.zombiepig.zpigangry", getSoundVolume() * 2.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
        }

        if (angerLevel > 0 && angerTargetUUID != null && getAITarget() == null)
        {
            EntityPlayer entityplayer = worldObj.getPlayerEntityByUUID(angerTargetUUID);
            setRevengeTarget(entityplayer);
            attackingPlayer = entityplayer;
            recentlyHit = getRevengeTimer();
        }

        super.updateAITasks();
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
        return worldObj.getDifficulty() != EnumDifficulty.PEACEFUL;
    }

    /**
     * Checks that the entity is not colliding with any blocks / liquids
     */
    public boolean isNotColliding()
    {
        return worldObj.checkNoEntityCollision(getEntityBoundingBox(), this) && worldObj.getCollidingBoundingBoxes(this, getEntityBoundingBox()).isEmpty() && !worldObj.isAnyLiquid(getEntityBoundingBox());
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setShort("Anger", (short) angerLevel);

        if (angerTargetUUID != null)
        {
            tagCompound.setString("HurtBy", angerTargetUUID.toString());
        }
        else
        {
            tagCompound.setString("HurtBy", "");
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        angerLevel = tagCompund.getShort("Anger");
        String s = tagCompund.getString("HurtBy");

        if (s.length() > 0)
        {
            angerTargetUUID = UUID.fromString(s);
            EntityPlayer entityplayer = worldObj.getPlayerEntityByUUID(angerTargetUUID);
            setRevengeTarget(entityplayer);

            if (entityplayer != null)
            {
                attackingPlayer = entityplayer;
                recentlyHit = getRevengeTimer();
            }
        }
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            Entity entity = source.getEntity();

            if (entity instanceof EntityPlayer)
            {
                becomeAngryAt(entity);
            }

            return super.attackEntityFrom(source, amount);
        }
    }

    /**
     * Causes this PigZombie to become angry at the supplied Entity (which will be a player).
     */
    private void becomeAngryAt(Entity p_70835_1_)
    {
        angerLevel = 400 + rand.nextInt(400);
        randomSoundDelay = rand.nextInt(40);

        if (p_70835_1_ instanceof EntityLivingBase)
        {
            setRevengeTarget((EntityLivingBase)p_70835_1_);
        }
    }

    public boolean isAngry()
    {
        return angerLevel > 0;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return "mob.zombiepig.zpig";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.zombiepig.zpighurt";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.zombiepig.zpigdeath";
    }

    /**
     * Drop 0-2 items of this living's type
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
        int i = rand.nextInt(2 + p_70628_2_);

        for (int j = 0; j < i; ++j)
        {
            dropItem(Items.rotten_flesh, 1);
        }

        i = rand.nextInt(2 + p_70628_2_);

        for (int k = 0; k < i; ++k)
        {
            dropItem(Items.gold_nugget, 1);
        }
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean interact(EntityPlayer player)
    {
        return false;
    }

    /**
     * Causes this Entity to drop a random item.
     */
    protected void addRandomDrop()
    {
        dropItem(Items.gold_ingot, 1);
    }

    /**
     * Gives armor or weapon for entity based on given DifficultyInstance
     */
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty)
    {
        setCurrentItemOrArmor(0, new ItemStack(Items.golden_sword));
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata)
    {
        super.onInitialSpawn(difficulty, livingdata);
        setVillager(false);
        return livingdata;
    }

    static class AIHurtByAggressor extends EntityAIHurtByTarget
    {
        public AIHurtByAggressor(EntityPigZombie p_i45828_1_)
        {
            super(p_i45828_1_, true);
        }

        protected void setEntityAttackTarget(EntityCreature creatureIn, EntityLivingBase entityLivingBaseIn)
        {
            super.setEntityAttackTarget(creatureIn, entityLivingBaseIn);

            if (creatureIn instanceof EntityPigZombie)
            {
                ((EntityPigZombie)creatureIn).becomeAngryAt(entityLivingBaseIn);
            }
        }
    }

    static class AITargetAggressor extends EntityAINearestAttackableTarget<EntityPlayer>
    {
        public AITargetAggressor(EntityPigZombie p_i45829_1_)
        {
            super(p_i45829_1_, EntityPlayer.class, true);
        }

        public boolean shouldExecute()
        {
            return ((EntityPigZombie) taskOwner).isAngry() && super.shouldExecute();
        }
    }
}
