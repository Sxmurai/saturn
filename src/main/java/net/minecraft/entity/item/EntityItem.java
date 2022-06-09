package net.minecraft.entity.item;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityItem extends Entity
{
    private static final Logger logger = LogManager.getLogger();

    /**
     * The age of this EntityItem (used to animate it up and down as well as expire it)
     */
    private int age;
    private int delayBeforeCanPickup;

    /** The health of this EntityItem. (For example, damage for tools) */
    private int health;
    private String thrower;
    private String owner;

    /** The EntityItem's random initial float height. */
    public float hoverStart;

    public EntityItem(World worldIn, double x, double y, double z)
    {
        super(worldIn);
        health = 5;
        hoverStart = (float)(Math.random() * Math.PI * 2.0D);
        setSize(0.25F, 0.25F);
        setPosition(x, y, z);
        rotationYaw = (float)(Math.random() * 360.0D);
        motionX = (float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D);
        motionY = 0.20000000298023224D;
        motionZ = (float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D);
    }

    public EntityItem(World worldIn, double x, double y, double z, ItemStack stack)
    {
        this(worldIn, x, y, z);
        setEntityItemStack(stack);
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    public EntityItem(World worldIn)
    {
        super(worldIn);
        health = 5;
        hoverStart = (float)(Math.random() * Math.PI * 2.0D);
        setSize(0.25F, 0.25F);
        setEntityItemStack(new ItemStack(Blocks.air, 0));
    }

    protected void entityInit()
    {
        getDataWatcher().addObjectByDataType(10, 5);
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        if (getEntityItem() == null)
        {
            setDead();
        }
        else
        {
            super.onUpdate();

            if (delayBeforeCanPickup > 0 && delayBeforeCanPickup != 32767)
            {
                --delayBeforeCanPickup;
            }

            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;
            motionY -= 0.03999999910593033D;
            noClip = pushOutOfBlocks(posX, (getEntityBoundingBox().minY + getEntityBoundingBox().maxY) / 2.0D, posZ);
            moveEntity(motionX, motionY, motionZ);
            boolean flag = (int) prevPosX != (int) posX || (int) prevPosY != (int) posY || (int) prevPosZ != (int) posZ;

            if (flag || ticksExisted % 25 == 0)
            {
                if (worldObj.getBlockState(new BlockPos(this)).getBlock().getMaterial() == Material.lava)
                {
                    motionY = 0.20000000298023224D;
                    motionX = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
                    motionZ = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
                    playSound("random.fizz", 0.4F, 2.0F + rand.nextFloat() * 0.4F);
                }

                if (!worldObj.isRemote)
                {
                    searchForOtherItemsNearby();
                }
            }

            float f = 0.98F;

            if (onGround)
            {
                f = worldObj.getBlockState(new BlockPos(MathHelper.floor_double(posX), MathHelper.floor_double(getEntityBoundingBox().minY) - 1, MathHelper.floor_double(posZ))).getBlock().slipperiness * 0.98F;
            }

            motionX *= f;
            motionY *= 0.9800000190734863D;
            motionZ *= f;

            if (onGround)
            {
                motionY *= -0.5D;
            }

            if (age != -32768)
            {
                ++age;
            }

            handleWaterMovement();

            if (!worldObj.isRemote && age >= 6000)
            {
                setDead();
            }
        }
    }

    /**
     * Looks for other itemstacks nearby and tries to stack them together
     */
    private void searchForOtherItemsNearby()
    {
        for (EntityItem entityitem : worldObj.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().expand(0.5D, 0.0D, 0.5D)))
        {
            combineItems(entityitem);
        }
    }

    /**
     * Tries to merge this item with the item passed as the parameter. Returns true if successful. Either this item or
     * the other item will  be removed from the world.
     */
    private boolean combineItems(EntityItem other)
    {
        if (other == this)
        {
            return false;
        }
        else if (other.isEntityAlive() && isEntityAlive())
        {
            ItemStack itemstack = getEntityItem();
            ItemStack itemstack1 = other.getEntityItem();

            if (delayBeforeCanPickup != 32767 && other.delayBeforeCanPickup != 32767)
            {
                if (age != -32768 && other.age != -32768)
                {
                    if (itemstack1.getItem() != itemstack.getItem())
                    {
                        return false;
                    }
                    else if (itemstack1.hasTagCompound() ^ itemstack.hasTagCompound())
                    {
                        return false;
                    }
                    else if (itemstack1.hasTagCompound() && !itemstack1.getTagCompound().equals(itemstack.getTagCompound()))
                    {
                        return false;
                    }
                    else if (itemstack1.getItem() == null)
                    {
                        return false;
                    }
                    else if (itemstack1.getItem().getHasSubtypes() && itemstack1.getMetadata() != itemstack.getMetadata())
                    {
                        return false;
                    }
                    else if (itemstack1.stackSize < itemstack.stackSize)
                    {
                        return other.combineItems(this);
                    }
                    else if (itemstack1.stackSize + itemstack.stackSize > itemstack1.getMaxStackSize())
                    {
                        return false;
                    }
                    else
                    {
                        itemstack1.stackSize += itemstack.stackSize;
                        other.delayBeforeCanPickup = Math.max(other.delayBeforeCanPickup, delayBeforeCanPickup);
                        other.age = Math.min(other.age, age);
                        other.setEntityItemStack(itemstack1);
                        setDead();
                        return true;
                    }
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * sets the age of the item so that it'll despawn one minute after it has been dropped (instead of five). Used when
     * items are dropped from players in creative mode
     */
    public void setAgeToCreativeDespawnTime()
    {
        age = 4800;
    }

    /**
     * Returns if this entity is in water and will end up adding the waters velocity to the entity
     */
    public boolean handleWaterMovement()
    {
        if (worldObj.handleMaterialAcceleration(getEntityBoundingBox(), Material.water, this))
        {
            if (!inWater && !firstUpdate)
            {
                resetHeight();
            }

            inWater = true;
        }
        else
        {
            inWater = false;
        }

        return inWater;
    }

    /**
     * Will deal the specified amount of damage to the entity if the entity isn't immune to fire damage. Args:
     * amountDamage
     */
    protected void dealFireDamage(int amount)
    {
        attackEntityFrom(DamageSource.inFire, (float)amount);
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
        else if (getEntityItem() != null && getEntityItem().getItem() == Items.nether_star && source.isExplosion())
        {
            return false;
        }
        else
        {
            setBeenAttacked();
            health = (int)((float) health - amount);

            if (health <= 0)
            {
                setDead();
            }

            return false;
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setShort("Health", (byte) health);
        tagCompound.setShort("Age", (short) age);
        tagCompound.setShort("PickupDelay", (short) delayBeforeCanPickup);

        if (getThrower() != null)
        {
            tagCompound.setString("Thrower", thrower);
        }

        if (getOwner() != null)
        {
            tagCompound.setString("Owner", owner);
        }

        if (getEntityItem() != null)
        {
            tagCompound.setTag("Item", getEntityItem().writeToNBT(new NBTTagCompound()));
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        health = tagCompund.getShort("Health") & 255;
        age = tagCompund.getShort("Age");

        if (tagCompund.hasKey("PickupDelay"))
        {
            delayBeforeCanPickup = tagCompund.getShort("PickupDelay");
        }

        if (tagCompund.hasKey("Owner"))
        {
            owner = tagCompund.getString("Owner");
        }

        if (tagCompund.hasKey("Thrower"))
        {
            thrower = tagCompund.getString("Thrower");
        }

        NBTTagCompound nbttagcompound = tagCompund.getCompoundTag("Item");
        setEntityItemStack(ItemStack.loadItemStackFromNBT(nbttagcompound));

        if (getEntityItem() == null)
        {
            setDead();
        }
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    public void onCollideWithPlayer(EntityPlayer entityIn)
    {
        if (!worldObj.isRemote)
        {
            ItemStack itemstack = getEntityItem();
            int i = itemstack.stackSize;

            if (delayBeforeCanPickup == 0 && (owner == null || 6000 - age <= 200 || owner.equals(entityIn.getName())) && entityIn.inventory.addItemStackToInventory(itemstack))
            {
                if (itemstack.getItem() == Item.getItemFromBlock(Blocks.log))
                {
                    entityIn.triggerAchievement(AchievementList.mineWood);
                }

                if (itemstack.getItem() == Item.getItemFromBlock(Blocks.log2))
                {
                    entityIn.triggerAchievement(AchievementList.mineWood);
                }

                if (itemstack.getItem() == Items.leather)
                {
                    entityIn.triggerAchievement(AchievementList.killCow);
                }

                if (itemstack.getItem() == Items.diamond)
                {
                    entityIn.triggerAchievement(AchievementList.diamonds);
                }

                if (itemstack.getItem() == Items.blaze_rod)
                {
                    entityIn.triggerAchievement(AchievementList.blazeRod);
                }

                if (itemstack.getItem() == Items.diamond && getThrower() != null)
                {
                    EntityPlayer entityplayer = worldObj.getPlayerEntityByName(getThrower());

                    if (entityplayer != null && entityplayer != entityIn)
                    {
                        entityplayer.triggerAchievement(AchievementList.diamondsToYou);
                    }
                }

                if (!isSilent())
                {
                    worldObj.playSoundAtEntity(entityIn, "random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }

                entityIn.onItemPickup(this, i);

                if (itemstack.stackSize <= 0)
                {
                    setDead();
                }
            }
        }
    }

    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
    public String getName()
    {
        return hasCustomName() ? getCustomNameTag() : StatCollector.translateToLocal("item." + getEntityItem().getUnlocalizedName());
    }

    /**
     * If returns false, the item will not inflict any damage against entities.
     */
    public boolean canAttackWithItem()
    {
        return false;
    }

    /**
     * Teleports the entity to another dimension. Params: Dimension number to teleport to
     */
    public void travelToDimension(int dimensionId)
    {
        super.travelToDimension(dimensionId);

        if (!worldObj.isRemote)
        {
            searchForOtherItemsNearby();
        }
    }

    /**
     * Returns the ItemStack corresponding to the Entity (Note: if no item exists, will log an error but still return an
     * ItemStack containing Block.stone)
     */
    public ItemStack getEntityItem()
    {
        ItemStack itemstack = getDataWatcher().getWatchableObjectItemStack(10);

        if (itemstack == null)
        {
            if (worldObj != null)
            {
                EntityItem.logger.error("Item entity " + getEntityId() + " has no item?!");
            }

            return new ItemStack(Blocks.stone);
        }
        else
        {
            return itemstack;
        }
    }

    /**
     * Sets the ItemStack for this entity
     */
    public void setEntityItemStack(ItemStack stack)
    {
        getDataWatcher().updateObject(10, stack);
        getDataWatcher().setObjectWatched(10);
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public String getThrower()
    {
        return thrower;
    }

    public void setThrower(String thrower)
    {
        this.thrower = thrower;
    }

    public int getAge()
    {
        return age;
    }

    public void setDefaultPickupDelay()
    {
        delayBeforeCanPickup = 10;
    }

    public void setNoPickupDelay()
    {
        delayBeforeCanPickup = 0;
    }

    public void setInfinitePickupDelay()
    {
        delayBeforeCanPickup = 32767;
    }

    public void setPickupDelay(int ticks)
    {
        delayBeforeCanPickup = ticks;
    }

    public boolean cannotPickup()
    {
        return delayBeforeCanPickup > 0;
    }

    public void setNoDespawn()
    {
        age = -6000;
    }

    public void func_174870_v()
    {
        setInfinitePickupDelay();
        age = 5999;
    }
}
