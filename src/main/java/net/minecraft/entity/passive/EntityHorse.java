package net.minecraft.entity.passive;

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIRunAroundLikeCrazy;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.Potion;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityHorse extends EntityAnimal implements IInvBasic
{
    private static final Predicate<Entity> horseBreedingSelector = new Predicate<Entity>()
    {
        public boolean apply(Entity p_apply_1_)
        {
            return p_apply_1_ instanceof EntityHorse && ((EntityHorse)p_apply_1_).isBreeding();
        }
    };
    private static final IAttribute horseJumpStrength = (new RangedAttribute(null, "horse.jumpStrength", 0.7D, 0.0D, 2.0D)).setDescription("Jump Strength").setShouldWatch(true);
    private static final String[] horseArmorTextures = new String[] {null, "textures/entity/horse/armor/horse_armor_iron.png", "textures/entity/horse/armor/horse_armor_gold.png", "textures/entity/horse/armor/horse_armor_diamond.png"};
    private static final String[] HORSE_ARMOR_TEXTURES_ABBR = new String[] {"", "meo", "goo", "dio"};
    private static final int[] armorValues = new int[] {0, 5, 7, 11};
    private static final String[] horseTextures = new String[] {"textures/entity/horse/horse_white.png", "textures/entity/horse/horse_creamy.png", "textures/entity/horse/horse_chestnut.png", "textures/entity/horse/horse_brown.png", "textures/entity/horse/horse_black.png", "textures/entity/horse/horse_gray.png", "textures/entity/horse/horse_darkbrown.png"};
    private static final String[] HORSE_TEXTURES_ABBR = new String[] {"hwh", "hcr", "hch", "hbr", "hbl", "hgr", "hdb"};
    private static final String[] horseMarkingTextures = new String[] {null, "textures/entity/horse/horse_markings_white.png", "textures/entity/horse/horse_markings_whitefield.png", "textures/entity/horse/horse_markings_whitedots.png", "textures/entity/horse/horse_markings_blackdots.png"};
    private static final String[] HORSE_MARKING_TEXTURES_ABBR = new String[] {"", "wo_", "wmo", "wdo", "bdo"};
    private int eatingHaystackCounter;
    private int openMouthCounter;
    private int jumpRearingCounter;
    public int field_110278_bp;
    public int field_110279_bq;
    protected boolean horseJumping;
    private AnimalChest horseChest;
    private boolean hasReproduced;

    /**
     * "The higher this value, the more likely the horse is to be tamed next time a player rides it."
     */
    protected int temper;
    protected float jumpPower;
    private boolean field_110294_bI;
    private float headLean;
    private float prevHeadLean;
    private float rearingAmount;
    private float prevRearingAmount;
    private float mouthOpenness;
    private float prevMouthOpenness;

    /** Used to determine the sound that the horse should make when it steps */
    private int gallopTime;
    private String texturePrefix;
    private final String[] horseTexturesArray = new String[3];
    private boolean field_175508_bO = false;

    public EntityHorse(World worldIn)
    {
        super(worldIn);
        setSize(1.4F, 1.6F);
        isImmuneToFire = false;
        setChested(false);
        ((PathNavigateGround) getNavigator()).setAvoidsWater(true);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.2D));
        tasks.addTask(1, new EntityAIRunAroundLikeCrazy(this, 1.2D));
        tasks.addTask(2, new EntityAIMate(this, 1.0D));
        tasks.addTask(4, new EntityAIFollowParent(this, 1.0D));
        tasks.addTask(6, new EntityAIWander(this, 0.7D));
        tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        initHorseChest();
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(16, Integer.valueOf(0));
        dataWatcher.addObject(19, Byte.valueOf((byte)0));
        dataWatcher.addObject(20, Integer.valueOf(0));
        dataWatcher.addObject(21, String.valueOf(""));
        dataWatcher.addObject(22, Integer.valueOf(0));
    }

    public void setHorseType(int type)
    {
        dataWatcher.updateObject(19, Byte.valueOf((byte)type));
        resetTexturePrefix();
    }

    /**
     * Returns the horse type. 0 = Normal, 1 = Donkey, 2 = Mule, 3 = Undead Horse, 4 = Skeleton Horse
     */
    public int getHorseType()
    {
        return dataWatcher.getWatchableObjectByte(19);
    }

    public void setHorseVariant(int variant)
    {
        dataWatcher.updateObject(20, Integer.valueOf(variant));
        resetTexturePrefix();
    }

    public int getHorseVariant()
    {
        return dataWatcher.getWatchableObjectInt(20);
    }

    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
    public String getName()
    {
        if (hasCustomName())
        {
            return getCustomNameTag();
        }
        else
        {
            int i = getHorseType();

            switch (i)
            {
                case 0:
                default:
                    return StatCollector.translateToLocal("entity.horse.name");

                case 1:
                    return StatCollector.translateToLocal("entity.donkey.name");

                case 2:
                    return StatCollector.translateToLocal("entity.mule.name");

                case 3:
                    return StatCollector.translateToLocal("entity.zombiehorse.name");

                case 4:
                    return StatCollector.translateToLocal("entity.skeletonhorse.name");
            }
        }
    }

    private boolean getHorseWatchableBoolean(int p_110233_1_)
    {
        return (dataWatcher.getWatchableObjectInt(16) & p_110233_1_) != 0;
    }

    private void setHorseWatchableBoolean(int p_110208_1_, boolean p_110208_2_)
    {
        int i = dataWatcher.getWatchableObjectInt(16);

        if (p_110208_2_)
        {
            dataWatcher.updateObject(16, Integer.valueOf(i | p_110208_1_));
        }
        else
        {
            dataWatcher.updateObject(16, Integer.valueOf(i & ~p_110208_1_));
        }
    }

    public boolean isAdultHorse()
    {
        return !isChild();
    }

    public boolean isTame()
    {
        return getHorseWatchableBoolean(2);
    }

    public boolean func_110253_bW()
    {
        return isAdultHorse();
    }

    /**
     * Gets the horse's owner
     */
    public String getOwnerId()
    {
        return dataWatcher.getWatchableObjectString(21);
    }

    public void setOwnerId(String id)
    {
        dataWatcher.updateObject(21, id);
    }

    public float getHorseSize()
    {
        return 0.5F;
    }

    /**
     * "Sets the scale for an ageable entity according to the boolean parameter, which says if it's a child."
     */
    public void setScaleForAge(boolean p_98054_1_)
    {
        if (p_98054_1_)
        {
            setScale(getHorseSize());
        }
        else
        {
            setScale(1.0F);
        }
    }

    public boolean isHorseJumping()
    {
        return horseJumping;
    }

    public void setHorseTamed(boolean tamed)
    {
        setHorseWatchableBoolean(2, tamed);
    }

    public void setHorseJumping(boolean jumping)
    {
        horseJumping = jumping;
    }

    public boolean allowLeashing()
    {
        return !isUndead() && super.allowLeashing();
    }

    protected void func_142017_o(float p_142017_1_)
    {
        if (p_142017_1_ > 6.0F && isEatingHaystack())
        {
            setEatingHaystack(false);
        }
    }

    public boolean isChested()
    {
        return getHorseWatchableBoolean(8);
    }

    /**
     * Returns type of armor from DataWatcher (0 = iron, 1 = gold, 2 = diamond)
     */
    public int getHorseArmorIndexSynced()
    {
        return dataWatcher.getWatchableObjectInt(22);
    }

    /**
     * 0 = iron, 1 = gold, 2 = diamond
     */
    private int getHorseArmorIndex(ItemStack itemStackIn)
    {
        if (itemStackIn == null)
        {
            return 0;
        }
        else
        {
            Item item = itemStackIn.getItem();
            return item == Items.iron_horse_armor ? 1 : (item == Items.golden_horse_armor ? 2 : (item == Items.diamond_horse_armor ? 3 : 0));
        }
    }

    public boolean isEatingHaystack()
    {
        return getHorseWatchableBoolean(32);
    }

    public boolean isRearing()
    {
        return getHorseWatchableBoolean(64);
    }

    public boolean isBreeding()
    {
        return getHorseWatchableBoolean(16);
    }

    public boolean getHasReproduced()
    {
        return hasReproduced;
    }

    /**
     * Set horse armor stack (for example: new ItemStack(Items.iron_horse_armor))
     */
    public void setHorseArmorStack(ItemStack itemStackIn)
    {
        dataWatcher.updateObject(22, Integer.valueOf(getHorseArmorIndex(itemStackIn)));
        resetTexturePrefix();
    }

    public void setBreeding(boolean breeding)
    {
        setHorseWatchableBoolean(16, breeding);
    }

    public void setChested(boolean chested)
    {
        setHorseWatchableBoolean(8, chested);
    }

    public void setHasReproduced(boolean hasReproducedIn)
    {
        hasReproduced = hasReproducedIn;
    }

    public void setHorseSaddled(boolean saddled)
    {
        setHorseWatchableBoolean(4, saddled);
    }

    public int getTemper()
    {
        return temper;
    }

    public void setTemper(int temperIn)
    {
        temper = temperIn;
    }

    public int increaseTemper(int p_110198_1_)
    {
        int i = MathHelper.clamp_int(getTemper() + p_110198_1_, 0, getMaxTemper());
        setTemper(i);
        return i;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        Entity entity = source.getEntity();
        return (this.riddenByEntity == null || !riddenByEntity.equals(entity)) && super.attackEntityFrom(source, amount);
    }

    /**
     * Returns the current armor value as determined by a call to InventoryPlayer.getTotalArmorValue
     */
    public int getTotalArmorValue()
    {
        return EntityHorse.armorValues[getHorseArmorIndexSynced()];
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    public boolean canBePushed()
    {
        return riddenByEntity == null;
    }

    public boolean prepareChunkForSpawn()
    {
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(posZ);
        worldObj.getBiomeGenForCoords(new BlockPos(i, 0, j));
        return true;
    }

    public void dropChests()
    {
        if (!worldObj.isRemote && isChested())
        {
            dropItem(Item.getItemFromBlock(Blocks.chest), 1);
            setChested(false);
        }
    }

    private void func_110266_cB()
    {
        openHorseMouth();

        if (!isSilent())
        {
            worldObj.playSoundAtEntity(this, "eating", 1.0F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);
        }
    }

    public void fall(float distance, float damageMultiplier)
    {
        if (distance > 1.0F)
        {
            playSound("mob.horse.land", 0.4F, 1.0F);
        }

        int i = MathHelper.ceiling_float_int((distance * 0.5F - 3.0F) * damageMultiplier);

        if (i > 0)
        {
            attackEntityFrom(DamageSource.fall, (float)i);

            if (riddenByEntity != null)
            {
                riddenByEntity.attackEntityFrom(DamageSource.fall, (float)i);
            }

            Block block = worldObj.getBlockState(new BlockPos(posX, posY - 0.2D - (double) prevRotationYaw, posZ)).getBlock();

            if (block.getMaterial() != Material.air && !isSilent())
            {
                Block.SoundType block$soundtype = block.stepSound;
                worldObj.playSoundAtEntity(this, block$soundtype.getStepSound(), block$soundtype.getVolume() * 0.5F, block$soundtype.getFrequency() * 0.75F);
            }
        }
    }

    /**
     * Returns number of slots depending horse type
     */
    private int getChestSize()
    {
        int i = getHorseType();
        return !isChested() || i != 1 && i != 2 ? 2 : 17;
    }

    private void initHorseChest()
    {
        AnimalChest animalchest = horseChest;
        horseChest = new AnimalChest("HorseChest", getChestSize());
        horseChest.setCustomName(getName());

        if (animalchest != null)
        {
            animalchest.func_110132_b(this);
            int i = Math.min(animalchest.getSizeInventory(), horseChest.getSizeInventory());

            for (int j = 0; j < i; ++j)
            {
                ItemStack itemstack = animalchest.getStackInSlot(j);

                if (itemstack != null)
                {
                    horseChest.setInventorySlotContents(j, itemstack.copy());
                }
            }
        }

        horseChest.func_110134_a(this);
        updateHorseSlots();
    }

    /**
     * Updates the items in the saddle and armor slots of the horse's inventory.
     */
    private void updateHorseSlots()
    {
        if (!worldObj.isRemote)
        {
            setHorseSaddled(horseChest.getStackInSlot(0) != null);

            if (canWearArmor())
            {
                setHorseArmorStack(horseChest.getStackInSlot(1));
            }
        }
    }

    /**
     * Called by InventoryBasic.onInventoryChanged() on a array that is never filled.
     */
    public void onInventoryChanged(InventoryBasic p_76316_1_)
    {
        int i = getHorseArmorIndexSynced();
        boolean flag = isHorseSaddled();
        updateHorseSlots();

        if (ticksExisted > 20)
        {
            if (i == 0 && i != getHorseArmorIndexSynced())
            {
                playSound("mob.horse.armor", 0.5F, 1.0F);
            }
            else if (i != getHorseArmorIndexSynced())
            {
                playSound("mob.horse.armor", 0.5F, 1.0F);
            }

            if (!flag && isHorseSaddled())
            {
                playSound("mob.horse.leather", 0.5F, 1.0F);
            }
        }
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
        prepareChunkForSpawn();
        return super.getCanSpawnHere();
    }

    protected EntityHorse getClosestHorse(Entity entityIn, double distance)
    {
        double d0 = Double.MAX_VALUE;
        Entity entity = null;

        for (Entity entity1 : worldObj.getEntitiesInAABBexcluding(entityIn, entityIn.getEntityBoundingBox().addCoord(distance, distance, distance), EntityHorse.horseBreedingSelector))
        {
            double d1 = entity1.getDistanceSq(entityIn.posX, entityIn.posY, entityIn.posZ);

            if (d1 < d0)
            {
                entity = entity1;
                d0 = d1;
            }
        }

        return (EntityHorse)entity;
    }

    public double getHorseJumpStrength()
    {
        return getEntityAttribute(EntityHorse.horseJumpStrength).getAttributeValue();
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        openHorseMouth();
        int i = getHorseType();
        return i == 3 ? "mob.horse.zombie.death" : (i == 4 ? "mob.horse.skeleton.death" : (i != 1 && i != 2 ? "mob.horse.death" : "mob.horse.donkey.death"));
    }

    protected Item getDropItem()
    {
        boolean flag = rand.nextInt(4) == 0;
        int i = getHorseType();
        return i == 4 ? Items.bone : (i == 3 ? (flag ? null : Items.rotten_flesh) : Items.leather);
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        openHorseMouth();

        if (rand.nextInt(3) == 0)
        {
            makeHorseRear();
        }

        int i = getHorseType();
        return i == 3 ? "mob.horse.zombie.hit" : (i == 4 ? "mob.horse.skeleton.hit" : (i != 1 && i != 2 ? "mob.horse.hit" : "mob.horse.donkey.hit"));
    }

    public boolean isHorseSaddled()
    {
        return getHorseWatchableBoolean(4);
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        openHorseMouth();

        if (rand.nextInt(10) == 0 && !isMovementBlocked())
        {
            makeHorseRear();
        }

        int i = getHorseType();
        return i == 3 ? "mob.horse.zombie.idle" : (i == 4 ? "mob.horse.skeleton.idle" : (i != 1 && i != 2 ? "mob.horse.idle" : "mob.horse.donkey.idle"));
    }

    protected String getAngrySoundName()
    {
        openHorseMouth();
        makeHorseRear();
        int i = getHorseType();
        return i != 3 && i != 4 ? (i != 1 && i != 2 ? "mob.horse.angry" : "mob.horse.donkey.angry") : null;
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        Block.SoundType block$soundtype = blockIn.stepSound;

        if (worldObj.getBlockState(pos.up()).getBlock() == Blocks.snow_layer)
        {
            block$soundtype = Blocks.snow_layer.stepSound;
        }

        if (!blockIn.getMaterial().isLiquid())
        {
            int i = getHorseType();

            if (riddenByEntity != null && i != 1 && i != 2)
            {
                ++gallopTime;

                if (gallopTime > 5 && gallopTime % 3 == 0)
                {
                    playSound("mob.horse.gallop", block$soundtype.getVolume() * 0.15F, block$soundtype.getFrequency());

                    if (i == 0 && rand.nextInt(10) == 0)
                    {
                        playSound("mob.horse.breathe", block$soundtype.getVolume() * 0.6F, block$soundtype.getFrequency());
                    }
                }
                else if (gallopTime <= 5)
                {
                    playSound("mob.horse.wood", block$soundtype.getVolume() * 0.15F, block$soundtype.getFrequency());
                }
            }
            else if (block$soundtype == Block.soundTypeWood)
            {
                playSound("mob.horse.wood", block$soundtype.getVolume() * 0.15F, block$soundtype.getFrequency());
            }
            else
            {
                playSound("mob.horse.soft", block$soundtype.getVolume() * 0.15F, block$soundtype.getFrequency());
            }
        }
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(EntityHorse.horseJumpStrength);
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(53.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.22499999403953552D);
    }

    /**
     * Will return how many at most can spawn in a chunk at once.
     */
    public int getMaxSpawnedInChunk()
    {
        return 6;
    }

    public int getMaxTemper()
    {
        return 100;
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.8F;
    }

    /**
     * Get number of ticks, at least during which the living entity will be silent.
     */
    public int getTalkInterval()
    {
        return 400;
    }

    public boolean func_110239_cn()
    {
        return getHorseType() == 0 || getHorseArmorIndexSynced() > 0;
    }

    private void resetTexturePrefix()
    {
        texturePrefix = null;
    }

    public boolean func_175507_cI()
    {
        return field_175508_bO;
    }

    private void setHorseTexturePaths()
    {
        texturePrefix = "horse/";
        horseTexturesArray[0] = null;
        horseTexturesArray[1] = null;
        horseTexturesArray[2] = null;
        int i = getHorseType();
        int j = getHorseVariant();

        if (i == 0)
        {
            int k = j & 255;
            int l = (j & 65280) >> 8;

            if (k >= EntityHorse.horseTextures.length)
            {
                field_175508_bO = false;
                return;
            }

            horseTexturesArray[0] = EntityHorse.horseTextures[k];
            texturePrefix = texturePrefix + EntityHorse.HORSE_TEXTURES_ABBR[k];

            if (l >= EntityHorse.horseMarkingTextures.length)
            {
                field_175508_bO = false;
                return;
            }

            horseTexturesArray[1] = EntityHorse.horseMarkingTextures[l];
            texturePrefix = texturePrefix + EntityHorse.HORSE_MARKING_TEXTURES_ABBR[l];
        }
        else
        {
            horseTexturesArray[0] = "";
            texturePrefix = texturePrefix + "_" + i + "_";
        }

        int i1 = getHorseArmorIndexSynced();

        if (i1 >= EntityHorse.horseArmorTextures.length)
        {
            field_175508_bO = false;
        }
        else
        {
            horseTexturesArray[2] = EntityHorse.horseArmorTextures[i1];
            texturePrefix = texturePrefix + EntityHorse.HORSE_ARMOR_TEXTURES_ABBR[i1];
            field_175508_bO = true;
        }
    }

    public String getHorseTexture()
    {
        if (texturePrefix == null)
        {
            setHorseTexturePaths();
        }

        return texturePrefix;
    }

    public String[] getVariantTexturePaths()
    {
        if (texturePrefix == null)
        {
            setHorseTexturePaths();
        }

        return horseTexturesArray;
    }

    public void openGUI(EntityPlayer playerEntity)
    {
        if (!worldObj.isRemote && (riddenByEntity == null || riddenByEntity == playerEntity) && isTame())
        {
            horseChest.setCustomName(getName());
            playerEntity.displayGUIHorse(this, horseChest);
        }
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean interact(EntityPlayer player)
    {
        ItemStack itemstack = player.inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() == Items.spawn_egg)
        {
            return super.interact(player);
        }
        else if (!isTame() && isUndead())
        {
            return false;
        }
        else if (isTame() && isAdultHorse() && player.isSneaking())
        {
            openGUI(player);
            return true;
        }
        else if (func_110253_bW() && riddenByEntity != null)
        {
            return super.interact(player);
        }
        else
        {
            if (itemstack != null)
            {
                boolean flag = false;

                if (canWearArmor())
                {
                    int i = -1;

                    if (itemstack.getItem() == Items.iron_horse_armor)
                    {
                        i = 1;
                    }
                    else if (itemstack.getItem() == Items.golden_horse_armor)
                    {
                        i = 2;
                    }
                    else if (itemstack.getItem() == Items.diamond_horse_armor)
                    {
                        i = 3;
                    }

                    if (i >= 0)
                    {
                        if (!isTame())
                        {
                            makeHorseRearWithSound();
                            return true;
                        }

                        openGUI(player);
                        return true;
                    }
                }

                if (!flag && !isUndead())
                {
                    float f = 0.0F;
                    int j = 0;
                    int k = 0;

                    if (itemstack.getItem() == Items.wheat)
                    {
                        f = 2.0F;
                        j = 20;
                        k = 3;
                    }
                    else if (itemstack.getItem() == Items.sugar)
                    {
                        f = 1.0F;
                        j = 30;
                        k = 3;
                    }
                    else if (Block.getBlockFromItem(itemstack.getItem()) == Blocks.hay_block)
                    {
                        f = 20.0F;
                        j = 180;
                    }
                    else if (itemstack.getItem() == Items.apple)
                    {
                        f = 3.0F;
                        j = 60;
                        k = 3;
                    }
                    else if (itemstack.getItem() == Items.golden_carrot)
                    {
                        f = 4.0F;
                        j = 60;
                        k = 5;

                        if (isTame() && getGrowingAge() == 0)
                        {
                            flag = true;
                            setInLove(player);
                        }
                    }
                    else if (itemstack.getItem() == Items.golden_apple)
                    {
                        f = 10.0F;
                        j = 240;
                        k = 10;

                        if (isTame() && getGrowingAge() == 0)
                        {
                            flag = true;
                            setInLove(player);
                        }
                    }

                    if (getHealth() < getMaxHealth() && f > 0.0F)
                    {
                        heal(f);
                        flag = true;
                    }

                    if (!isAdultHorse() && j > 0)
                    {
                        addGrowth(j);
                        flag = true;
                    }

                    if (k > 0 && (flag || !isTame()) && k < getMaxTemper())
                    {
                        flag = true;
                        increaseTemper(k);
                    }

                    if (flag)
                    {
                        func_110266_cB();
                    }
                }

                if (!isTame() && !flag)
                {
                    if (itemstack != null && itemstack.interactWithEntity(player, this))
                    {
                        return true;
                    }

                    makeHorseRearWithSound();
                    return true;
                }

                if (!flag && canCarryChest() && !isChested() && itemstack.getItem() == Item.getItemFromBlock(Blocks.chest))
                {
                    setChested(true);
                    playSound("mob.chickenplop", 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
                    flag = true;
                    initHorseChest();
                }

                if (!flag && func_110253_bW() && !isHorseSaddled() && itemstack.getItem() == Items.saddle)
                {
                    openGUI(player);
                    return true;
                }

                if (flag)
                {
                    if (!player.capabilities.isCreativeMode && --itemstack.stackSize == 0)
                    {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    }

                    return true;
                }
            }

            if (func_110253_bW() && riddenByEntity == null)
            {
                if (itemstack != null && itemstack.interactWithEntity(player, this))
                {
                    return true;
                }
                else
                {
                    mountTo(player);
                    return true;
                }
            }
            else
            {
                return super.interact(player);
            }
        }
    }

    private void mountTo(EntityPlayer player)
    {
        player.rotationYaw = rotationYaw;
        player.rotationPitch = rotationPitch;
        setEatingHaystack(false);
        setRearing(false);

        if (!worldObj.isRemote)
        {
            player.mountEntity(this);
        }
    }

    /**
     * Return true if the horse entity can wear an armor
     */
    public boolean canWearArmor()
    {
        return getHorseType() == 0;
    }

    /**
     * Return true if the horse entity can carry a chest.
     */
    public boolean canCarryChest()
    {
        int i = getHorseType();
        return i == 2 || i == 1;
    }

    /**
     * Dead and sleeping entities cannot move
     */
    protected boolean isMovementBlocked()
    {
        return riddenByEntity != null && isHorseSaddled() || isEatingHaystack() || isRearing();
    }

    /**
     * Used to know if the horse can be leashed, if he can mate, or if we can interact with him
     */
    public boolean isUndead()
    {
        int i = getHorseType();
        return i == 3 || i == 4;
    }

    /**
     * Return true if the horse entity is sterile (Undead || Mule)
     */
    public boolean isSterile()
    {
        return isUndead() || getHorseType() == 2;
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isBreedingItem(ItemStack stack)
    {
        return false;
    }

    private void func_110210_cH()
    {
        field_110278_bp = 1;
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);

        if (!worldObj.isRemote)
        {
            dropChestItems();
        }
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        if (rand.nextInt(200) == 0)
        {
            func_110210_cH();
        }

        super.onLivingUpdate();

        if (!worldObj.isRemote)
        {
            if (rand.nextInt(900) == 0 && deathTime == 0)
            {
                heal(1.0F);
            }

            if (!isEatingHaystack() && riddenByEntity == null && rand.nextInt(300) == 0 && worldObj.getBlockState(new BlockPos(MathHelper.floor_double(posX), MathHelper.floor_double(posY) - 1, MathHelper.floor_double(posZ))).getBlock() == Blocks.grass)
            {
                setEatingHaystack(true);
            }

            if (isEatingHaystack() && ++eatingHaystackCounter > 50)
            {
                eatingHaystackCounter = 0;
                setEatingHaystack(false);
            }

            if (isBreeding() && !isAdultHorse() && !isEatingHaystack())
            {
                EntityHorse entityhorse = getClosestHorse(this, 16.0D);

                if (entityhorse != null && getDistanceSqToEntity(entityhorse) > 4.0D)
                {
                    navigator.getPathToEntityLiving(entityhorse);
                }
            }
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (worldObj.isRemote && dataWatcher.hasObjectChanged())
        {
            dataWatcher.func_111144_e();
            resetTexturePrefix();
        }

        if (openMouthCounter > 0 && ++openMouthCounter > 30)
        {
            openMouthCounter = 0;
            setHorseWatchableBoolean(128, false);
        }

        if (!worldObj.isRemote && jumpRearingCounter > 0 && ++jumpRearingCounter > 20)
        {
            jumpRearingCounter = 0;
            setRearing(false);
        }

        if (field_110278_bp > 0 && ++field_110278_bp > 8)
        {
            field_110278_bp = 0;
        }

        if (field_110279_bq > 0)
        {
            ++field_110279_bq;

            if (field_110279_bq > 300)
            {
                field_110279_bq = 0;
            }
        }

        prevHeadLean = headLean;

        if (isEatingHaystack())
        {
            headLean += (1.0F - headLean) * 0.4F + 0.05F;

            if (headLean > 1.0F)
            {
                headLean = 1.0F;
            }
        }
        else
        {
            headLean += (0.0F - headLean) * 0.4F - 0.05F;

            if (headLean < 0.0F)
            {
                headLean = 0.0F;
            }
        }

        prevRearingAmount = rearingAmount;

        if (isRearing())
        {
            prevHeadLean = headLean = 0.0F;
            rearingAmount += (1.0F - rearingAmount) * 0.4F + 0.05F;

            if (rearingAmount > 1.0F)
            {
                rearingAmount = 1.0F;
            }
        }
        else
        {
            field_110294_bI = false;
            rearingAmount += (0.8F * rearingAmount * rearingAmount * rearingAmount - rearingAmount) * 0.6F - 0.05F;

            if (rearingAmount < 0.0F)
            {
                rearingAmount = 0.0F;
            }
        }

        prevMouthOpenness = mouthOpenness;

        if (getHorseWatchableBoolean(128))
        {
            mouthOpenness += (1.0F - mouthOpenness) * 0.7F + 0.05F;

            if (mouthOpenness > 1.0F)
            {
                mouthOpenness = 1.0F;
            }
        }
        else
        {
            mouthOpenness += (0.0F - mouthOpenness) * 0.7F - 0.05F;

            if (mouthOpenness < 0.0F)
            {
                mouthOpenness = 0.0F;
            }
        }
    }

    private void openHorseMouth()
    {
        if (!worldObj.isRemote)
        {
            openMouthCounter = 1;
            setHorseWatchableBoolean(128, true);
        }
    }

    /**
     * Return true if the horse entity ready to mate. (no rider, not riding, tame, adult, not steril...)
     */
    private boolean canMate()
    {
        return riddenByEntity == null && ridingEntity == null && isTame() && isAdultHorse() && !isSterile() && getHealth() >= getMaxHealth() && isInLove();
    }

    public void setEating(boolean eating)
    {
        setHorseWatchableBoolean(32, eating);
    }

    public void setEatingHaystack(boolean p_110227_1_)
    {
        setEating(p_110227_1_);
    }

    public void setRearing(boolean rearing)
    {
        if (rearing)
        {
            setEatingHaystack(false);
        }

        setHorseWatchableBoolean(64, rearing);
    }

    private void makeHorseRear()
    {
        if (!worldObj.isRemote)
        {
            jumpRearingCounter = 1;
            setRearing(true);
        }
    }

    public void makeHorseRearWithSound()
    {
        makeHorseRear();
        String s = getAngrySoundName();

        if (s != null)
        {
            playSound(s, getSoundVolume(), getSoundPitch());
        }
    }

    public void dropChestItems()
    {
        dropItemsInChest(this, horseChest);
        dropChests();
    }

    private void dropItemsInChest(Entity entityIn, AnimalChest animalChestIn)
    {
        if (animalChestIn != null && !worldObj.isRemote)
        {
            for (int i = 0; i < animalChestIn.getSizeInventory(); ++i)
            {
                ItemStack itemstack = animalChestIn.getStackInSlot(i);

                if (itemstack != null)
                {
                    entityDropItem(itemstack, 0.0F);
                }
            }
        }
    }

    public boolean setTamedBy(EntityPlayer player)
    {
        setOwnerId(player.getUniqueID().toString());
        setHorseTamed(true);
        return true;
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    public void moveEntityWithHeading(float strafe, float forward)
    {
        if (riddenByEntity != null && riddenByEntity instanceof EntityLivingBase && isHorseSaddled())
        {
            prevRotationYaw = rotationYaw = riddenByEntity.rotationYaw;
            rotationPitch = riddenByEntity.rotationPitch * 0.5F;
            setRotation(rotationYaw, rotationPitch);
            rotationYawHead = renderYawOffset = rotationYaw;
            strafe = ((EntityLivingBase) riddenByEntity).moveStrafing * 0.5F;
            forward = ((EntityLivingBase) riddenByEntity).moveForward;

            if (forward <= 0.0F)
            {
                forward *= 0.25F;
                gallopTime = 0;
            }

            if (onGround && jumpPower == 0.0F && isRearing() && !field_110294_bI)
            {
                strafe = 0.0F;
                forward = 0.0F;
            }

            if (jumpPower > 0.0F && !isHorseJumping() && onGround)
            {
                motionY = getHorseJumpStrength() * (double) jumpPower;

                if (isPotionActive(Potion.jump))
                {
                    motionY += (float)(getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
                }

                setHorseJumping(true);
                isAirBorne = true;

                if (forward > 0.0F)
                {
                    float f = MathHelper.sin(rotationYaw * (float)Math.PI / 180.0F);
                    float f1 = MathHelper.cos(rotationYaw * (float)Math.PI / 180.0F);
                    motionX += -0.4F * f * jumpPower;
                    motionZ += 0.4F * f1 * jumpPower;
                    playSound("mob.horse.jump", 0.4F, 1.0F);
                }

                jumpPower = 0.0F;
            }

            stepHeight = 1.0F;
            jumpMovementFactor = getAIMoveSpeed() * 0.1F;

            if (!worldObj.isRemote)
            {
                setAIMoveSpeed((float) getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
                super.moveEntityWithHeading(strafe, forward);
            }

            if (onGround)
            {
                jumpPower = 0.0F;
                setHorseJumping(false);
            }

            prevLimbSwingAmount = limbSwingAmount;
            double d1 = posX - prevPosX;
            double d0 = posZ - prevPosZ;
            float f2 = MathHelper.sqrt_double(d1 * d1 + d0 * d0) * 4.0F;

            if (f2 > 1.0F)
            {
                f2 = 1.0F;
            }

            limbSwingAmount += (f2 - limbSwingAmount) * 0.4F;
            limbSwing += limbSwingAmount;
        }
        else
        {
            stepHeight = 0.5F;
            jumpMovementFactor = 0.02F;
            super.moveEntityWithHeading(strafe, forward);
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("EatingHaystack", isEatingHaystack());
        tagCompound.setBoolean("ChestedHorse", isChested());
        tagCompound.setBoolean("HasReproduced", getHasReproduced());
        tagCompound.setBoolean("Bred", isBreeding());
        tagCompound.setInteger("Type", getHorseType());
        tagCompound.setInteger("Variant", getHorseVariant());
        tagCompound.setInteger("Temper", getTemper());
        tagCompound.setBoolean("Tame", isTame());
        tagCompound.setString("OwnerUUID", getOwnerId());

        if (isChested())
        {
            NBTTagList nbttaglist = new NBTTagList();

            for (int i = 2; i < horseChest.getSizeInventory(); ++i)
            {
                ItemStack itemstack = horseChest.getStackInSlot(i);

                if (itemstack != null)
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    nbttagcompound.setByte("Slot", (byte)i);
                    itemstack.writeToNBT(nbttagcompound);
                    nbttaglist.appendTag(nbttagcompound);
                }
            }

            tagCompound.setTag("Items", nbttaglist);
        }

        if (horseChest.getStackInSlot(1) != null)
        {
            tagCompound.setTag("ArmorItem", horseChest.getStackInSlot(1).writeToNBT(new NBTTagCompound()));
        }

        if (horseChest.getStackInSlot(0) != null)
        {
            tagCompound.setTag("SaddleItem", horseChest.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        setEatingHaystack(tagCompund.getBoolean("EatingHaystack"));
        setBreeding(tagCompund.getBoolean("Bred"));
        setChested(tagCompund.getBoolean("ChestedHorse"));
        setHasReproduced(tagCompund.getBoolean("HasReproduced"));
        setHorseType(tagCompund.getInteger("Type"));
        setHorseVariant(tagCompund.getInteger("Variant"));
        setTemper(tagCompund.getInteger("Temper"));
        setHorseTamed(tagCompund.getBoolean("Tame"));
        String s = "";

        if (tagCompund.hasKey("OwnerUUID", 8))
        {
            s = tagCompund.getString("OwnerUUID");
        }
        else
        {
            String s1 = tagCompund.getString("Owner");
            s = PreYggdrasilConverter.getStringUUIDFromName(s1);
        }

        if (s.length() > 0)
        {
            setOwnerId(s);
        }

        IAttributeInstance iattributeinstance = getAttributeMap().getAttributeInstanceByName("Speed");

        if (iattributeinstance != null)
        {
            getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(iattributeinstance.getBaseValue() * 0.25D);
        }

        if (isChested())
        {
            NBTTagList nbttaglist = tagCompund.getTagList("Items", 10);
            initHorseChest();

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;

                if (j >= 2 && j < horseChest.getSizeInventory())
                {
                    horseChest.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound));
                }
            }
        }

        if (tagCompund.hasKey("ArmorItem", 10))
        {
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(tagCompund.getCompoundTag("ArmorItem"));

            if (itemstack != null && EntityHorse.isArmorItem(itemstack.getItem()))
            {
                horseChest.setInventorySlotContents(1, itemstack);
            }
        }

        if (tagCompund.hasKey("SaddleItem", 10))
        {
            ItemStack itemstack1 = ItemStack.loadItemStackFromNBT(tagCompund.getCompoundTag("SaddleItem"));

            if (itemstack1 != null && itemstack1.getItem() == Items.saddle)
            {
                horseChest.setInventorySlotContents(0, itemstack1);
            }
        }
        else if (tagCompund.getBoolean("Saddle"))
        {
            horseChest.setInventorySlotContents(0, new ItemStack(Items.saddle));
        }

        updateHorseSlots();
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    public boolean canMateWith(EntityAnimal otherAnimal)
    {
        if (otherAnimal == this)
        {
            return false;
        }
        else if (otherAnimal.getClass() != getClass())
        {
            return false;
        }
        else
        {
            EntityHorse entityhorse = (EntityHorse)otherAnimal;

            if (canMate() && entityhorse.canMate())
            {
                int i = getHorseType();
                int j = entityhorse.getHorseType();
                return i == j || i == 0 && j == 1 || i == 1 && j == 0;
            }
            else
            {
                return false;
            }
        }
    }

    public EntityAgeable createChild(EntityAgeable ageable)
    {
        EntityHorse entityhorse = (EntityHorse)ageable;
        EntityHorse entityhorse1 = new EntityHorse(worldObj);
        int i = getHorseType();
        int j = entityhorse.getHorseType();
        int k = 0;

        if (i == j)
        {
            k = i;
        }
        else if (i == 0 && j == 1 || i == 1 && j == 0)
        {
            k = 2;
        }

        if (k == 0)
        {
            int i1 = rand.nextInt(9);
            int l;

            if (i1 < 4)
            {
                l = getHorseVariant() & 255;
            }
            else if (i1 < 8)
            {
                l = entityhorse.getHorseVariant() & 255;
            }
            else
            {
                l = rand.nextInt(7);
            }

            int j1 = rand.nextInt(5);

            if (j1 < 2)
            {
                l = l | getHorseVariant() & 65280;
            }
            else if (j1 < 4)
            {
                l = l | entityhorse.getHorseVariant() & 65280;
            }
            else
            {
                l = l | rand.nextInt(5) << 8 & 65280;
            }

            entityhorse1.setHorseVariant(l);
        }

        entityhorse1.setHorseType(k);
        double d1 = getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue() + ageable.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue() + (double) getModifiedMaxHealth();
        entityhorse1.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(d1 / 3.0D);
        double d2 = getEntityAttribute(EntityHorse.horseJumpStrength).getBaseValue() + ageable.getEntityAttribute(EntityHorse.horseJumpStrength).getBaseValue() + getModifiedJumpStrength();
        entityhorse1.getEntityAttribute(EntityHorse.horseJumpStrength).setBaseValue(d2 / 3.0D);
        double d0 = getEntityAttribute(SharedMonsterAttributes.movementSpeed).getBaseValue() + ageable.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getBaseValue() + getModifiedMovementSpeed();
        entityhorse1.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(d0 / 3.0D);
        return entityhorse1;
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata)
    {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        int i = 0;
        int j = 0;

        if (livingdata instanceof EntityHorse.GroupData)
        {
            i = ((EntityHorse.GroupData)livingdata).horseType;
            j = ((EntityHorse.GroupData)livingdata).horseVariant & 255 | rand.nextInt(5) << 8;
        }
        else
        {
            if (rand.nextInt(10) == 0)
            {
                i = 1;
            }
            else
            {
                int k = rand.nextInt(7);
                int l = rand.nextInt(5);
                i = 0;
                j = k | l << 8;
            }

            livingdata = new EntityHorse.GroupData(i, j);
        }

        setHorseType(i);
        setHorseVariant(j);

        if (rand.nextInt(5) == 0)
        {
            setGrowingAge(-24000);
        }

        if (i != 4 && i != 3)
        {
            getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(getModifiedMaxHealth());

            if (i == 0)
            {
                getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(getModifiedMovementSpeed());
            }
            else
            {
                getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.17499999701976776D);
            }
        }
        else
        {
            getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(15.0D);
            getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.20000000298023224D);
        }

        if (i != 2 && i != 1)
        {
            getEntityAttribute(EntityHorse.horseJumpStrength).setBaseValue(getModifiedJumpStrength());
        }
        else
        {
            getEntityAttribute(EntityHorse.horseJumpStrength).setBaseValue(0.5D);
        }

        setHealth(getMaxHealth());
        return livingdata;
    }

    public float getGrassEatingAmount(float p_110258_1_)
    {
        return prevHeadLean + (headLean - prevHeadLean) * p_110258_1_;
    }

    public float getRearingAmount(float p_110223_1_)
    {
        return prevRearingAmount + (rearingAmount - prevRearingAmount) * p_110223_1_;
    }

    public float getMouthOpennessAngle(float p_110201_1_)
    {
        return prevMouthOpenness + (mouthOpenness - prevMouthOpenness) * p_110201_1_;
    }

    public void setJumpPower(int jumpPowerIn)
    {
        if (isHorseSaddled())
        {
            if (jumpPowerIn < 0)
            {
                jumpPowerIn = 0;
            }
            else
            {
                field_110294_bI = true;
                makeHorseRear();
            }

            if (jumpPowerIn >= 90)
            {
                jumpPower = 1.0F;
            }
            else
            {
                jumpPower = 0.4F + 0.4F * (float)jumpPowerIn / 90.0F;
            }
        }
    }

    /**
     * "Spawns particles for the horse entity. par1 tells whether to spawn hearts. If it is false, it spawns smoke."
     */
    protected void spawnHorseParticles(boolean p_110216_1_)
    {
        EnumParticleTypes enumparticletypes = p_110216_1_ ? EnumParticleTypes.HEART : EnumParticleTypes.SMOKE_NORMAL;

        for (int i = 0; i < 7; ++i)
        {
            double d0 = rand.nextGaussian() * 0.02D;
            double d1 = rand.nextGaussian() * 0.02D;
            double d2 = rand.nextGaussian() * 0.02D;
            worldObj.spawnParticle(enumparticletypes, posX + (double)(rand.nextFloat() * width * 2.0F) - (double) width, posY + 0.5D + (double)(rand.nextFloat() * height), posZ + (double)(rand.nextFloat() * width * 2.0F) - (double) width, d0, d1, d2);
        }
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 7)
        {
            spawnHorseParticles(true);
        }
        else if (id == 6)
        {
            spawnHorseParticles(false);
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }

    public void updateRiderPosition()
    {
        super.updateRiderPosition();

        if (prevRearingAmount > 0.0F)
        {
            float f = MathHelper.sin(renderYawOffset * (float)Math.PI / 180.0F);
            float f1 = MathHelper.cos(renderYawOffset * (float)Math.PI / 180.0F);
            float f2 = 0.7F * prevRearingAmount;
            float f3 = 0.15F * prevRearingAmount;
            riddenByEntity.setPosition(posX + (double)(f2 * f), posY + getMountedYOffset() + riddenByEntity.getYOffset() + (double)f3, posZ - (double)(f2 * f1));

            if (riddenByEntity instanceof EntityLivingBase)
            {
                ((EntityLivingBase) riddenByEntity).renderYawOffset = renderYawOffset;
            }
        }
    }

    /**
     * Returns randomized max health
     */
    private float getModifiedMaxHealth()
    {
        return 15.0F + (float) rand.nextInt(8) + (float) rand.nextInt(9);
    }

    /**
     * Returns randomized jump strength
     */
    private double getModifiedJumpStrength()
    {
        return 0.4000000059604645D + rand.nextDouble() * 0.2D + rand.nextDouble() * 0.2D + rand.nextDouble() * 0.2D;
    }

    /**
     * Returns randomized movement speed
     */
    private double getModifiedMovementSpeed()
    {
        return (0.44999998807907104D + rand.nextDouble() * 0.3D + rand.nextDouble() * 0.3D + rand.nextDouble() * 0.3D) * 0.25D;
    }

    /**
     * Returns true if given item is horse armor
     */
    public static boolean isArmorItem(Item p_146085_0_)
    {
        return p_146085_0_ == Items.iron_horse_armor || p_146085_0_ == Items.golden_horse_armor || p_146085_0_ == Items.diamond_horse_armor;
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    public boolean isOnLadder()
    {
        return false;
    }

    public float getEyeHeight()
    {
        return height;
    }

    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn)
    {
        if (inventorySlot == 499 && canCarryChest())
        {
            if (itemStackIn == null && isChested())
            {
                setChested(false);
                initHorseChest();
                return true;
            }

            if (itemStackIn != null && itemStackIn.getItem() == Item.getItemFromBlock(Blocks.chest) && !isChested())
            {
                setChested(true);
                initHorseChest();
                return true;
            }
        }

        int i = inventorySlot - 400;

        if (i >= 0 && i < 2 && i < horseChest.getSizeInventory())
        {
            if (i == 0 && itemStackIn != null && itemStackIn.getItem() != Items.saddle)
            {
                return false;
            }
            else if (i != 1 || (itemStackIn == null || EntityHorse.isArmorItem(itemStackIn.getItem())) && canWearArmor())
            {
                horseChest.setInventorySlotContents(i, itemStackIn);
                updateHorseSlots();
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            int j = inventorySlot - 500 + 2;

            if (j >= 2 && j < horseChest.getSizeInventory())
            {
                horseChest.setInventorySlotContents(j, itemStackIn);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public static class GroupData implements IEntityLivingData
    {
        public int horseType;
        public int horseVariant;

        public GroupData(int type, int variant)
        {
            horseType = type;
            horseVariant = variant;
        }
    }
}
