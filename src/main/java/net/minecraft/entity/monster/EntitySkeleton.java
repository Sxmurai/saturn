package net.minecraft.entity.monster;

import java.util.Calendar;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;

public class EntitySkeleton extends EntityMob implements IRangedAttackMob
{
    private final EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 1.0D, 20, 60, 15.0F);
    private final EntityAIAttackOnCollide aiAttackOnCollide = new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.2D, false);

    public EntitySkeleton(World worldIn)
    {
        super(worldIn);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIRestrictSun(this));
        tasks.addTask(3, new EntityAIFleeSun(this, 1.0D));
        tasks.addTask(3, new EntityAIAvoidEntity(this, EntityWolf.class, 6.0F, 1.0D, 1.2D));
        tasks.addTask(4, new EntityAIWander(this, 1.0D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(6, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityIronGolem.class, true));

        if (worldIn != null && !worldIn.isRemote)
        {
            setCombatTask();
        }
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(13, new Byte((byte)0));
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return "mob.skeleton.say";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.skeleton.hurt";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.skeleton.death";
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        playSound("mob.skeleton.step", 0.15F, 1.0F);
    }

    public boolean attackEntityAsMob(Entity entityIn)
    {
        if (super.attackEntityAsMob(entityIn))
        {
            if (getSkeletonType() == 1 && entityIn instanceof EntityLivingBase)
            {
                ((EntityLivingBase)entityIn).addPotionEffect(new PotionEffect(Potion.wither.id, 200));
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.UNDEAD;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        if (worldObj.isDaytime() && !worldObj.isRemote)
        {
            float f = getBrightness(1.0F);
            BlockPos blockpos = new BlockPos(posX, (double)Math.round(posY), posZ);

            if (f > 0.5F && rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && worldObj.canSeeSky(blockpos))
            {
                boolean flag = true;
                ItemStack itemstack = getEquipmentInSlot(4);

                if (itemstack != null)
                {
                    if (itemstack.isItemStackDamageable())
                    {
                        itemstack.setItemDamage(itemstack.getItemDamage() + rand.nextInt(2));

                        if (itemstack.getItemDamage() >= itemstack.getMaxDamage())
                        {
                            renderBrokenItemStack(itemstack);
                            setCurrentItemOrArmor(4, null);
                        }
                    }

                    flag = false;
                }

                if (flag)
                {
                    setFire(8);
                }
            }
        }

        if (worldObj.isRemote && getSkeletonType() == 1)
        {
            setSize(0.72F, 2.535F);
        }

        super.onLivingUpdate();
    }

    /**
     * Handles updating while being ridden by an entity
     */
    public void updateRidden()
    {
        super.updateRidden();

        if (ridingEntity instanceof EntityCreature)
        {
            EntityCreature entitycreature = (EntityCreature) ridingEntity;
            renderYawOffset = entitycreature.renderYawOffset;
        }
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);

        if (cause.getSourceOfDamage() instanceof EntityArrow && cause.getEntity() instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)cause.getEntity();
            double d0 = entityplayer.posX - posX;
            double d1 = entityplayer.posZ - posZ;

            if (d0 * d0 + d1 * d1 >= 2500.0D)
            {
                entityplayer.triggerAchievement(AchievementList.snipeSkeleton);
            }
        }
        else if (cause.getEntity() instanceof EntityCreeper && ((EntityCreeper)cause.getEntity()).getPowered() && ((EntityCreeper)cause.getEntity()).isAIEnabled())
        {
            ((EntityCreeper)cause.getEntity()).func_175493_co();
            entityDropItem(new ItemStack(Items.skull, 1, getSkeletonType() == 1 ? 1 : 0), 0.0F);
        }
    }

    protected Item getDropItem()
    {
        return Items.arrow;
    }

    /**
     * Drop 0-2 items of this living's type
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
        if (getSkeletonType() == 1)
        {
            int i = rand.nextInt(3 + p_70628_2_) - 1;

            for (int j = 0; j < i; ++j)
            {
                dropItem(Items.coal, 1);
            }
        }
        else
        {
            int k = rand.nextInt(3 + p_70628_2_);

            for (int i1 = 0; i1 < k; ++i1)
            {
                dropItem(Items.arrow, 1);
            }
        }

        int l = rand.nextInt(3 + p_70628_2_);

        for (int j1 = 0; j1 < l; ++j1)
        {
            dropItem(Items.bone, 1);
        }
    }

    /**
     * Causes this Entity to drop a random item.
     */
    protected void addRandomDrop()
    {
        if (getSkeletonType() == 1)
        {
            entityDropItem(new ItemStack(Items.skull, 1, 1), 0.0F);
        }
    }

    /**
     * Gives armor or weapon for entity based on given DifficultyInstance
     */
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty)
    {
        super.setEquipmentBasedOnDifficulty(difficulty);
        setCurrentItemOrArmor(0, new ItemStack(Items.bow));
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata)
    {
        livingdata = super.onInitialSpawn(difficulty, livingdata);

        if (worldObj.provider instanceof WorldProviderHell && getRNG().nextInt(5) > 0)
        {
            tasks.addTask(4, aiAttackOnCollide);
            setSkeletonType(1);
            setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
            getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4.0D);
        }
        else
        {
            tasks.addTask(4, aiArrowAttack);
            setEquipmentBasedOnDifficulty(difficulty);
            setEnchantmentBasedOnDifficulty(difficulty);
        }

        setCanPickUpLoot(rand.nextFloat() < 0.55F * difficulty.getClampedAdditionalDifficulty());

        if (getEquipmentInSlot(4) == null)
        {
            Calendar calendar = worldObj.getCurrentDate();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && rand.nextFloat() < 0.25F)
            {
                setCurrentItemOrArmor(4, new ItemStack(rand.nextFloat() < 0.1F ? Blocks.lit_pumpkin : Blocks.pumpkin));
                equipmentDropChances[4] = 0.0F;
            }
        }

        return livingdata;
    }

    /**
     * sets this entity's combat AI.
     */
    public void setCombatTask()
    {
        tasks.removeTask(aiAttackOnCollide);
        tasks.removeTask(aiArrowAttack);
        ItemStack itemstack = getHeldItem();

        if (itemstack != null && itemstack.getItem() == Items.bow)
        {
            tasks.addTask(4, aiArrowAttack);
        }
        else
        {
            tasks.addTask(4, aiAttackOnCollide);
        }
    }

    /**
     * Attack the specified entity using a ranged attack.
     */
    public void attackEntityWithRangedAttack(EntityLivingBase p_82196_1_, float p_82196_2_)
    {
        EntityArrow entityarrow = new EntityArrow(worldObj, this, p_82196_1_, 1.6F, (float)(14 - worldObj.getDifficulty().getDifficultyId() * 4));
        int i = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, getHeldItem());
        int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, getHeldItem());
        entityarrow.setDamage((double)(p_82196_2_ * 2.0F) + rand.nextGaussian() * 0.25D + (double)((float) worldObj.getDifficulty().getDifficultyId() * 0.11F));

        if (i > 0)
        {
            entityarrow.setDamage(entityarrow.getDamage() + (double)i * 0.5D + 0.5D);
        }

        if (j > 0)
        {
            entityarrow.setKnockbackStrength(j);
        }

        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, getHeldItem()) > 0 || getSkeletonType() == 1)
        {
            entityarrow.setFire(100);
        }

        playSound("random.bow", 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        worldObj.spawnEntityInWorld(entityarrow);
    }

    /**
     * Return this skeleton's type.
     */
    public int getSkeletonType()
    {
        return dataWatcher.getWatchableObjectByte(13);
    }

    /**
     * Set this skeleton's type.
     */
    public void setSkeletonType(int p_82201_1_)
    {
        dataWatcher.updateObject(13, Byte.valueOf((byte)p_82201_1_));
        isImmuneToFire = p_82201_1_ == 1;

        if (p_82201_1_ == 1)
        {
            setSize(0.72F, 2.535F);
        }
        else
        {
            setSize(0.6F, 1.95F);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("SkeletonType", 99))
        {
            int i = tagCompund.getByte("SkeletonType");
            setSkeletonType(i);
        }

        setCombatTask();
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setByte("SkeletonType", (byte) getSkeletonType());
    }

    /**
     * Sets the held item, or an armor slot. Slot 0 is held item. Slot 1-4 is armor. Params: Item, slot
     */
    public void setCurrentItemOrArmor(int slotIn, ItemStack stack)
    {
        super.setCurrentItemOrArmor(slotIn, stack);

        if (!worldObj.isRemote && slotIn == 0)
        {
            setCombatTask();
        }
    }

    public float getEyeHeight()
    {
        return getSkeletonType() == 1 ? super.getEyeHeight() : 1.74F;
    }

    /**
     * Returns the Y Offset of this entity.
     */
    public double getYOffset()
    {
        return isChild() ? 0.0D : -0.35D;
    }
}
