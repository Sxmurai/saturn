package net.minecraft.entity.player;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.FoodStats;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

@SuppressWarnings("incomplete-switch")
public abstract class EntityPlayer extends EntityLivingBase
{
    /** Inventory of the player */
    public InventoryPlayer inventory = new InventoryPlayer(this);
    private InventoryEnderChest theInventoryEnderChest = new InventoryEnderChest();

    /**
     * The Container for the player's inventory (which opens when they press E)
     */
    public Container inventoryContainer;

    /** The Container the player has open. */
    public Container openContainer;

    /** The food object of the player, the general hunger logic. */
    protected FoodStats foodStats = new FoodStats();

    /**
     * Used to tell if the player pressed jump twice. If this is at 0 and it's pressed (And they are allowed to fly, as
     * defined in the player's movementInput) it sets this to 7. If it's pressed and it's greater than 0 enable fly.
     */
    protected int flyToggleTimer;
    public float prevCameraYaw;
    public float cameraYaw;

    /**
     * Used by EntityPlayer to prevent too many xp orbs from getting absorbed at once.
     */
    public int xpCooldown;
    public double prevChasingPosX;
    public double prevChasingPosY;
    public double prevChasingPosZ;
    public double chasingPosX;
    public double chasingPosY;
    public double chasingPosZ;

    /** Boolean value indicating weather a player is sleeping or not */
    protected boolean sleeping;

    /** the current location of the player */
    public BlockPos playerLocation;
    private int sleepTimer;
    public float renderOffsetX;
    public float renderOffsetY;
    public float renderOffsetZ;

    /** holds the spawn chunk of the player */
    private BlockPos spawnChunk;

    /**
     * Whether this player's spawn point is forced, preventing execution of bed checks.
     */
    private boolean spawnForced;

    /** Holds the coordinate of the player when enter a minecraft to ride. */
    private BlockPos startMinecartRidingCoordinate;

    /** The player's capabilities. (See class PlayerCapabilities) */
    public PlayerCapabilities capabilities = new PlayerCapabilities();

    /** The current experience level the player is on. */
    public int experienceLevel;

    /**
     * The total amount of experience the player has. This also includes the amount of experience within their
     * Experience Bar.
     */
    public int experienceTotal;

    /**
     * The current amount of experience the player has within their Experience Bar.
     */
    public float experience;
    private int xpSeed;

    /**
     * This is the item that is in use when the player is holding down the useItemButton (e.g., bow, food, sword)
     */
    private ItemStack itemInUse;

    /**
     * This field starts off equal to getMaxItemUseDuration and is decremented on each tick
     */
    private int itemInUseCount;
    protected float speedOnGround = 0.1F;
    protected float speedInAir = 0.02F;
    private int lastXPSound;

    /** The player's unique game profile */
    private final GameProfile gameProfile;
    private boolean hasReducedDebug = false;

    /**
     * An instance of a fishing rod's hook. If this isn't null, the icon image of the fishing rod is slightly different
     */
    public EntityFishHook fishEntity;

