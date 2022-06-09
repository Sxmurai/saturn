package net.minecraft.entity;

import java.util.UUID;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityJumpHelper;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import optifine.BlockPosM;
import optifine.Config;
import optifine.Reflector;

public abstract class EntityLiving extends EntityLivingBase
{
    /** Number of ticks since this EntityLiving last produced its sound */
    public int livingSoundTime;

    /** The experience points the Entity gives. */
    protected int experienceValue;
    private final EntityLookHelper lookHelper;
    protected EntityMoveHelper moveHelper;

    /** Entity jumping helper */
    protected EntityJumpHelper jumpHelper;
    private final EntityBodyHelper bodyHelper;
    protected PathNavigate navigator;

    /** Passive tasks (wandering, look, idle, ...) */
    protected final EntityAITasks tasks;

    /** Fighting tasks (used by monsters, wolves, ocelots) */
    protected final EntityAITasks targetTasks;

    /** The active target the Task system uses for tracking */
    private EntityLivingBase attackTarget;
    private final EntitySenses senses;

    /** Equipment (armor and held item) for this entity. */
    private final ItemStack[] equipment = new ItemStack[5];

    /** Chances for each equipment piece from dropping when this entity dies. */
    protected float[] equipmentDropChances = new float[5];

    /** Whether this entity can pick up items from the ground. */
    private boolean canPickUpLoot;

    /** Whether this entity should NOT despawn. */
    private boolean persistenceRequired;
    private boolean isLeashed;
    private Entity leashedToEntity;
    private NBTTagCompound leashNBTTag;
    private static final String __OBFID = "CL_00001550";
    public int randomMobsId = 0;
    public BiomeGenBase spawnBiome = null;
    public BlockPos spawnPosition = null;