    public EntityPlayer(World worldIn, GameProfile gameProfileIn)
    {
        super(worldIn);
        entityUniqueID = EntityPlayer.getUUID(gameProfileIn);
        gameProfile = gameProfileIn;
        inventoryContainer = new ContainerPlayer(inventory, !worldIn.isRemote, this);
        openContainer = inventoryContainer;
        BlockPos blockpos = worldIn.getSpawnPoint();
        setLocationAndAngles((double)blockpos.getX() + 0.5D, blockpos.getY() + 1, (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
        field_70741_aB = 180.0F;
        fireResistance = 20;
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.10000000149011612D);
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(16, Byte.valueOf((byte)0));
        dataWatcher.addObject(17, Float.valueOf(0.0F));
        dataWatcher.addObject(18, Integer.valueOf(0));
        dataWatcher.addObject(10, Byte.valueOf((byte)0));
    }

    /**
     * returns the ItemStack containing the itemInUse
     */
    public ItemStack getItemInUse()
    {
        return itemInUse;
    }

    /**
     * Returns the item in use count
     */
    public int getItemInUseCount()
    {
        return itemInUseCount;
    }

    /**
     * Checks if the entity is currently using an item (e.g., bow, food, sword) by holding down the useItemButton
     */
    public boolean isUsingItem()
    {
        return itemInUse != null;
    }

    /**
     * gets the duration for how long the current itemInUse has been in use
     */
    public int getItemInUseDuration()
    {
        return isUsingItem() ? itemInUse.getMaxItemUseDuration() - itemInUseCount : 0;
    }

    public void stopUsingItem()
    {
        if (itemInUse != null)
        {
            itemInUse.onPlayerStoppedUsing(worldObj, this, itemInUseCount);
        }

        clearItemInUse();
    }

    public void clearItemInUse()
    {
        itemInUse = null;
        itemInUseCount = 0;

        if (!worldObj.isRemote)
        {
            setEating(false);
        }
    }

    public boolean isBlocking()
    {
        return isUsingItem() && itemInUse.getItem().getItemUseAction(itemInUse) == EnumAction.BLOCK;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        noClip = isSpectator();

        if (isSpectator())
        {
            onGround = false;
        }

        if (itemInUse != null)
        {
            ItemStack itemstack = inventory.getCurrentItem();

            if (itemstack == itemInUse)
            {
                if (itemInUseCount <= 25 && itemInUseCount % 4 == 0)
                {
                    updateItemUse(itemstack, 5);
                }

                if (--itemInUseCount == 0 && !worldObj.isRemote)
                {
                    onItemUseFinish();
                }
            }
            else
            {
                clearItemInUse();
            }
        }

        if (xpCooldown > 0)
        {
            --xpCooldown;
        }

        if (isPlayerSleeping())
        {
            ++sleepTimer;

            if (sleepTimer > 100)
            {
                sleepTimer = 100;
            }

            if (!worldObj.isRemote)
            {
                if (!isInBed())
                {
                    wakeUpPlayer(true, true, false);
                }
                else if (worldObj.isDaytime())
                {
                    wakeUpPlayer(false, true, true);
                }
            }
        }
        else if (sleepTimer > 0)
        {
            ++sleepTimer;

            if (sleepTimer >= 110)
            {
                sleepTimer = 0;
            }
        }

        super.onUpdate();

        if (!worldObj.isRemote && openContainer != null && !openContainer.canInteractWith(this))
        {
            closeScreen();
            openContainer = inventoryContainer;
        }

        if (isBurning() && capabilities.disableDamage)
        {
            extinguish();
        }

        prevChasingPosX = chasingPosX;
        prevChasingPosY = chasingPosY;
        prevChasingPosZ = chasingPosZ;
        double d5 = posX - chasingPosX;
        double d0 = posY - chasingPosY;
        double d1 = posZ - chasingPosZ;
        double d2 = 10.0D;

        if (d5 > d2)
        {
            prevChasingPosX = chasingPosX = posX;
        }

        if (d1 > d2)
        {
            prevChasingPosZ = chasingPosZ = posZ;
        }

        if (d0 > d2)
        {
            prevChasingPosY = chasingPosY = posY;
        }

        if (d5 < -d2)
        {
            prevChasingPosX = chasingPosX = posX;
        }

        if (d1 < -d2)
        {
            prevChasingPosZ = chasingPosZ = posZ;
        }

        if (d0 < -d2)
        {
            prevChasingPosY = chasingPosY = posY;
        }

        chasingPosX += d5 * 0.25D;
        chasingPosZ += d1 * 0.25D;
        chasingPosY += d0 * 0.25D;

        if (ridingEntity == null)
        {
            startMinecartRidingCoordinate = null;
        }

        if (!worldObj.isRemote)
        {
            foodStats.onUpdate(this);
            triggerAchievement(StatList.minutesPlayedStat);

            if (isEntityAlive())
            {
                triggerAchievement(StatList.timeSinceDeathStat);
            }
        }

        int i = 29999999;
        double d3 = MathHelper.clamp_double(posX, -2.9999999E7D, 2.9999999E7D);
        double d4 = MathHelper.clamp_double(posZ, -2.9999999E7D, 2.9999999E7D);

        if (d3 != posX || d4 != posZ)
        {
            setPosition(d3, posY, d4);
        }
    }

    /**
     * Return the amount of time this entity should stay in a portal before being transported.
     */
    public int getMaxInPortalTime()
    {
        return capabilities.disableDamage ? 0 : 80;
    }

    protected String getSwimSound()
    {
        return "game.player.swim";
    }

    protected String getSplashSound()
    {
        return "game.player.swim.splash";
    }

    /**
     * Return the amount of cooldown before this entity can use a portal again.
     */
    public int getPortalCooldown()
    {
        return 10;
    }

    public void playSound(String name, float volume, float pitch)
    {
        worldObj.playSoundToNearExcept(this, name, volume, pitch);
    }

    /**
     * Plays sounds and makes particles for item in use state
     */
    protected void updateItemUse(ItemStack itemStackIn, int p_71010_2_)
    {
        if (itemStackIn.getItemUseAction() == EnumAction.DRINK)
        {
            playSound("random.drink", 0.5F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (itemStackIn.getItemUseAction() == EnumAction.EAT)
        {
            for (int i = 0; i < p_71010_2_; ++i)
            {
                Vec3 vec3 = new Vec3(((double) rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
                vec3 = vec3.rotatePitch(-rotationPitch * (float)Math.PI / 180.0F);
                vec3 = vec3.rotateYaw(-rotationYaw * (float)Math.PI / 180.0F);
                double d0 = (double)(-rand.nextFloat()) * 0.6D - 0.3D;
                Vec3 vec31 = new Vec3(((double) rand.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
                vec31 = vec31.rotatePitch(-rotationPitch * (float)Math.PI / 180.0F);
                vec31 = vec31.rotateYaw(-rotationYaw * (float)Math.PI / 180.0F);
                vec31 = vec31.addVector(posX, posY + (double) getEyeHeight(), posZ);

                if (itemStackIn.getHasSubtypes())
                {
                    worldObj.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec31.xCoord, vec31.yCoord, vec31.zCoord, vec3.xCoord, vec3.yCoord + 0.05D, vec3.zCoord, Item.getIdFromItem(itemStackIn.getItem()), itemStackIn.getMetadata());
                }
                else
                {
                    worldObj.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec31.xCoord, vec31.yCoord, vec31.zCoord, vec3.xCoord, vec3.yCoord + 0.05D, vec3.zCoord, Item.getIdFromItem(itemStackIn.getItem()));
                }
            }

            playSound("random.eat", 0.5F + 0.5F * (float) rand.nextInt(2), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
        }
    }

    /**
     * Used for when item use count runs out, ie: eating completed
     */
    protected void onItemUseFinish()
    {
        if (itemInUse != null)
        {
            updateItemUse(itemInUse, 16);
            int i = itemInUse.stackSize;
            ItemStack itemstack = itemInUse.onItemUseFinish(worldObj, this);

            if (itemstack != itemInUse || itemstack != null && itemstack.stackSize != i)
            {
                inventory.mainInventory[inventory.currentItem] = itemstack;

                if (itemstack.stackSize == 0)
                {
                    inventory.mainInventory[inventory.currentItem] = null;
                }
            }

            clearItemInUse();
        }
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 9)
        {
            onItemUseFinish();
        }
        else if (id == 23)
        {
            hasReducedDebug = false;
        }
        else if (id == 22)
        {
            hasReducedDebug = true;
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }

    /**
     * Dead and sleeping entities cannot move
     */
    protected boolean isMovementBlocked()
    {
        return getHealth() <= 0.0F || isPlayerSleeping();
    }

    /**
     * set current crafting inventory back to the 2x2 square
     */
    protected void closeScreen()
    {
        openContainer = inventoryContainer;
    }

    /**
     * Handles updating while being ridden by an entity
     */
    public void updateRidden()
    {
        if (!worldObj.isRemote && isSneaking())
        {
            mountEntity(null);
            setSneaking(false);
        }
        else
        {
            double d0 = posX;
            double d1 = posY;
            double d2 = posZ;
            float f = rotationYaw;
            float f1 = rotationPitch;
            super.updateRidden();
            prevCameraYaw = cameraYaw;
            cameraYaw = 0.0F;
            addMountedMovementStat(posX - d0, posY - d1, posZ - d2);

            if (ridingEntity instanceof EntityPig)
            {
                rotationPitch = f1;
                rotationYaw = f;
                renderYawOffset = ((EntityPig) ridingEntity).renderYawOffset;
            }
        }
    }

    /**
     * Keeps moving the entity up so it isn't colliding with blocks and other requirements for this entity to be spawned
     * (only actually used on players though its also on Entity)
     */
    public void preparePlayerToSpawn()
    {
        setSize(0.6F, 1.8F);
        super.preparePlayerToSpawn();
        setHealth(getMaxHealth());
        deathTime = 0;
    }

    protected void updateEntityActionState()
    {
        super.updateEntityActionState();
        updateArmSwingProgress();
        rotationYawHead = rotationYaw;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        if (flyToggleTimer > 0)
        {
            --flyToggleTimer;
        }

        if (worldObj.getDifficulty() == EnumDifficulty.PEACEFUL && worldObj.getGameRules().getBoolean("naturalRegeneration"))
        {
            if (getHealth() < getMaxHealth() && ticksExisted % 20 == 0)
            {
                heal(1.0F);
            }

            if (foodStats.needFood() && ticksExisted % 10 == 0)
            {
                foodStats.setFoodLevel(foodStats.getFoodLevel() + 1);
            }
        }

        inventory.decrementAnimations();
        prevCameraYaw = cameraYaw;
        super.onLivingUpdate();
        IAttributeInstance iattributeinstance = getEntityAttribute(SharedMonsterAttributes.movementSpeed);

        if (!worldObj.isRemote)
        {
            iattributeinstance.setBaseValue(capabilities.getWalkSpeed());
        }

        jumpMovementFactor = speedInAir;

        if (isSprinting())
        {
            jumpMovementFactor = (float)((double) jumpMovementFactor + (double) speedInAir * 0.3D);
        }

        setAIMoveSpeed((float)iattributeinstance.getAttributeValue());
        float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        float f1 = (float)(Math.atan(-motionY * 0.20000000298023224D) * 15.0D);

        if (f > 0.1F)
        {
            f = 0.1F;
        }

        if (!onGround || getHealth() <= 0.0F)
        {
            f = 0.0F;
        }

        if (onGround || getHealth() <= 0.0F)
        {
            f1 = 0.0F;
        }

        cameraYaw += (f - cameraYaw) * 0.4F;
        cameraPitch += (f1 - cameraPitch) * 0.8F;

        if (getHealth() > 0.0F && !isSpectator())
        {
            AxisAlignedBB axisalignedbb = null;

            if (ridingEntity != null && !ridingEntity.isDead)
            {
                axisalignedbb = getEntityBoundingBox().union(ridingEntity.getEntityBoundingBox()).expand(1.0D, 0.0D, 1.0D);
            }
            else
            {
                axisalignedbb = getEntityBoundingBox().expand(1.0D, 0.5D, 1.0D);
            }

            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, axisalignedbb);

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity = list.get(i);

                if (!entity.isDead)
                {
                    collideWithPlayer(entity);
                }
            }
        }
    }

    private void collideWithPlayer(Entity p_71044_1_)
    {
        p_71044_1_.onCollideWithPlayer(this);
    }

    public int getScore()
    {
        return dataWatcher.getWatchableObjectInt(18);
    }

    /**
     * Set player's score
     */
    public void setScore(int p_85040_1_)
    {
        dataWatcher.updateObject(18, Integer.valueOf(p_85040_1_));
    }

    /**
     * Add to player's score
     */
    public void addScore(int p_85039_1_)
    {
        int i = getScore();
        dataWatcher.updateObject(18, Integer.valueOf(i + p_85039_1_));
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);
        setSize(0.2F, 0.2F);
        setPosition(posX, posY, posZ);
        motionY = 0.10000000149011612D;

        if (getName().equals("Notch"))
        {
            dropItem(new ItemStack(Items.apple, 1), true, false);
        }

        if (!worldObj.getGameRules().getBoolean("keepInventory"))
        {
            inventory.dropAllItems();
        }

        if (cause != null)
        {
            motionX = -MathHelper.cos((attackedAtYaw + rotationYaw) * (float)Math.PI / 180.0F) * 0.1F;
            motionZ = -MathHelper.sin((attackedAtYaw + rotationYaw) * (float)Math.PI / 180.0F) * 0.1F;
        }
        else
        {
            motionX = motionZ = 0.0D;
        }

        triggerAchievement(StatList.deathsStat);
        func_175145_a(StatList.timeSinceDeathStat);
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "game.player.hurt";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "game.player.die";
    }

    /**
     * Adds a value to the player score. Currently not actually used and the entity passed in does nothing. Args:
     * entity, scoreToAdd
     */
    public void addToPlayerScore(Entity entityIn, int amount)
    {
        addScore(amount);
        Collection<ScoreObjective> collection = getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.totalKillCount);

        if (entityIn instanceof EntityPlayer)
        {
            triggerAchievement(StatList.playerKillsStat);
            collection.addAll(getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.playerKillCount));
            collection.addAll(func_175137_e(entityIn));
        }
        else
        {
            triggerAchievement(StatList.mobKillsStat);
        }

        for (ScoreObjective scoreobjective : collection)
        {
            Score score = getWorldScoreboard().getValueFromObjective(getName(), scoreobjective);
            score.func_96648_a();
        }
    }

    private Collection<ScoreObjective> func_175137_e(Entity p_175137_1_)
    {
        ScorePlayerTeam scoreplayerteam = getWorldScoreboard().getPlayersTeam(getName());

        if (scoreplayerteam != null)
        {
            int i = scoreplayerteam.getChatFormat().getColorIndex();

            if (i >= 0 && i < IScoreObjectiveCriteria.field_178793_i.length)
            {
                for (ScoreObjective scoreobjective : getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.field_178793_i[i]))
                {
                    Score score = getWorldScoreboard().getValueFromObjective(p_175137_1_.getName(), scoreobjective);
                    score.func_96648_a();
                }
            }
        }

        ScorePlayerTeam scoreplayerteam1 = getWorldScoreboard().getPlayersTeam(p_175137_1_.getName());

        if (scoreplayerteam1 != null)
        {
            int j = scoreplayerteam1.getChatFormat().getColorIndex();

            if (j >= 0 && j < IScoreObjectiveCriteria.field_178792_h.length)
            {
                return getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.field_178792_h[j]);
            }
        }

        return Lists.newArrayList();
    }

    /**
     * Called when player presses the drop item key
     */
    public EntityItem dropOneItem(boolean dropAll)
    {
        return dropItem(inventory.decrStackSize(inventory.currentItem, dropAll && inventory.getCurrentItem() != null ? inventory.getCurrentItem().stackSize : 1), false, true);
    }

    /**
     * Args: itemstack, flag
     */
    public EntityItem dropPlayerItemWithRandomChoice(ItemStack itemStackIn, boolean unused)
    {
        return dropItem(itemStackIn, false, false);
    }

    public EntityItem dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem)
    {
        if (droppedItem == null)
        {
            return null;
        }
        else if (droppedItem.stackSize == 0)
        {
            return null;
        }
        else
        {
            double d0 = posY - 0.30000001192092896D + (double) getEyeHeight();
            EntityItem entityitem = new EntityItem(worldObj, posX, d0, posZ, droppedItem);
            entityitem.setPickupDelay(40);

            if (traceItem)
            {
                entityitem.setThrower(getName());
            }

            if (dropAround)
            {
                float f = rand.nextFloat() * 0.5F;
                float f1 = rand.nextFloat() * (float)Math.PI * 2.0F;
                entityitem.motionX = -MathHelper.sin(f1) * f;
                entityitem.motionZ = MathHelper.cos(f1) * f;
                entityitem.motionY = 0.20000000298023224D;
            }
            else
            {
                float f2 = 0.3F;
                entityitem.motionX = -MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float)Math.PI) * f2;
                entityitem.motionZ = MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float)Math.PI) * f2;
                entityitem.motionY = -MathHelper.sin(rotationPitch / 180.0F * (float)Math.PI) * f2 + 0.1F;
                float f3 = rand.nextFloat() * (float)Math.PI * 2.0F;
                f2 = 0.02F * rand.nextFloat();
                entityitem.motionX += Math.cos(f3) * (double)f2;
                entityitem.motionY += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                entityitem.motionZ += Math.sin(f3) * (double)f2;
            }

            joinEntityItemWithWorld(entityitem);

            if (traceItem)
            {
                triggerAchievement(StatList.dropStat);
            }

            return entityitem;
        }
    }

    /**
     * Joins the passed in entity item with the world. Args: entityItem
     */
    protected void joinEntityItemWithWorld(EntityItem itemIn)
    {
        worldObj.spawnEntityInWorld(itemIn);
    }

    /**
     * Block hardness will be further counted in net/minecraft/block/Block.getPlayerRelativeBlockHardness
     */
    public float getToolDigEfficiency(Block p_180471_1_)
    {
        float f = inventory.getStrVsBlock(p_180471_1_);

        if (f > 1.0F)
        {
            int i = EnchantmentHelper.getEfficiencyModifier(this);
            ItemStack itemstack = inventory.getCurrentItem();

            if (i > 0 && itemstack != null)
            {
                f += (float)(i * i + 1);
            }
        }

        if (isPotionActive(Potion.digSpeed))
        {
            f *= 1.0F + (float)(getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
        }

        if (isPotionActive(Potion.digSlowdown))
        {
            float f1 = 1.0F;

            switch (getActivePotionEffect(Potion.digSlowdown).getAmplifier())
            {
                case 0:
                    f1 = 0.3F;
                    break;

                case 1:
                    f1 = 0.09F;
                    break;

                case 2:
                    f1 = 0.0027F;
                    break;

                case 3:
                default:
                    f1 = 8.1E-4F;
            }

            f *= f1;
        }

        if (isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(this))
        {
            f /= 5.0F;
        }

        if (!onGround)
        {
            f /= 5.0F;
        }

        return f;
    }

    /**
     * Checks if the player has the ability to harvest a block (checks current inventory item for a tool if necessary)
     */
    public boolean canHarvestBlock(Block blockToHarvest)
    {
        return inventory.canHeldItemHarvest(blockToHarvest);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        entityUniqueID = EntityPlayer.getUUID(gameProfile);
        NBTTagList nbttaglist = tagCompund.getTagList("Inventory", 10);
        inventory.readFromNBT(nbttaglist);
        inventory.currentItem = tagCompund.getInteger("SelectedItemSlot");
        sleeping = tagCompund.getBoolean("Sleeping");
        sleepTimer = tagCompund.getShort("SleepTimer");
        experience = tagCompund.getFloat("XpP");
        experienceLevel = tagCompund.getInteger("XpLevel");
        experienceTotal = tagCompund.getInteger("XpTotal");
        xpSeed = tagCompund.getInteger("XpSeed");

        if (xpSeed == 0)
        {
            xpSeed = rand.nextInt();
        }

        setScore(tagCompund.getInteger("Score"));

        if (sleeping)
        {
            playerLocation = new BlockPos(this);
            wakeUpPlayer(true, true, false);
        }

        if (tagCompund.hasKey("SpawnX", 99) && tagCompund.hasKey("SpawnY", 99) && tagCompund.hasKey("SpawnZ", 99))
        {
            spawnChunk = new BlockPos(tagCompund.getInteger("SpawnX"), tagCompund.getInteger("SpawnY"), tagCompund.getInteger("SpawnZ"));
            spawnForced = tagCompund.getBoolean("SpawnForced");
        }

        foodStats.readNBT(tagCompund);
        capabilities.readCapabilitiesFromNBT(tagCompund);

        if (tagCompund.hasKey("EnderItems", 9))
        {
            NBTTagList nbttaglist1 = tagCompund.getTagList("EnderItems", 10);
            theInventoryEnderChest.loadInventoryFromNBT(nbttaglist1);
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setTag("Inventory", inventory.writeToNBT(new NBTTagList()));
        tagCompound.setInteger("SelectedItemSlot", inventory.currentItem);
        tagCompound.setBoolean("Sleeping", sleeping);
        tagCompound.setShort("SleepTimer", (short) sleepTimer);
        tagCompound.setFloat("XpP", experience);
        tagCompound.setInteger("XpLevel", experienceLevel);
        tagCompound.setInteger("XpTotal", experienceTotal);
        tagCompound.setInteger("XpSeed", xpSeed);
        tagCompound.setInteger("Score", getScore());

        if (spawnChunk != null)
        {
            tagCompound.setInteger("SpawnX", spawnChunk.getX());
            tagCompound.setInteger("SpawnY", spawnChunk.getY());
            tagCompound.setInteger("SpawnZ", spawnChunk.getZ());
            tagCompound.setBoolean("SpawnForced", spawnForced);
        }

        foodStats.writeNBT(tagCompound);
        capabilities.writeCapabilitiesToNBT(tagCompound);
        tagCompound.setTag("EnderItems", theInventoryEnderChest.saveInventoryToNBT());
        ItemStack itemstack = inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() != null)
        {
            tagCompound.setTag("SelectedItem", itemstack.writeToNBT(new NBTTagCompound()));
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
        else if (capabilities.disableDamage && !source.canHarmInCreative())
        {
            return false;
        }
        else
        {
            entityAge = 0;

            if (getHealth() <= 0.0F)
            {
                return false;
            }
            else
            {
                if (isPlayerSleeping() && !worldObj.isRemote)
                {
                    wakeUpPlayer(true, true, false);
                }

                if (source.isDifficultyScaled())
                {
                    if (worldObj.getDifficulty() == EnumDifficulty.PEACEFUL)
                    {
                        amount = 0.0F;
                    }

                    if (worldObj.getDifficulty() == EnumDifficulty.EASY)
                    {
                        amount = amount / 2.0F + 1.0F;
                    }

                    if (worldObj.getDifficulty() == EnumDifficulty.HARD)
                    {
                        amount = amount * 3.0F / 2.0F;
                    }
                }

                if (amount == 0.0F)
                {
                    return false;
                }
                else
                {
                    Entity entity = source.getEntity();

                    if (entity instanceof EntityArrow && ((EntityArrow)entity).shootingEntity != null)
                    {
                        entity = ((EntityArrow)entity).shootingEntity;
                    }

                    return super.attackEntityFrom(source, amount);
                }
            }
        }
    }

    public boolean canAttackPlayer(EntityPlayer other)
    {
        Team team = getTeam();
        Team team1 = other.getTeam();
        return team == null || (!team.isSameTeam(team1) || team.getAllowFriendlyFire());
    }

    protected void damageArmor(float p_70675_1_)
    {
        inventory.damageArmor(p_70675_1_);
    }

    /**
     * Returns the current armor value as determined by a call to InventoryPlayer.getTotalArmorValue
     */
    public int getTotalArmorValue()
    {
        return inventory.getTotalArmorValue();
    }

    /**
     * When searching for vulnerable players, if a player is invisible, the return value of this is the chance of seeing
     * them anyway.
     */
    public float getArmorVisibility()
    {
        int i = 0;

        for (ItemStack itemstack : inventory.armorInventory)
        {
            if (itemstack != null)
            {
                ++i;
            }
        }

        return (float)i / (float) inventory.armorInventory.length;
    }

    /**
     * Deals damage to the entity. If its a EntityPlayer then will take damage from the armor first and then health
     * second with the reduced value. Args: damageAmount
     */
    protected void damageEntity(DamageSource damageSrc, float damageAmount)
    {
        if (!isEntityInvulnerable(damageSrc))
        {
            if (!damageSrc.isUnblockable() && isBlocking() && damageAmount > 0.0F)
            {
                damageAmount = (1.0F + damageAmount) * 0.5F;
            }

            damageAmount = applyArmorCalculations(damageSrc, damageAmount);
            damageAmount = applyPotionDamageCalculations(damageSrc, damageAmount);
            float f = damageAmount;
            damageAmount = Math.max(damageAmount - getAbsorptionAmount(), 0.0F);
            setAbsorptionAmount(getAbsorptionAmount() - (f - damageAmount));

            if (damageAmount != 0.0F)
            {
                addExhaustion(damageSrc.getHungerDamage());
                float f1 = getHealth();
                setHealth(getHealth() - damageAmount);
                getCombatTracker().trackDamage(damageSrc, f1, damageAmount);

                if (damageAmount < 3.4028235E37F)
                {
                    addStat(StatList.damageTakenStat, Math.round(damageAmount * 10.0F));
                }
            }
        }
    }

    public void openEditSign(TileEntitySign signTile)
    {
    }

    public void openEditCommandBlock(CommandBlockLogic cmdBlockLogic)
    {
    }

    public void displayVillagerTradeGui(IMerchant villager)
    {
    }

    /**
     * Displays the GUI for interacting with a chest inventory. Args: chestInventory
     */
    public void displayGUIChest(IInventory chestInventory)
    {
    }

    public void displayGUIHorse(EntityHorse horse, IInventory horseInventory)
    {
    }

    public void displayGui(IInteractionObject guiOwner)
    {
    }

    /**
     * Displays the GUI for interacting with a book.
     */
    public void displayGUIBook(ItemStack bookStack)
    {
    }

    public boolean interactWith(Entity p_70998_1_)
    {
        if (isSpectator())
        {
            if (p_70998_1_ instanceof IInventory)
            {
                displayGUIChest((IInventory)p_70998_1_);
            }

            return false;
        }
        else
        {
            ItemStack itemstack = getCurrentEquippedItem();
            ItemStack itemstack1 = itemstack != null ? itemstack.copy() : null;

            if (!p_70998_1_.interactFirst(this))
            {
                if (itemstack != null && p_70998_1_ instanceof EntityLivingBase)
                {
                    if (capabilities.isCreativeMode)
                    {
                        itemstack = itemstack1;
                    }

                    if (itemstack.interactWithEntity(this, (EntityLivingBase)p_70998_1_))
                    {
                        if (itemstack.stackSize <= 0 && !capabilities.isCreativeMode)
                        {
                            destroyCurrentEquippedItem();
                        }

                        return true;
                    }
                }

                return false;
            }
            else
            {
                if (itemstack != null && itemstack == getCurrentEquippedItem())
                {
                    if (itemstack.stackSize <= 0 && !capabilities.isCreativeMode)
                    {
                        destroyCurrentEquippedItem();
                    }
                    else if (itemstack.stackSize < itemstack1.stackSize && capabilities.isCreativeMode)
                    {
                        itemstack.stackSize = itemstack1.stackSize;
                    }
                }

                return true;
            }
        }
    }

    /**
     * Returns the currently being used item by the player.
     */
    public ItemStack getCurrentEquippedItem()
    {
        return inventory.getCurrentItem();
    }

    /**
     * Destroys the currently equipped item from the player's inventory.
     */
    public void destroyCurrentEquippedItem()
    {
        inventory.setInventorySlotContents(inventory.currentItem, null);
    }

    /**
     * Returns the Y Offset of this entity.
     */
    public double getYOffset()
    {
        return -0.35D;
    }

    /**
     * Attacks for the player the targeted entity with the currently equipped item.  The equipped item has hitEntity
     * called on it. Args: targetEntity
     */
    public void attackTargetEntityWithCurrentItem(Entity targetEntity)
    {
        if (targetEntity.canAttackWithItem())
        {
            if (!targetEntity.hitByEntity(this))
            {
                float f = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
                int i = 0;
                float f1 = 0.0F;

                if (targetEntity instanceof EntityLivingBase)
                {
                    f1 = EnchantmentHelper.func_152377_a(getHeldItem(), ((EntityLivingBase)targetEntity).getCreatureAttribute());
                }
                else
                {
                    f1 = EnchantmentHelper.func_152377_a(getHeldItem(), EnumCreatureAttribute.UNDEFINED);
                }

                i = i + EnchantmentHelper.getKnockbackModifier(this);

                if (isSprinting())
                {
                    ++i;
                }

                if (f > 0.0F || f1 > 0.0F)
                {
                    boolean flag = fallDistance > 0.0F && !onGround && !isOnLadder() && !isInWater() && !isPotionActive(Potion.blindness) && ridingEntity == null && targetEntity instanceof EntityLivingBase;

                    if (flag && f > 0.0F)
                    {
                        f *= 1.5F;
                    }

                    f = f + f1;
                    boolean flag1 = false;
                    int j = EnchantmentHelper.getFireAspectModifier(this);

                    if (targetEntity instanceof EntityLivingBase && j > 0 && !targetEntity.isBurning())
                    {
                        flag1 = true;
                        targetEntity.setFire(1);
                    }

                    double d0 = targetEntity.motionX;
                    double d1 = targetEntity.motionY;
                    double d2 = targetEntity.motionZ;
                    boolean flag2 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(this), f);

                    if (flag2)
                    {
                        if (i > 0)
                        {
                            targetEntity.addVelocity(-MathHelper.sin(rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F, 0.1D, MathHelper.cos(rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F);
                            motionX *= 0.6D;
                            motionZ *= 0.6D;
                            setSprinting(false);
                        }

                        if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged)
                        {
                            ((EntityPlayerMP)targetEntity).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = d0;
                            targetEntity.motionY = d1;
                            targetEntity.motionZ = d2;
                        }

                        if (flag)
                        {
                            onCriticalHit(targetEntity);
                        }

                        if (f1 > 0.0F)
                        {
                            onEnchantmentCritical(targetEntity);
                        }

                        if (f >= 18.0F)
                        {
                            triggerAchievement(AchievementList.overkill);
                        }

                        setLastAttacker(targetEntity);

                        if (targetEntity instanceof EntityLivingBase)
                        {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase)targetEntity, this);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(this, targetEntity);
                        ItemStack itemstack = getCurrentEquippedItem();
                        Entity entity = targetEntity;

                        if (targetEntity instanceof EntityDragonPart)
                        {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart)targetEntity).entityDragonObj;

                            if (ientitymultipart instanceof EntityLivingBase)
                            {
                                entity = (EntityLivingBase)ientitymultipart;
                            }
                        }

                        if (itemstack != null && entity instanceof EntityLivingBase)
                        {
                            itemstack.hitEntity((EntityLivingBase)entity, this);

                            if (itemstack.stackSize <= 0)
                            {
                                destroyCurrentEquippedItem();
                            }
                        }

                        if (targetEntity instanceof EntityLivingBase)
                        {
                            addStat(StatList.damageDealtStat, Math.round(f * 10.0F));

                            if (j > 0)
                            {
                                targetEntity.setFire(j * 4);
                            }
                        }

                        addExhaustion(0.3F);
                    }
                    else if (flag1)
                    {
                        targetEntity.extinguish();
                    }
                }
            }
        }
    }

    /**
     * Called when the player performs a critical hit on the Entity. Args: entity that was hit critically
     */
    public void onCriticalHit(Entity entityHit)
    {
    }

    public void onEnchantmentCritical(Entity entityHit)
    {
    }

    public void respawnPlayer()
    {
    }

    /**
     * Will get destroyed next tick.
     */
    public void setDead()
    {
        super.setDead();
        inventoryContainer.onContainerClosed(this);

        if (openContainer != null)
        {
            openContainer.onContainerClosed(this);
        }
    }

    /**
     * Checks if this entity is inside of an opaque block
     */
    public boolean isEntityInsideOpaqueBlock()
    {
        return !sleeping && super.isEntityInsideOpaqueBlock();
    }

    /**
     * returns true if this is an EntityPlayerSP, or the logged in player.
     */
    public boolean isUser()
    {
        return false;
    }

    /**
     * Returns the GameProfile for this player
     */
    public GameProfile getGameProfile()
    {
        return gameProfile;
    }

    public EntityPlayer.EnumStatus trySleep(BlockPos bedLocation)
    {
        if (!worldObj.isRemote)
        {
            if (isPlayerSleeping() || !isEntityAlive())
            {
                return EntityPlayer.EnumStatus.OTHER_PROBLEM;
            }

            if (!worldObj.provider.isSurfaceWorld())
            {
                return EntityPlayer.EnumStatus.NOT_POSSIBLE_HERE;
            }

            if (worldObj.isDaytime())
            {
                return EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW;
            }

            if (Math.abs(posX - (double)bedLocation.getX()) > 3.0D || Math.abs(posY - (double)bedLocation.getY()) > 2.0D || Math.abs(posZ - (double)bedLocation.getZ()) > 3.0D)
            {
                return EntityPlayer.EnumStatus.TOO_FAR_AWAY;
            }

            double d0 = 8.0D;
            double d1 = 5.0D;
            List<EntityMob> list = worldObj.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double)bedLocation.getX() - d0, (double)bedLocation.getY() - d1, (double)bedLocation.getZ() - d0, (double)bedLocation.getX() + d0, (double)bedLocation.getY() + d1, (double)bedLocation.getZ() + d0));

            if (!list.isEmpty())
            {
                return EntityPlayer.EnumStatus.NOT_SAFE;
            }
        }

        if (isRiding())
        {
            mountEntity(null);
        }

        setSize(0.2F, 0.2F);

        if (worldObj.isBlockLoaded(bedLocation))
        {
            EnumFacing enumfacing = worldObj.getBlockState(bedLocation).getValue(BlockDirectional.FACING);
            float f = 0.5F;
            float f1 = 0.5F;

            switch (enumfacing)
            {
                case SOUTH:
                    f1 = 0.9F;
                    break;

                case NORTH:
                    f1 = 0.1F;
                    break;

                case WEST:
                    f = 0.1F;
                    break;

                case EAST:
                    f = 0.9F;
            }

            func_175139_a(enumfacing);
            setPosition((float)bedLocation.getX() + f, (float)bedLocation.getY() + 0.6875F, (float)bedLocation.getZ() + f1);
        }
        else
        {
            setPosition((float)bedLocation.getX() + 0.5F, (float)bedLocation.getY() + 0.6875F, (float)bedLocation.getZ() + 0.5F);
        }

        sleeping = true;
        sleepTimer = 0;
        playerLocation = bedLocation;
        motionX = motionZ = motionY = 0.0D;

        if (!worldObj.isRemote)
        {
            worldObj.updateAllPlayersSleepingFlag();
        }

        return EntityPlayer.EnumStatus.OK;
    }

    private void func_175139_a(EnumFacing p_175139_1_)
    {
        renderOffsetX = 0.0F;
        renderOffsetZ = 0.0F;

        switch (p_175139_1_)
        {
            case SOUTH:
                renderOffsetZ = -1.8F;
                break;

            case NORTH:
                renderOffsetZ = 1.8F;
                break;

            case WEST:
                renderOffsetX = 1.8F;
                break;

            case EAST:
                renderOffsetX = -1.8F;
        }
    }

    /**
     * Wake up the player if they're sleeping.
     */
    public void wakeUpPlayer(boolean p_70999_1_, boolean updateWorldFlag, boolean setSpawn)
    {
        setSize(0.6F, 1.8F);
        IBlockState iblockstate = worldObj.getBlockState(playerLocation);

        if (playerLocation != null && iblockstate.getBlock() == Blocks.bed)
        {
            worldObj.setBlockState(playerLocation, iblockstate.withProperty(BlockBed.OCCUPIED, Boolean.valueOf(false)), 4);
            BlockPos blockpos = BlockBed.getSafeExitLocation(worldObj, playerLocation, 0);

            if (blockpos == null)
            {
                blockpos = playerLocation.up();
            }

            setPosition((float)blockpos.getX() + 0.5F, (float)blockpos.getY() + 0.1F, (float)blockpos.getZ() + 0.5F);
        }

        sleeping = false;

        if (!worldObj.isRemote && updateWorldFlag)
        {
            worldObj.updateAllPlayersSleepingFlag();
        }

        sleepTimer = p_70999_1_ ? 0 : 100;

        if (setSpawn)
        {
            setSpawnPoint(playerLocation, false);
        }
    }

    private boolean isInBed()
    {
        return worldObj.getBlockState(playerLocation).getBlock() == Blocks.bed;
    }

    /**
     * Return null if bed is invalid
     */
    public static BlockPos getBedSpawnLocation(World worldIn, BlockPos bedLocation, boolean forceSpawn)
    {
        Block block = worldIn.getBlockState(bedLocation).getBlock();

        if (block != Blocks.bed)
        {
            if (!forceSpawn)
            {
                return null;
            }
            else
            {
                boolean flag = block.func_181623_g();
                boolean flag1 = worldIn.getBlockState(bedLocation.up()).getBlock().func_181623_g();
                return flag && flag1 ? bedLocation : null;
            }
        }
        else
        {
            return BlockBed.getSafeExitLocation(worldIn, bedLocation, 0);
        }
    }

    /**
     * Returns the orientation of the bed in degrees.
     */
    public float getBedOrientationInDegrees()
    {
        if (playerLocation != null)
        {
            EnumFacing enumfacing = worldObj.getBlockState(playerLocation).getValue(BlockDirectional.FACING);

            switch (enumfacing)
            {
                case SOUTH:
                    return 90.0F;

                case NORTH:
                    return 270.0F;

                case WEST:
                    return 0.0F;

                case EAST:
                    return 180.0F;
            }
        }

        return 0.0F;
    }

    /**
     * Returns whether player is sleeping or not
     */
    public boolean isPlayerSleeping()
    {
        return sleeping;
    }

    /**
     * Returns whether or not the player is asleep and the screen has fully faded.
     */
    public boolean isPlayerFullyAsleep()
    {
        return sleeping && sleepTimer >= 100;
    }

    public int getSleepTimer()
    {
        return sleepTimer;
    }

    public void addChatComponentMessage(IChatComponent chatComponent)
    {
    }

    public BlockPos getBedLocation()
    {
        return spawnChunk;
    }

    public boolean isSpawnForced()
    {
        return spawnForced;
    }

    public void setSpawnPoint(BlockPos pos, boolean forced)
    {
        if (pos != null)
        {
            spawnChunk = pos;
            spawnForced = forced;
        }
        else
        {
            spawnChunk = null;
            spawnForced = false;
        }
    }

    /**
     * Will trigger the specified trigger.
     */
    public void triggerAchievement(StatBase achievementIn)
    {
        addStat(achievementIn, 1);
    }

    /**
     * Adds a value to a statistic field.
     */
    public void addStat(StatBase stat, int amount)
    {
    }

    public void func_175145_a(StatBase p_175145_1_)
    {
    }

    /**
     * Causes this entity to do an upwards motion (jumping).
     */
    public void jump()
    {
        super.jump();
        triggerAchievement(StatList.jumpStat);

        if (isSprinting())
        {
            addExhaustion(0.8F);
        }
        else
        {
            addExhaustion(0.2F);
        }
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    public void moveEntityWithHeading(float strafe, float forward)
    {
        double d0 = posX;
        double d1 = posY;
        double d2 = posZ;

        if (capabilities.isFlying && ridingEntity == null)
        {
            double d3 = motionY;
            float f = jumpMovementFactor;
            jumpMovementFactor = capabilities.getFlySpeed() * (float)(isSprinting() ? 2 : 1);
            super.moveEntityWithHeading(strafe, forward);
            motionY = d3 * 0.6D;
            jumpMovementFactor = f;
        }
        else
        {
            super.moveEntityWithHeading(strafe, forward);
        }

        addMovementStat(posX - d0, posY - d1, posZ - d2);
    }

    /**
     * the movespeed used for the new AI system
     */
    public float getAIMoveSpeed()
    {
        return (float) getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
    }

    /**
     * Adds a value to a movement statistic field - like run, walk, swin or climb.
     */
    public void addMovementStat(double p_71000_1_, double p_71000_3_, double p_71000_5_)
    {
        if (ridingEntity == null)
        {
            if (isInsideOfMaterial(Material.water))
            {
                int i = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_3_ * p_71000_3_ + p_71000_5_ * p_71000_5_) * 100.0F);

                if (i > 0)
                {
                    addStat(StatList.distanceDoveStat, i);
                    addExhaustion(0.015F * (float)i * 0.01F);
                }
            }
            else if (isInWater())
            {
                int j = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);

                if (j > 0)
                {
                    addStat(StatList.distanceSwumStat, j);
                    addExhaustion(0.015F * (float)j * 0.01F);
                }
            }
            else if (isOnLadder())
            {
                if (p_71000_3_ > 0.0D)
                {
                    addStat(StatList.distanceClimbedStat, (int)Math.round(p_71000_3_ * 100.0D));
                }
            }
            else if (onGround)
            {
                int k = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);

                if (k > 0)
                {
                    addStat(StatList.distanceWalkedStat, k);

                    if (isSprinting())
                    {
                        addStat(StatList.distanceSprintedStat, k);
                        addExhaustion(0.099999994F * (float)k * 0.01F);
                    }
                    else
                    {
                        if (isSneaking())
                        {
                            addStat(StatList.distanceCrouchedStat, k);
                        }

                        addExhaustion(0.01F * (float)k * 0.01F);
                    }
                }
            }
            else
            {
                int l = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0F);

                if (l > 25)
                {
                    addStat(StatList.distanceFlownStat, l);
                }
            }
        }
    }

    /**
     * Adds a value to a mounted movement statistic field - by minecart, boat, or pig.
     */
    private void addMountedMovementStat(double p_71015_1_, double p_71015_3_, double p_71015_5_)
    {
        if (ridingEntity != null)
        {
            int i = Math.round(MathHelper.sqrt_double(p_71015_1_ * p_71015_1_ + p_71015_3_ * p_71015_3_ + p_71015_5_ * p_71015_5_) * 100.0F);

            if (i > 0)
            {
                if (ridingEntity instanceof EntityMinecart)
                {
                    addStat(StatList.distanceByMinecartStat, i);

                    if (startMinecartRidingCoordinate == null)
                    {
                        startMinecartRidingCoordinate = new BlockPos(this);
                    }
                    else if (startMinecartRidingCoordinate.distanceSq(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)) >= 1000000.0D)
                    {
                        triggerAchievement(AchievementList.onARail);
                    }
                }
                else if (ridingEntity instanceof EntityBoat)
                {
                    addStat(StatList.distanceByBoatStat, i);
                }
                else if (ridingEntity instanceof EntityPig)
                {
                    addStat(StatList.distanceByPigStat, i);
                }
                else if (ridingEntity instanceof EntityHorse)
                {
                    addStat(StatList.distanceByHorseStat, i);
                }
            }
        }
    }

    public void fall(float distance, float damageMultiplier)
    {
        if (!capabilities.allowFlying)
        {
            if (distance >= 2.0F)
            {
                addStat(StatList.distanceFallenStat, (int)Math.round((double)distance * 100.0D));
            }

            super.fall(distance, damageMultiplier);
        }
    }

    /**
     * sets the players height back to normal after doing things like sleeping and dieing
     */
    protected void resetHeight()
    {
        if (!isSpectator())
        {
            super.resetHeight();
        }
    }

    protected String getFallSoundString(int damageValue)
    {
        return damageValue > 4 ? "game.player.hurt.fall.big" : "game.player.hurt.fall.small";
    }

    /**
     * This method gets called when the entity kills another one.
     */
    public void onKillEntity(EntityLivingBase entityLivingIn)
    {
        if (entityLivingIn instanceof IMob)
        {
            triggerAchievement(AchievementList.killEnemy);
        }

        EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.entityEggs.get(Integer.valueOf(EntityList.getEntityID(entityLivingIn)));

        if (entitylist$entityegginfo != null)
        {
            triggerAchievement(entitylist$entityegginfo.field_151512_d);
        }
    }

    /**
     * Sets the Entity inside a web block.
     */
    public void setInWeb()
    {
        if (!capabilities.isFlying)
        {
            super.setInWeb();
        }
    }

    public ItemStack getCurrentArmor(int slotIn)
    {
        return inventory.armorItemInSlot(slotIn);
    }

    /**
     * Add experience points to player.
     */
    public void addExperience(int amount)
    {
        addScore(amount);
        int i = Integer.MAX_VALUE - experienceTotal;

        if (amount > i)
        {
            amount = i;
        }

        experience += (float)amount / (float) xpBarCap();

        for (experienceTotal += amount; experience >= 1.0F; experience /= (float) xpBarCap())
        {
            experience = (experience - 1.0F) * (float) xpBarCap();
            addExperienceLevel(1);
        }
    }

    public int getXPSeed()
    {
        return xpSeed;
    }

    public void removeExperienceLevel(int levels)
    {
        experienceLevel -= levels;

        if (experienceLevel < 0)
        {
            experienceLevel = 0;
            experience = 0.0F;
            experienceTotal = 0;
        }

        xpSeed = rand.nextInt();
    }

    /**
     * Add experience levels to this player.
     */
    public void addExperienceLevel(int levels)
    {
        experienceLevel += levels;

        if (experienceLevel < 0)
        {
            experienceLevel = 0;
            experience = 0.0F;
            experienceTotal = 0;
        }

        if (levels > 0 && experienceLevel % 5 == 0 && (float) lastXPSound < (float) ticksExisted - 100.0F)
        {
            float f = experienceLevel > 30 ? 1.0F : (float) experienceLevel / 30.0F;
            worldObj.playSoundAtEntity(this, "random.levelup", f * 0.75F, 1.0F);
            lastXPSound = ticksExisted;
        }
    }

    /**
     * This method returns the cap amount of experience that the experience bar can hold. With each level, the
     * experience cap on the player's experience bar is raised by 10.
     */
    public int xpBarCap()
    {
        return experienceLevel >= 30 ? 112 + (experienceLevel - 30) * 9 : (experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2);
    }

    /**
     * increases exhaustion level by supplied amount
     */
    public void addExhaustion(float p_71020_1_)
    {
        if (!capabilities.disableDamage)
        {
            if (!worldObj.isRemote)
            {
                foodStats.addExhaustion(p_71020_1_);
            }
        }
    }

    /**
     * Returns the player's FoodStats object.
     */
    public FoodStats getFoodStats()
    {
        return foodStats;
    }

    public boolean canEat(boolean ignoreHunger)
    {
        return (ignoreHunger || foodStats.needFood()) && !capabilities.disableDamage;
    }

    /**
     * Checks if the player's health is not full and not zero.
     */
    public boolean shouldHeal()
    {
        return getHealth() > 0.0F && getHealth() < getMaxHealth();
    }

    /**
     * sets the itemInUse when the use item button is clicked. Args: itemstack, int maxItemUseDuration
     */
    public void setItemInUse(ItemStack stack, int duration)
    {
        if (stack != itemInUse)
        {
            itemInUse = stack;
            itemInUseCount = duration;

            if (!worldObj.isRemote)
            {
                setEating(true);
            }
        }
    }

    public boolean isAllowEdit()
    {
        return capabilities.allowEdit;
    }

    public boolean canPlayerEdit(BlockPos p_175151_1_, EnumFacing p_175151_2_, ItemStack p_175151_3_)
    {
        if (capabilities.allowEdit)
        {
            return true;
        }
        else if (p_175151_3_ == null)
        {
            return false;
        }
        else
        {
            BlockPos blockpos = p_175151_1_.offset(p_175151_2_.getOpposite());
            Block block = worldObj.getBlockState(blockpos).getBlock();
            return p_175151_3_.canPlaceOn(block) || p_175151_3_.canEditBlocks();
        }
    }

    /**
     * Get the experience points the entity currently has.
     */
    protected int getExperiencePoints(EntityPlayer player)
    {
        if (worldObj.getGameRules().getBoolean("keepInventory"))
        {
            return 0;
        }
        else
        {
            int i = experienceLevel * 7;
            return i > 100 ? 100 : i;
        }
    }

    /**
     * Only use is to identify if class is an instance of player for experience dropping
     */
    protected boolean isPlayer()
    {
        return true;
    }

    public boolean getAlwaysRenderNameTagForRender()
    {
        return true;
    }

    /**
     * Copies the values from the given player into this player if boolean par2 is true. Always clones Ender Chest
     * Inventory.
     */
    public void clonePlayer(EntityPlayer oldPlayer, boolean respawnFromEnd)
    {
        if (respawnFromEnd)
        {
            inventory.copyInventory(oldPlayer.inventory);
            setHealth(oldPlayer.getHealth());
            foodStats = oldPlayer.foodStats;
            experienceLevel = oldPlayer.experienceLevel;
            experienceTotal = oldPlayer.experienceTotal;
            experience = oldPlayer.experience;
            setScore(oldPlayer.getScore());
            field_181016_an = oldPlayer.field_181016_an;
            field_181017_ao = oldPlayer.field_181017_ao;
            field_181018_ap = oldPlayer.field_181018_ap;
        }
        else if (worldObj.getGameRules().getBoolean("keepInventory"))
        {
            inventory.copyInventory(oldPlayer.inventory);
            experienceLevel = oldPlayer.experienceLevel;
            experienceTotal = oldPlayer.experienceTotal;
            experience = oldPlayer.experience;
            setScore(oldPlayer.getScore());
        }

        xpSeed = oldPlayer.xpSeed;
        theInventoryEnderChest = oldPlayer.theInventoryEnderChest;
        getDataWatcher().updateObject(10, Byte.valueOf(oldPlayer.getDataWatcher().getWatchableObjectByte(10)));
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return !capabilities.isFlying;
    }

    /**
     * Sends the player's abilities to the server (if there is one).
     */
    public void sendPlayerAbilities()
    {
    }

    /**
     * Sets the player's game mode and sends it to them.
     */
    public void setGameType(WorldSettings.GameType gameType)
    {
    }

    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
    public String getName()
    {
        return gameProfile.getName();
    }

    /**
     * Returns the InventoryEnderChest of this player.
     */
    public InventoryEnderChest getInventoryEnderChest()
    {
        return theInventoryEnderChest;
    }

    /**
     * 0: Tool in Hand; 1-4: Armor
     */
    public ItemStack getEquipmentInSlot(int slotIn)
    {
        return slotIn == 0 ? inventory.getCurrentItem() : inventory.armorInventory[slotIn - 1];
    }

    /**
     * Returns the item that this EntityLiving is holding, if any.
     */
    public ItemStack getHeldItem()
    {
        return inventory.getCurrentItem();
    }

    /**
     * Sets the held item, or an armor slot. Slot 0 is held item. Slot 1-4 is armor. Params: Item, slot
     */
    public void setCurrentItemOrArmor(int slotIn, ItemStack stack)
    {
        inventory.armorInventory[slotIn] = stack;
    }

    /**
     * Only used by renderer in EntityLivingBase subclasses.
     * Determines if an entity is visible or not to a specfic player, if the entity is normally invisible.
     * For EntityLivingBase subclasses, returning false when invisible will render the entity semitransparent.
     */
    public boolean isInvisibleToPlayer(EntityPlayer player)
    {
        if (!isInvisible())
        {
            return false;
        }
        else if (player.isSpectator())
        {
            return false;
        }
        else
        {
            Team team = getTeam();
            return team == null || player == null || player.getTeam() != team || !team.getSeeFriendlyInvisiblesEnabled();
        }
    }

    /**
     * Returns true if the player is in spectator mode.
     */
    public abstract boolean isSpectator();

    /**
     * returns the inventory of this entity (only used in EntityPlayerMP it seems)
     */
    public ItemStack[] getInventory()
    {
        return inventory.armorInventory;
    }

    public boolean isPushedByWater()
    {
        return !capabilities.isFlying;
    }

    public Scoreboard getWorldScoreboard()
    {
        return worldObj.getScoreboard();
    }

    public Team getTeam()
    {
        return getWorldScoreboard().getPlayersTeam(getName());
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public IChatComponent getDisplayName()
    {
        IChatComponent ichatcomponent = new ChatComponentText(ScorePlayerTeam.formatPlayerName(getTeam(), getName()));
        ichatcomponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + getName() + " "));
        ichatcomponent.getChatStyle().setChatHoverEvent(getHoverEvent());
        ichatcomponent.getChatStyle().setInsertion(getName());
        return ichatcomponent;
    }

    public float getEyeHeight()
    {
        float f = 1.62F;

        if (isPlayerSleeping())
        {
            f = 0.2F;
        }

        if (isSneaking())
        {
            f -= 0.08F;
        }

        return f;
    }

    public void setAbsorptionAmount(float amount)
    {
        if (amount < 0.0F)
        {
            amount = 0.0F;
        }

        getDataWatcher().updateObject(17, Float.valueOf(amount));
    }

    public float getAbsorptionAmount()
    {
        return getDataWatcher().getWatchableObjectFloat(17);
    }

    /**
     * Gets a players UUID given their GameProfie
     */
    public static UUID getUUID(GameProfile profile)
    {
        UUID uuid = profile.getId();

        if (uuid == null)
        {
            uuid = EntityPlayer.getOfflineUUID(profile.getName());
        }

        return uuid;
    }

    public static UUID getOfflineUUID(String username)
    {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
    }

    /**
     * Check whether this player can open an inventory locked with the given LockCode.
     */
    public boolean canOpen(LockCode code)
    {
        if (code.isEmpty())
        {
            return true;
        }
        else
        {
            ItemStack itemstack = getCurrentEquippedItem();
            return itemstack != null && itemstack.hasDisplayName() && itemstack.getDisplayName().equals(code.getLock());
        }
    }

    public boolean isWearing(EnumPlayerModelParts p_175148_1_)
    {
        return (getDataWatcher().getWatchableObjectByte(10) & p_175148_1_.getPartMask()) == p_175148_1_.getPartMask();
    }

    /**
     * Returns true if the command sender should be sent feedback about executed commands
     */
    public boolean sendCommandFeedback()
    {
        return MinecraftServer.getServer().worldServers[0].getGameRules().getBoolean("sendCommandFeedback");
    }

    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn)
    {
        if (inventorySlot >= 0 && inventorySlot < inventory.mainInventory.length)
        {
            inventory.setInventorySlotContents(inventorySlot, itemStackIn);
            return true;
        }
        else
        {
            int i = inventorySlot - 100;

            if (i >= 0 && i < inventory.armorInventory.length)
            {
                int k = i + 1;

                if (itemStackIn != null && itemStackIn.getItem() != null)
                {
                    if (itemStackIn.getItem() instanceof ItemArmor)
                    {
                        if (EntityLiving.getArmorPosition(itemStackIn) != k)
                        {
                            return false;
                        }
                    }
                    else if (k != 4 || itemStackIn.getItem() != Items.skull && !(itemStackIn.getItem() instanceof ItemBlock))
                    {
                        return false;
                    }
                }

                inventory.setInventorySlotContents(i + inventory.mainInventory.length, itemStackIn);
                return true;
            }
            else
            {
                int j = inventorySlot - 200;

                if (j >= 0 && j < theInventoryEnderChest.getSizeInventory())
                {
                    theInventoryEnderChest.setInventorySlotContents(j, itemStackIn);
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
    }

    /**
     * Whether the "reducedDebugInfo" option is active for this player.
     */
    public boolean hasReducedDebug()
    {
        return hasReducedDebug;
    }

    public void setReducedDebug(boolean reducedDebug)
    {
        hasReducedDebug = reducedDebug;
    }

    public static enum EnumChatVisibility
    {
        FULL(0, "options.chat.visibility.full"),
        SYSTEM(1, "options.chat.visibility.system"),
        HIDDEN(2, "options.chat.visibility.hidden");

        private static final EntityPlayer.EnumChatVisibility[] ID_LOOKUP = new EntityPlayer.EnumChatVisibility[EnumChatVisibility.values().length];
        private final int chatVisibility;
        private final String resourceKey;

        private EnumChatVisibility(int id, String resourceKey)
        {
            chatVisibility = id;
            this.resourceKey = resourceKey;
        }

        public int getChatVisibility()
        {
            return chatVisibility;
        }

        public static EntityPlayer.EnumChatVisibility getEnumChatVisibility(int id)
        {
            return EnumChatVisibility.ID_LOOKUP[id % EnumChatVisibility.ID_LOOKUP.length];
        }

        public String getResourceKey()
        {
            return resourceKey;
        }

        static {
            for (EntityPlayer.EnumChatVisibility entityplayer$enumchatvisibility : EnumChatVisibility.values())
            {
                EnumChatVisibility.ID_LOOKUP[entityplayer$enumchatvisibility.chatVisibility] = entityplayer$enumchatvisibility;
            }
        }
    }

    public static enum EnumStatus
    {
        OK,
        NOT_POSSIBLE_HERE,
        NOT_POSSIBLE_NOW,
        TOO_FAR_AWAY,
        OTHER_PROBLEM,
        NOT_SAFE
    }
}