    public EntityLiving(World worldIn)
    {
        super(worldIn);
        tasks = new EntityAITasks(worldIn != null && worldIn.theProfiler != null ? worldIn.theProfiler : null);
        targetTasks = new EntityAITasks(worldIn != null && worldIn.theProfiler != null ? worldIn.theProfiler : null);
        lookHelper = new EntityLookHelper(this);
        moveHelper = new EntityMoveHelper(this);
        jumpHelper = new EntityJumpHelper(this);
        bodyHelper = new EntityBodyHelper(this);
        navigator = getNewNavigator(worldIn);
        senses = new EntitySenses(this);

        for (int i = 0; i < equipmentDropChances.length; ++i)
        {
            equipmentDropChances[i] = 0.085F;
        }

        UUID uuid = getUniqueID();
        long j = uuid.getLeastSignificantBits();
        randomMobsId = (int)(j & 2147483647L);
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.followRange).setBaseValue(16.0D);
    }

    /**
     * Returns new PathNavigateGround instance
     */
    protected PathNavigate getNewNavigator(World worldIn)
    {
        return new PathNavigateGround(this, worldIn);
    }

    public EntityLookHelper getLookHelper()
    {
        return lookHelper;
    }

    public EntityMoveHelper getMoveHelper()
    {
        return moveHelper;
    }

    public EntityJumpHelper getJumpHelper()
    {
        return jumpHelper;
    }

    public PathNavigate getNavigator()
    {
        return navigator;
    }

    /**
     * returns the EntitySenses Object for the EntityLiving
     */
    public EntitySenses getEntitySenses()
    {
        return senses;
    }

    /**
     * Gets the active target the Task system uses for tracking
     */
    public EntityLivingBase getAttackTarget()
    {
        return attackTarget;
    }

    /**
     * Sets the active target the Task system uses for tracking
     */
    public void setAttackTarget(EntityLivingBase entitylivingbaseIn)
    {
        attackTarget = entitylivingbaseIn;
        Reflector.callVoid(Reflector.ForgeHooks_onLivingSetAttackTarget, this, entitylivingbaseIn);
    }

    /**
     * Returns true if this entity can attack entities of the specified class.
     */
    public boolean canAttackClass(Class cls)
    {
        return cls != EntityGhast.class;
    }

    /**
     * This function applies the benefits of growing back wool and faster growing up to the acting entity. (This
     * function is used in the AIEatGrass)
     */
    public void eatGrassBonus()
    {
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(15, Byte.valueOf((byte)0));
    }

    /**
     * Get number of ticks, at least during which the living entity will be silent.
     */
    public int getTalkInterval()
    {
        return 80;
    }

    /**
     * Plays living's sound at its position
     */
    public void playLivingSound()
    {
        String s = getLivingSound();

        if (s != null)
        {
            playSound(s, getSoundVolume(), getSoundPitch());
        }
    }

    /**
     * Gets called every tick from main Entity class
     */
    public void onEntityUpdate()
    {
        super.onEntityUpdate();
        worldObj.theProfiler.startSection("mobBaseTick");

        if (isEntityAlive() && rand.nextInt(1000) < livingSoundTime++)
        {
            livingSoundTime = -getTalkInterval();
            playLivingSound();
        }

        worldObj.theProfiler.endSection();
    }

    /**
     * Get the experience points the entity currently has.
     */
    protected int getExperiencePoints(EntityPlayer player)
    {
        if (experienceValue > 0)
        {
            int i = experienceValue;
            ItemStack[] aitemstack = getInventory();

            for (int j = 0; j < aitemstack.length; ++j)
            {
                if (aitemstack[j] != null && equipmentDropChances[j] <= 1.0F)
                {
                    i += 1 + rand.nextInt(3);
                }
            }

            return i;
        }
        else
        {
            return experienceValue;
        }
    }

    /**
     * Spawns an explosion particle around the Entity's location
     */
    public void spawnExplosionParticle()
    {
        if (worldObj.isRemote)
        {
            for (int i = 0; i < 20; ++i)
            {
                double d0 = rand.nextGaussian() * 0.02D;
                double d1 = rand.nextGaussian() * 0.02D;
                double d2 = rand.nextGaussian() * 0.02D;
                double d3 = 10.0D;
                worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, posX + (double)(rand.nextFloat() * width * 2.0F) - (double) width - d0 * d3, posY + (double)(rand.nextFloat() * height) - d1 * d3, posZ + (double)(rand.nextFloat() * width * 2.0F) - (double) width - d2 * d3, d0, d1, d2);
            }
        }
        else
        {
            worldObj.setEntityState(this, (byte)20);
        }
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 20)
        {
            spawnExplosionParticle();
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        if (Config.isSmoothWorld() && canSkipUpdate())
        {
            onUpdateMinimal();
        }
        else
        {
            super.onUpdate();

            if (!worldObj.isRemote)
            {
                updateLeashedState();
            }
        }
    }

    protected float func_110146_f(float p_110146_1_, float p_110146_2_)
    {
        bodyHelper.updateRenderAngles();
        return p_110146_2_;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return null;
    }

    protected Item getDropItem()
    {
        return null;
    }

    /**
     * Drop 0-2 items of this living's type
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
        Item item = getDropItem();

        if (item != null)
        {
            int i = rand.nextInt(3);

            if (p_70628_2_ > 0)
            {
                i += rand.nextInt(p_70628_2_ + 1);
            }

            for (int j = 0; j < i; ++j)
            {
                dropItem(item, 1);
            }
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("CanPickUpLoot", canPickUpLoot());
        tagCompound.setBoolean("PersistenceRequired", persistenceRequired);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < equipment.length; ++i)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            if (equipment[i] != null)
            {
                equipment[i].writeToNBT(nbttagcompound);
            }

            nbttaglist.appendTag(nbttagcompound);
        }

        tagCompound.setTag("Equipment", nbttaglist);
        NBTTagList nbttaglist1 = new NBTTagList();

        for (int j = 0; j < equipmentDropChances.length; ++j)
        {
            nbttaglist1.appendTag(new NBTTagFloat(equipmentDropChances[j]));
        }

        tagCompound.setTag("DropChances", nbttaglist1);
        tagCompound.setBoolean("Leashed", isLeashed);

        if (leashedToEntity != null)
        {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            if (leashedToEntity instanceof EntityLivingBase)
            {
                nbttagcompound1.setLong("UUIDMost", leashedToEntity.getUniqueID().getMostSignificantBits());
                nbttagcompound1.setLong("UUIDLeast", leashedToEntity.getUniqueID().getLeastSignificantBits());
            }
            else if (leashedToEntity instanceof EntityHanging)
            {
                BlockPos blockpos = ((EntityHanging) leashedToEntity).getHangingPosition();
                nbttagcompound1.setInteger("X", blockpos.getX());
                nbttagcompound1.setInteger("Y", blockpos.getY());
                nbttagcompound1.setInteger("Z", blockpos.getZ());
            }

            tagCompound.setTag("Leash", nbttagcompound1);
        }

        if (isAIDisabled())
        {
            tagCompound.setBoolean("NoAI", isAIDisabled());
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("CanPickUpLoot", 1))
        {
            setCanPickUpLoot(tagCompund.getBoolean("CanPickUpLoot"));
        }

        persistenceRequired = tagCompund.getBoolean("PersistenceRequired");

        if (tagCompund.hasKey("Equipment", 9))
        {
            NBTTagList nbttaglist = tagCompund.getTagList("Equipment", 10);

            for (int i = 0; i < equipment.length; ++i)
            {
                equipment[i] = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
            }
        }

        if (tagCompund.hasKey("DropChances", 9))
        {
            NBTTagList nbttaglist1 = tagCompund.getTagList("DropChances", 5);

            for (int j = 0; j < nbttaglist1.tagCount(); ++j)
            {
                equipmentDropChances[j] = nbttaglist1.getFloatAt(j);
            }
        }

        isLeashed = tagCompund.getBoolean("Leashed");

        if (isLeashed && tagCompund.hasKey("Leash", 10))
        {
            leashNBTTag = tagCompund.getCompoundTag("Leash");
        }

        setNoAI(tagCompund.getBoolean("NoAI"));
    }

    public void setMoveForward(float p_70657_1_)
    {
        moveForward = p_70657_1_;
    }

    /**
     * set the movespeed used for the new AI system
     */
    public void setAIMoveSpeed(float speedIn)
    {
        super.setAIMoveSpeed(speedIn);
        setMoveForward(speedIn);
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        worldObj.theProfiler.startSection("looting");

        if (!worldObj.isRemote && canPickUpLoot() && !dead && worldObj.getGameRules().getBoolean("mobGriefing"))
        {
            for (EntityItem entityitem : worldObj.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().expand(1.0D, 0.0D, 1.0D)))
            {
                if (!entityitem.isDead && entityitem.getEntityItem() != null && !entityitem.cannotPickup())
                {
                    updateEquipmentIfNeeded(entityitem);
                }
            }
        }

        worldObj.theProfiler.endSection();
    }

    /**
     * Tests if this entity should pickup a weapon or an armor. Entity drops current weapon or armor if the new one is
     * better.
     */
    protected void updateEquipmentIfNeeded(EntityItem itemEntity)
    {
        ItemStack itemstack = itemEntity.getEntityItem();
        int i = EntityLiving.getArmorPosition(itemstack);

        if (i > -1)
        {
            boolean flag = true;
            ItemStack itemstack1 = getEquipmentInSlot(i);

            if (itemstack1 != null)
            {
                if (i == 0)
                {
                    if (itemstack.getItem() instanceof ItemSword && !(itemstack1.getItem() instanceof ItemSword))
                    {
                        flag = true;
                    }
                    else if (itemstack.getItem() instanceof ItemSword && itemstack1.getItem() instanceof ItemSword)
                    {
                        ItemSword itemsword = (ItemSword)itemstack.getItem();
                        ItemSword itemsword1 = (ItemSword)itemstack1.getItem();

                        if (itemsword.getDamageVsEntity() != itemsword1.getDamageVsEntity())
                        {
                            flag = itemsword.getDamageVsEntity() > itemsword1.getDamageVsEntity();
                        }
                        else
                        {
                            flag = itemstack.getMetadata() > itemstack1.getMetadata() || itemstack.hasTagCompound() && !itemstack1.hasTagCompound();
                        }
                    }
                    else if (itemstack.getItem() instanceof ItemBow && itemstack1.getItem() instanceof ItemBow)
                    {
                        flag = itemstack.hasTagCompound() && !itemstack1.hasTagCompound();
                    }
                    else
                    {
                        flag = false;
                    }
                }
                else if (itemstack.getItem() instanceof ItemArmor && !(itemstack1.getItem() instanceof ItemArmor))
                {
                    flag = true;
                }
                else if (itemstack.getItem() instanceof ItemArmor && itemstack1.getItem() instanceof ItemArmor)
                {
                    ItemArmor itemarmor = (ItemArmor)itemstack.getItem();
                    ItemArmor itemarmor1 = (ItemArmor)itemstack1.getItem();

                    if (itemarmor.damageReduceAmount != itemarmor1.damageReduceAmount)
                    {
                        flag = itemarmor.damageReduceAmount > itemarmor1.damageReduceAmount;
                    }
                    else
                    {
                        flag = itemstack.getMetadata() > itemstack1.getMetadata() || itemstack.hasTagCompound() && !itemstack1.hasTagCompound();
                    }
                }
                else
                {
                    flag = false;
                }
            }

            if (flag && func_175448_a(itemstack))
            {
                if (itemstack1 != null && rand.nextFloat() - 0.1F < equipmentDropChances[i])
                {
                    entityDropItem(itemstack1, 0.0F);
                }

                if (itemstack.getItem() == Items.diamond && itemEntity.getThrower() != null)
                {
                    EntityPlayer entityplayer = worldObj.getPlayerEntityByName(itemEntity.getThrower());

                    if (entityplayer != null)
                    {
                        entityplayer.triggerAchievement(AchievementList.diamondsToYou);
                    }
                }

                setCurrentItemOrArmor(i, itemstack);
                equipmentDropChances[i] = 2.0F;
                persistenceRequired = true;
                onItemPickup(itemEntity, 1);
                itemEntity.setDead();
            }
        }
    }

    protected boolean func_175448_a(ItemStack stack)
    {
        return true;
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    protected boolean canDespawn()
    {
        return true;
    }

    /**
     * Makes the entity despawn if requirements are reached
     */
    protected void despawnEntity()
    {
        Object object = null;
        Object object1 = Reflector.getFieldValue(Reflector.Event_Result_DEFAULT);
        Object object2 = Reflector.getFieldValue(Reflector.Event_Result_DENY);

        if (persistenceRequired)
        {
            entityAge = 0;
        }
        else if ((entityAge & 31) == 31 && (object = Reflector.call(Reflector.ForgeEventFactory_canEntityDespawn, this)) != object1)
        {
            if (object == object2)
            {
                entityAge = 0;
            }
            else
            {
                setDead();
            }
        }
        else
        {
            EntityPlayer entityplayer = worldObj.getClosestPlayerToEntity(this, -1.0D);

            if (entityplayer != null)
            {
                double d0 = entityplayer.posX - posX;
                double d1 = entityplayer.posY - posY;
                double d2 = entityplayer.posZ - posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (canDespawn() && d3 > 16384.0D)
                {
                    setDead();
                }

                if (entityAge > 600 && rand.nextInt(800) == 0 && d3 > 1024.0D && canDespawn())
                {
                    setDead();
                }
                else if (d3 < 1024.0D)
                {
                    entityAge = 0;
                }
            }
        }
    }

    protected final void updateEntityActionState()
    {
        ++entityAge;
        worldObj.theProfiler.startSection("checkDespawn");
        despawnEntity();
        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("sensing");
        senses.clearSensingCache();
        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("targetSelector");
        targetTasks.onUpdateTasks();
        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("goalSelector");
        tasks.onUpdateTasks();
        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("navigation");
        navigator.onUpdateNavigation();
        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("mob tick");
        updateAITasks();
        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("controls");
        worldObj.theProfiler.startSection("move");
        moveHelper.onUpdateMoveHelper();
        worldObj.theProfiler.endStartSection("look");
        lookHelper.onUpdateLook();
        worldObj.theProfiler.endStartSection("jump");
        jumpHelper.doJump();
        worldObj.theProfiler.endSection();
        worldObj.theProfiler.endSection();
    }

    protected void updateAITasks()
    {
    }

    /**
     * The speed it takes to move the entityliving's rotationPitch through the faceEntity method. This is only currently
     * use in wolves.
     */
    public int getVerticalFaceSpeed()
    {
        return 40;
    }

    /**
     * Changes pitch and yaw so that the entity calling the function is facing the entity provided as an argument.
     */
    public void faceEntity(Entity entityIn, float p_70625_2_, float p_70625_3_)
    {
        double d0 = entityIn.posX - posX;
        double d1 = entityIn.posZ - posZ;
        double d2;

        if (entityIn instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase)entityIn;
            d2 = entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - (posY + (double) getEyeHeight());
        }
        else
        {
            d2 = (entityIn.getEntityBoundingBox().minY + entityIn.getEntityBoundingBox().maxY) / 2.0D - (posY + (double) getEyeHeight());
        }

        double d3 = MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        float f = (float)(MathHelper.func_181159_b(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float f1 = (float)(-(MathHelper.func_181159_b(d2, d3) * 180.0D / Math.PI));
        rotationPitch = updateRotation(rotationPitch, f1, p_70625_3_);
        rotationYaw = updateRotation(rotationYaw, f, p_70625_2_);
    }

    /**
     * Arguments: current rotation, intended rotation, max increment.
     */
    private float updateRotation(float p_70663_1_, float p_70663_2_, float p_70663_3_)
    {
        float f = MathHelper.wrapAngleTo180_float(p_70663_2_ - p_70663_1_);

        if (f > p_70663_3_)
        {
            f = p_70663_3_;
        }

        if (f < -p_70663_3_)
        {
            f = -p_70663_3_;
        }

        return p_70663_1_ + f;
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
        return true;
    }

    /**
     * Checks that the entity is not colliding with any blocks / liquids
     */
    public boolean isNotColliding()
    {
        return worldObj.checkNoEntityCollision(getEntityBoundingBox(), this) && worldObj.getCollidingBoundingBoxes(this, getEntityBoundingBox()).isEmpty() && !worldObj.isAnyLiquid(getEntityBoundingBox());
    }

    /**
     * Returns render size modifier
     */
    public float getRenderSizeModifier()
    {
        return 1.0F;
    }

    /**
     * Will return how many at most can spawn in a chunk at once.
     */
    public int getMaxSpawnedInChunk()
    {
        return 4;
    }

    /**
     * The maximum height from where the entity is alowed to jump (used in pathfinder)
     */
    public int getMaxFallHeight()
    {
        if (getAttackTarget() == null)
        {
            return 3;
        }
        else
        {
            int i = (int)(getHealth() - getMaxHealth() * 0.33F);
            i = i - (3 - worldObj.getDifficulty().getDifficultyId()) * 4;

            if (i < 0)
            {
                i = 0;
            }

            return i + 3;
        }
    }

    /**
     * Returns the item that this EntityLiving is holding, if any.
     */
    public ItemStack getHeldItem()
    {
        return equipment[0];
    }

    /**
     * 0: Tool in Hand; 1-4: Armor
     */
    public ItemStack getEquipmentInSlot(int slotIn)
    {
        return equipment[slotIn];
    }

    public ItemStack getCurrentArmor(int slotIn)
    {
        return equipment[slotIn + 1];
    }

    /**
     * Sets the held item, or an armor slot. Slot 0 is held item. Slot 1-4 is armor. Params: Item, slot
     */
    public void setCurrentItemOrArmor(int slotIn, ItemStack stack)
    {
        equipment[slotIn] = stack;
    }

    /**
     * returns the inventory of this entity (only used in EntityPlayerMP it seems)
     */
    public ItemStack[] getInventory()
    {
        return equipment;
    }

    /**
     * Drop the equipment for this entity.
     */
    protected void dropEquipment(boolean p_82160_1_, int p_82160_2_)
    {
        for (int i = 0; i < getInventory().length; ++i)
        {
            ItemStack itemstack = getEquipmentInSlot(i);
            boolean flag = equipmentDropChances[i] > 1.0F;

            if (itemstack != null && (p_82160_1_ || flag) && rand.nextFloat() - (float)p_82160_2_ * 0.01F < equipmentDropChances[i])
            {
                if (!flag && itemstack.isItemStackDamageable())
                {
                    int j = Math.max(itemstack.getMaxDamage() - 25, 1);
                    int k = itemstack.getMaxDamage() - rand.nextInt(rand.nextInt(j) + 1);

                    if (k > j)
                    {
                        k = j;
                    }

                    if (k < 1)
                    {
                        k = 1;
                    }

                    itemstack.setItemDamage(k);
                }

                entityDropItem(itemstack, 0.0F);
            }
        }
    }

    /**
     * Gives armor or weapon for entity based on given DifficultyInstance
     */
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty)
    {
        if (rand.nextFloat() < 0.15F * difficulty.getClampedAdditionalDifficulty())
        {
            int i = rand.nextInt(2);
            float f = worldObj.getDifficulty() == EnumDifficulty.HARD ? 0.1F : 0.25F;

            if (rand.nextFloat() < 0.095F)
            {
                ++i;
            }

            if (rand.nextFloat() < 0.095F)
            {
                ++i;
            }

            if (rand.nextFloat() < 0.095F)
            {
                ++i;
            }

            for (int j = 3; j >= 0; --j)
            {
                ItemStack itemstack = getCurrentArmor(j);

                if (j < 3 && rand.nextFloat() < f)
                {
                    break;
                }

                if (itemstack == null)
                {
                    Item item = EntityLiving.getArmorItemForSlot(j + 1, i);

                    if (item != null)
                    {
                        setCurrentItemOrArmor(j + 1, new ItemStack(item));
                    }
                }
            }
        }
    }

    public static int getArmorPosition(ItemStack stack)
    {
        if (stack.getItem() != Item.getItemFromBlock(Blocks.pumpkin) && stack.getItem() != Items.skull)
        {
            if (stack.getItem() instanceof ItemArmor)
            {
                switch (((ItemArmor)stack.getItem()).armorType)
                {
                    case 0:
                        return 4;

                    case 1:
                        return 3;

                    case 2:
                        return 2;

                    case 3:
                        return 1;
                }
            }

            return 0;
        }
        else
        {
            return 4;
        }
    }

    /**
     * Gets the vanilla armor Item that can go in the slot specified for the given tier.
     */
    public static Item getArmorItemForSlot(int armorSlot, int itemTier)
    {
        switch (armorSlot)
        {
            case 4:
                if (itemTier == 0)
                {
                    return Items.leather_helmet;
                }
                else if (itemTier == 1)
                {
                    return Items.golden_helmet;
                }
                else if (itemTier == 2)
                {
                    return Items.chainmail_helmet;
                }
                else if (itemTier == 3)
                {
                    return Items.iron_helmet;
                }
                else if (itemTier == 4)
                {
                    return Items.diamond_helmet;
                }

            case 3:
                if (itemTier == 0)
                {
                    return Items.leather_chestplate;
                }
                else if (itemTier == 1)
                {
                    return Items.golden_chestplate;
                }
                else if (itemTier == 2)
                {
                    return Items.chainmail_chestplate;
                }
                else if (itemTier == 3)
                {
                    return Items.iron_chestplate;
                }
                else if (itemTier == 4)
                {
                    return Items.diamond_chestplate;
                }

            case 2:
                if (itemTier == 0)
                {
                    return Items.leather_leggings;
                }
                else if (itemTier == 1)
                {
                    return Items.golden_leggings;
                }
                else if (itemTier == 2)
                {
                    return Items.chainmail_leggings;
                }
                else if (itemTier == 3)
                {
                    return Items.iron_leggings;
                }
                else if (itemTier == 4)
                {
                    return Items.diamond_leggings;
                }

            case 1:
                if (itemTier == 0)
                {
                    return Items.leather_boots;
                }
                else if (itemTier == 1)
                {
                    return Items.golden_boots;
                }
                else if (itemTier == 2)
                {
                    return Items.chainmail_boots;
                }
                else if (itemTier == 3)
                {
                    return Items.iron_boots;
                }
                else if (itemTier == 4)
                {
                    return Items.diamond_boots;
                }

            default:
                return null;
        }
    }

    /**
     * Enchants Entity's current equipments based on given DifficultyInstance
     */
    protected void setEnchantmentBasedOnDifficulty(DifficultyInstance difficulty)
    {
        float f = difficulty.getClampedAdditionalDifficulty();

        if (getHeldItem() != null && rand.nextFloat() < 0.25F * f)
        {
            EnchantmentHelper.addRandomEnchantment(rand, getHeldItem(), (int)(5.0F + f * (float) rand.nextInt(18)));
        }

        for (int i = 0; i < 4; ++i)
        {
            ItemStack itemstack = getCurrentArmor(i);

            if (itemstack != null && rand.nextFloat() < 0.5F * f)
            {
                EnchantmentHelper.addRandomEnchantment(rand, itemstack, (int)(5.0F + f * (float) rand.nextInt(18)));
            }
        }
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata)
    {
        getEntityAttribute(SharedMonsterAttributes.followRange).applyModifier(new AttributeModifier("Random spawn bonus", rand.nextGaussian() * 0.05D, 1));
        return livingdata;
    }

    /**
     * returns true if all the conditions for steering the entity are met. For pigs, this is true if it is being ridden
     * by a player and the player is holding a carrot-on-a-stick
     */
    public boolean canBeSteered()
    {
        return false;
    }

    /**
     * Enable the Entity persistence
     */
    public void enablePersistence()
    {
        persistenceRequired = true;
    }

    public void setEquipmentDropChance(int slotIn, float chance)
    {
        equipmentDropChances[slotIn] = chance;
    }

    public boolean canPickUpLoot()
    {
        return canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean canPickup)
    {
        canPickUpLoot = canPickup;
    }

    public boolean isNoDespawnRequired()
    {
        return persistenceRequired;
    }

    /**
     * First layer of player interaction
     */
    public final boolean interactFirst(EntityPlayer playerIn)
    {
        if (getLeashed() && getLeashedToEntity() == playerIn)
        {
            clearLeashed(true, !playerIn.capabilities.isCreativeMode);
            return true;
        }
        else
        {
            ItemStack itemstack = playerIn.inventory.getCurrentItem();

            if (itemstack != null && itemstack.getItem() == Items.lead && allowLeashing())
            {
                if (!(this instanceof EntityTameable) || !((EntityTameable)this).isTamed())
                {
                    setLeashedToEntity(playerIn, true);
                    --itemstack.stackSize;
                    return true;
                }

                if (((EntityTameable)this).isOwner(playerIn))
                {
                    setLeashedToEntity(playerIn, true);
                    --itemstack.stackSize;
                    return true;
                }
            }

            return interact(playerIn) || super.interactFirst(playerIn);
        }
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    protected boolean interact(EntityPlayer player)
    {
        return false;
    }

    /**
     * Applies logic related to leashes, for example dragging the entity or breaking the leash.
     */
    protected void updateLeashedState()
    {
        if (leashNBTTag != null)
        {
            recreateLeash();
        }

        if (isLeashed)
        {
            if (!isEntityAlive())
            {
                clearLeashed(true, true);
            }

            if (leashedToEntity == null || leashedToEntity.isDead)
            {
                clearLeashed(true, true);
            }
        }
    }

    /**
     * Removes the leash from this entity
     */
    public void clearLeashed(boolean sendPacket, boolean dropLead)
    {
        if (isLeashed)
        {
            isLeashed = false;
            leashedToEntity = null;

            if (!worldObj.isRemote && dropLead)
            {
                dropItem(Items.lead, 1);
            }

            if (!worldObj.isRemote && sendPacket && worldObj instanceof WorldServer)
            {
                ((WorldServer) worldObj).getEntityTracker().sendToAllTrackingEntity(this, new S1BPacketEntityAttach(1, this, null));
            }
        }
    }

    public boolean allowLeashing()
    {
        return !getLeashed() && !(this instanceof IMob);
    }

    public boolean getLeashed()
    {
        return isLeashed;
    }

    public Entity getLeashedToEntity()
    {
        return leashedToEntity;
    }

    /**
     * Sets the entity to be leashed to.
     */
    public void setLeashedToEntity(Entity entityIn, boolean sendAttachNotification)
    {
        isLeashed = true;
        leashedToEntity = entityIn;

        if (!worldObj.isRemote && sendAttachNotification && worldObj instanceof WorldServer)
        {
            ((WorldServer) worldObj).getEntityTracker().sendToAllTrackingEntity(this, new S1BPacketEntityAttach(1, this, leashedToEntity));
        }
    }

    private void recreateLeash()
    {
        if (isLeashed && leashNBTTag != null)
        {
            if (leashNBTTag.hasKey("UUIDMost", 4) && leashNBTTag.hasKey("UUIDLeast", 4))
            {
                UUID uuid = new UUID(leashNBTTag.getLong("UUIDMost"), leashNBTTag.getLong("UUIDLeast"));

                for (EntityLivingBase entitylivingbase : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().expand(10.0D, 10.0D, 10.0D)))
                {
                    if (entitylivingbase.getUniqueID().equals(uuid))
                    {
                        leashedToEntity = entitylivingbase;
                        break;
                    }
                }
            }
            else if (leashNBTTag.hasKey("X", 99) && leashNBTTag.hasKey("Y", 99) && leashNBTTag.hasKey("Z", 99))
            {
                BlockPos blockpos = new BlockPos(leashNBTTag.getInteger("X"), leashNBTTag.getInteger("Y"), leashNBTTag.getInteger("Z"));
                EntityLeashKnot entityleashknot = EntityLeashKnot.getKnotForPosition(worldObj, blockpos);

                if (entityleashknot == null)
                {
                    entityleashknot = EntityLeashKnot.createKnot(worldObj, blockpos);
                }

                leashedToEntity = entityleashknot;
            }
            else
            {
                clearLeashed(false, true);
            }
        }

        leashNBTTag = null;
    }

    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn)
    {
        int i;

        if (inventorySlot == 99)
        {
            i = 0;
        }
        else
        {
            i = inventorySlot - 100 + 1;

            if (i < 0 || i >= equipment.length)
            {
                return false;
            }
        }

        if (itemStackIn == null || EntityLiving.getArmorPosition(itemStackIn) == i || i == 4 && itemStackIn.getItem() instanceof ItemBlock)
        {
            setCurrentItemOrArmor(i, itemStackIn);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns whether the entity is in a server world
     */
    public boolean isServerWorld()
    {
        return super.isServerWorld() && !isAIDisabled();
    }

    /**
     * Set whether this Entity's AI is disabled
     */
    public void setNoAI(boolean disable)
    {
        dataWatcher.updateObject(15, Byte.valueOf((byte)(disable ? 1 : 0)));
    }

    /**
     * Get whether this Entity's AI is disabled
     */
    public boolean isAIDisabled()
    {
        return dataWatcher.getWatchableObjectByte(15) != 0;
    }

    /**
     * Checks if this entity is inside of an opaque block
     */
    public boolean isEntityInsideOpaqueBlock()
    {
        if (noClip)
        {
            return false;
        }
        else
        {
            BlockPosM blockposm = new BlockPosM(0, 0, 0);

            for (int i = 0; i < 8; ++i)
            {
                double d0 = posX + (double)(((float)((i >> 0) % 2) - 0.5F) * width * 0.8F);
                double d1 = posY + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
                double d2 = posZ + (double)(((float)((i >> 2) % 2) - 0.5F) * width * 0.8F);
                blockposm.setXyz(d0, d1 + (double) getEyeHeight(), d2);

                if (worldObj.getBlockState(blockposm).getBlock().isVisuallyOpaque())
                {
                    return true;
                }
            }

            return false;
        }
    }

    private boolean canSkipUpdate()
    {
        if (isChild())
        {
            return false;
        }
        else if (hurtTime > 0)
        {
            return false;
        }
        else if (ticksExisted < 20)
        {
            return false;
        }
        else
        {
            World world = getEntityWorld();

            if (world == null)
            {
                return false;
            }
            else if (world.playerEntities.size() != 1)
            {
                return false;
            }
            else
            {
                Entity entity = world.playerEntities.get(0);
                double d0 = Math.max(Math.abs(posX - entity.posX) - 16.0D, 0.0D);
                double d1 = Math.max(Math.abs(posZ - entity.posZ) - 16.0D, 0.0D);
                double d2 = d0 * d0 + d1 * d1;
                return !isInRangeToRenderDist(d2);
            }
        }
    }

    private void onUpdateMinimal()
    {
        ++entityAge;

        if (this instanceof EntityMob)
        {
            float f = getBrightness(1.0F);

            if (f > 0.5F)
            {
                entityAge += 2;
            }
        }

        despawnEntity();
    }

    public static enum SpawnPlacementType
    {
        ON_GROUND("ON_GROUND", 0),
        IN_AIR("IN_AIR", 1),
        IN_WATER("IN_WATER", 2);

        private static final EntityLiving.SpawnPlacementType[] $VALUES = new EntityLiving.SpawnPlacementType[]{SpawnPlacementType.ON_GROUND, SpawnPlacementType.IN_AIR, SpawnPlacementType.IN_WATER};
        private static final String __OBFID = "CL_00002255";

        private SpawnPlacementType(String p_i18_3_, int p_i18_4_)
        {
        }
    }
}
