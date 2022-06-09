package net.minecraft.entity.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Rotations;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityArmorStand extends EntityLivingBase
{
    private static final Rotations DEFAULT_HEAD_ROTATION = new Rotations(0.0F, 0.0F, 0.0F);
    private static final Rotations DEFAULT_BODY_ROTATION = new Rotations(0.0F, 0.0F, 0.0F);
    private static final Rotations DEFAULT_LEFTARM_ROTATION = new Rotations(-10.0F, 0.0F, -10.0F);
    private static final Rotations DEFAULT_RIGHTARM_ROTATION = new Rotations(-15.0F, 0.0F, 10.0F);
    private static final Rotations DEFAULT_LEFTLEG_ROTATION = new Rotations(-1.0F, 0.0F, -1.0F);
    private static final Rotations DEFAULT_RIGHTLEG_ROTATION = new Rotations(1.0F, 0.0F, 1.0F);
    private final ItemStack[] contents;
    private boolean canInteract;

    /**
     * After punching the stand, the cooldown before you can punch it again without breaking it.
     */
    private long punchCooldown;
    private int disabledSlots;
    private boolean field_181028_bj;
    private Rotations headRotation;
    private Rotations bodyRotation;
    private Rotations leftArmRotation;
    private Rotations rightArmRotation;
    private Rotations leftLegRotation;
    private Rotations rightLegRotation;

    public EntityArmorStand(World worldIn)
    {
        super(worldIn);
        contents = new ItemStack[5];
        headRotation = EntityArmorStand.DEFAULT_HEAD_ROTATION;
        bodyRotation = EntityArmorStand.DEFAULT_BODY_ROTATION;
        leftArmRotation = EntityArmorStand.DEFAULT_LEFTARM_ROTATION;
        rightArmRotation = EntityArmorStand.DEFAULT_RIGHTARM_ROTATION;
        leftLegRotation = EntityArmorStand.DEFAULT_LEFTLEG_ROTATION;
        rightLegRotation = EntityArmorStand.DEFAULT_RIGHTLEG_ROTATION;
        setSilent(true);
        noClip = hasNoGravity();
        setSize(0.5F, 1.975F);
    }

    public EntityArmorStand(World worldIn, double posX, double posY, double posZ)
    {
        this(worldIn);
        setPosition(posX, posY, posZ);
    }

    /**
     * Returns whether the entity is in a server world
     */
    public boolean isServerWorld()
    {
        return super.isServerWorld() && !hasNoGravity();
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(10, Byte.valueOf((byte)0));
        dataWatcher.addObject(11, EntityArmorStand.DEFAULT_HEAD_ROTATION);
        dataWatcher.addObject(12, EntityArmorStand.DEFAULT_BODY_ROTATION);
        dataWatcher.addObject(13, EntityArmorStand.DEFAULT_LEFTARM_ROTATION);
        dataWatcher.addObject(14, EntityArmorStand.DEFAULT_RIGHTARM_ROTATION);
        dataWatcher.addObject(15, EntityArmorStand.DEFAULT_LEFTLEG_ROTATION);
        dataWatcher.addObject(16, EntityArmorStand.DEFAULT_RIGHTLEG_ROTATION);
    }

    /**
     * Returns the item that this EntityLiving is holding, if any.
     */
    public ItemStack getHeldItem()
    {
        return contents[0];
    }

    /**
     * 0: Tool in Hand; 1-4: Armor
     */
    public ItemStack getEquipmentInSlot(int slotIn)
    {
        return contents[slotIn];
    }

    public ItemStack getCurrentArmor(int slotIn)
    {
        return contents[slotIn + 1];
    }

    /**
     * Sets the held item, or an armor slot. Slot 0 is held item. Slot 1-4 is armor. Params: Item, slot
     */
    public void setCurrentItemOrArmor(int slotIn, ItemStack stack)
    {
        contents[slotIn] = stack;
    }

    /**
     * returns the inventory of this entity (only used in EntityPlayerMP it seems)
     */
    public ItemStack[] getInventory()
    {
        return contents;
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

            if (i < 0 || i >= contents.length)
            {
                return false;
            }
        }

        if (itemStackIn != null && EntityLiving.getArmorPosition(itemStackIn) != i && (i != 4 || !(itemStackIn.getItem() instanceof ItemBlock)))
        {
            return false;
        }
        else
        {
            setCurrentItemOrArmor(i, itemStackIn);
            return true;
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < contents.length; ++i)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            if (contents[i] != null)
            {
                contents[i].writeToNBT(nbttagcompound);
            }

            nbttaglist.appendTag(nbttagcompound);
        }

        tagCompound.setTag("Equipment", nbttaglist);

        if (getAlwaysRenderNameTag() && (getCustomNameTag() == null || getCustomNameTag().length() == 0))
        {
            tagCompound.setBoolean("CustomNameVisible", getAlwaysRenderNameTag());
        }

        tagCompound.setBoolean("Invisible", isInvisible());
        tagCompound.setBoolean("Small", isSmall());
        tagCompound.setBoolean("ShowArms", getShowArms());
        tagCompound.setInteger("DisabledSlots", disabledSlots);
        tagCompound.setBoolean("NoGravity", hasNoGravity());
        tagCompound.setBoolean("NoBasePlate", hasNoBasePlate());

        if (func_181026_s())
        {
            tagCompound.setBoolean("Marker", func_181026_s());
        }

        tagCompound.setTag("Pose", readPoseFromNBT());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("Equipment", 9))
        {
            NBTTagList nbttaglist = tagCompund.getTagList("Equipment", 10);

            for (int i = 0; i < contents.length; ++i)
            {
                contents[i] = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
            }
        }

        setInvisible(tagCompund.getBoolean("Invisible"));
        setSmall(tagCompund.getBoolean("Small"));
        setShowArms(tagCompund.getBoolean("ShowArms"));
        disabledSlots = tagCompund.getInteger("DisabledSlots");
        setNoGravity(tagCompund.getBoolean("NoGravity"));
        setNoBasePlate(tagCompund.getBoolean("NoBasePlate"));
        func_181027_m(tagCompund.getBoolean("Marker"));
        field_181028_bj = !func_181026_s();
        noClip = hasNoGravity();
        NBTTagCompound nbttagcompound = tagCompund.getCompoundTag("Pose");
        writePoseToNBT(nbttagcompound);
    }

    /**
     * Saves the pose to an NBTTagCompound.
     */
    private void writePoseToNBT(NBTTagCompound tagCompound)
    {
        NBTTagList nbttaglist = tagCompound.getTagList("Head", 5);

        if (nbttaglist.tagCount() > 0)
        {
            setHeadRotation(new Rotations(nbttaglist));
        }
        else
        {
            setHeadRotation(EntityArmorStand.DEFAULT_HEAD_ROTATION);
        }

        NBTTagList nbttaglist1 = tagCompound.getTagList("Body", 5);

        if (nbttaglist1.tagCount() > 0)
        {
            setBodyRotation(new Rotations(nbttaglist1));
        }
        else
        {
            setBodyRotation(EntityArmorStand.DEFAULT_BODY_ROTATION);
        }

        NBTTagList nbttaglist2 = tagCompound.getTagList("LeftArm", 5);

        if (nbttaglist2.tagCount() > 0)
        {
            setLeftArmRotation(new Rotations(nbttaglist2));
        }
        else
        {
            setLeftArmRotation(EntityArmorStand.DEFAULT_LEFTARM_ROTATION);
        }

        NBTTagList nbttaglist3 = tagCompound.getTagList("RightArm", 5);

        if (nbttaglist3.tagCount() > 0)
        {
            setRightArmRotation(new Rotations(nbttaglist3));
        }
        else
        {
            setRightArmRotation(EntityArmorStand.DEFAULT_RIGHTARM_ROTATION);
        }

        NBTTagList nbttaglist4 = tagCompound.getTagList("LeftLeg", 5);

        if (nbttaglist4.tagCount() > 0)
        {
            setLeftLegRotation(new Rotations(nbttaglist4));
        }
        else
        {
            setLeftLegRotation(EntityArmorStand.DEFAULT_LEFTLEG_ROTATION);
        }

        NBTTagList nbttaglist5 = tagCompound.getTagList("RightLeg", 5);

        if (nbttaglist5.tagCount() > 0)
        {
            setRightLegRotation(new Rotations(nbttaglist5));
        }
        else
        {
            setRightLegRotation(EntityArmorStand.DEFAULT_RIGHTLEG_ROTATION);
        }
    }

    private NBTTagCompound readPoseFromNBT()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        if (!EntityArmorStand.DEFAULT_HEAD_ROTATION.equals(headRotation))
        {
            nbttagcompound.setTag("Head", headRotation.writeToNBT());
        }

        if (!EntityArmorStand.DEFAULT_BODY_ROTATION.equals(bodyRotation))
        {
            nbttagcompound.setTag("Body", bodyRotation.writeToNBT());
        }

        if (!EntityArmorStand.DEFAULT_LEFTARM_ROTATION.equals(leftArmRotation))
        {
            nbttagcompound.setTag("LeftArm", leftArmRotation.writeToNBT());
        }

        if (!EntityArmorStand.DEFAULT_RIGHTARM_ROTATION.equals(rightArmRotation))
        {
            nbttagcompound.setTag("RightArm", rightArmRotation.writeToNBT());
        }

        if (!EntityArmorStand.DEFAULT_LEFTLEG_ROTATION.equals(leftLegRotation))
        {
            nbttagcompound.setTag("LeftLeg", leftLegRotation.writeToNBT());
        }

        if (!EntityArmorStand.DEFAULT_RIGHTLEG_ROTATION.equals(rightLegRotation))
        {
            nbttagcompound.setTag("RightLeg", rightLegRotation.writeToNBT());
        }

        return nbttagcompound;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    public boolean canBePushed()
    {
        return false;
    }

    protected void collideWithEntity(Entity p_82167_1_)
    {
    }

    protected void collideWithNearbyEntities()
    {
        List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox());

        if (list != null && !list.isEmpty())
        {
            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity = list.get(i);

                if (entity instanceof EntityMinecart && ((EntityMinecart)entity).getMinecartType() == EntityMinecart.EnumMinecartType.RIDEABLE && getDistanceSqToEntity(entity) <= 0.2D)
                {
                    entity.applyEntityCollision(this);
                }
            }
        }
    }

    /**
     * New version of interactWith that includes vector information on where precisely the player targeted.
     */
    public boolean interactAt(EntityPlayer player, Vec3 targetVec3)
    {
        if (func_181026_s())
        {
            return false;
        }
        else if (!worldObj.isRemote && !player.isSpectator())
        {
            int i = 0;
            ItemStack itemstack = player.getCurrentEquippedItem();
            boolean flag = itemstack != null;

            if (flag && itemstack.getItem() instanceof ItemArmor)
            {
                ItemArmor itemarmor = (ItemArmor)itemstack.getItem();

                if (itemarmor.armorType == 3)
                {
                    i = 1;
                }
                else if (itemarmor.armorType == 2)
                {
                    i = 2;
                }
                else if (itemarmor.armorType == 1)
                {
                    i = 3;
                }
                else if (itemarmor.armorType == 0)
                {
                    i = 4;
                }
            }

            if (flag && (itemstack.getItem() == Items.skull || itemstack.getItem() == Item.getItemFromBlock(Blocks.pumpkin)))
            {
                i = 4;
            }

            double d4 = 0.1D;
            double d0 = 0.9D;
            double d1 = 0.4D;
            double d2 = 1.6D;
            int j = 0;
            boolean flag1 = isSmall();
            double d3 = flag1 ? targetVec3.yCoord * 2.0D : targetVec3.yCoord;

            if (d3 >= 0.1D && d3 < 0.1D + (flag1 ? 0.8D : 0.45D) && contents[1] != null)
            {
                j = 1;
            }
            else if (d3 >= 0.9D + (flag1 ? 0.3D : 0.0D) && d3 < 0.9D + (flag1 ? 1.0D : 0.7D) && contents[3] != null)
            {
                j = 3;
            }
            else if (d3 >= 0.4D && d3 < 0.4D + (flag1 ? 1.0D : 0.8D) && contents[2] != null)
            {
                j = 2;
            }
            else if (d3 >= 1.6D && contents[4] != null)
            {
                j = 4;
            }

            boolean flag2 = contents[j] != null;

            if ((disabledSlots & 1 << j) != 0 || (disabledSlots & 1 << i) != 0)
            {
                j = i;

                if ((disabledSlots & 1 << i) != 0)
                {
                    if ((disabledSlots & 1) != 0)
                    {
                        return true;
                    }

                    j = 0;
                }
            }

            if (flag && i == 0 && !getShowArms())
            {
                return true;
            }
            else
            {
                if (flag)
                {
                    func_175422_a(player, i);
                }
                else if (flag2)
                {
                    func_175422_a(player, j);
                }

                return true;
            }
        }
        else
        {
            return true;
        }
    }

    private void func_175422_a(EntityPlayer p_175422_1_, int p_175422_2_)
    {
        ItemStack itemstack = contents[p_175422_2_];

        if (itemstack == null || (disabledSlots & 1 << p_175422_2_ + 8) == 0)
        {
            if (itemstack != null || (disabledSlots & 1 << p_175422_2_ + 16) == 0)
            {
                int i = p_175422_1_.inventory.currentItem;
                ItemStack itemstack1 = p_175422_1_.inventory.getStackInSlot(i);

                if (p_175422_1_.capabilities.isCreativeMode && (itemstack == null || itemstack.getItem() == Item.getItemFromBlock(Blocks.air)) && itemstack1 != null)
                {
                    ItemStack itemstack3 = itemstack1.copy();
                    itemstack3.stackSize = 1;
                    setCurrentItemOrArmor(p_175422_2_, itemstack3);
                }
                else if (itemstack1 != null && itemstack1.stackSize > 1)
                {
                    if (itemstack == null)
                    {
                        ItemStack itemstack2 = itemstack1.copy();
                        itemstack2.stackSize = 1;
                        setCurrentItemOrArmor(p_175422_2_, itemstack2);
                        --itemstack1.stackSize;
                    }
                }
                else
                {
                    setCurrentItemOrArmor(p_175422_2_, itemstack1);
                    p_175422_1_.inventory.setInventorySlotContents(i, itemstack);
                }
            }
        }
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (worldObj.isRemote)
        {
            return false;
        }
        else if (DamageSource.outOfWorld.equals(source))
        {
            setDead();
            return false;
        }
        else if (!isEntityInvulnerable(source) && !canInteract && !func_181026_s())
        {
            if (source.isExplosion())
            {
                dropContents();
                setDead();
                return false;
            }
            else if (DamageSource.inFire.equals(source))
            {
                if (!isBurning())
                {
                    setFire(5);
                }
                else
                {
                    damageArmorStand(0.15F);
                }

                return false;
            }
            else if (DamageSource.onFire.equals(source) && getHealth() > 0.5F)
            {
                damageArmorStand(4.0F);
                return false;
            }
            else
            {
                boolean flag = "arrow".equals(source.getDamageType());
                boolean flag1 = "player".equals(source.getDamageType());

                if (!flag1 && !flag)
                {
                    return false;
                }
                else
                {
                    if (source.getSourceOfDamage() instanceof EntityArrow)
                    {
                        source.getSourceOfDamage().setDead();
                    }

                    if (source.getEntity() instanceof EntityPlayer && !((EntityPlayer)source.getEntity()).capabilities.allowEdit)
                    {
                        return false;
                    }
                    else if (source.isCreativePlayer())
                    {
                        playParticles();
                        setDead();
                        return false;
                    }
                    else
                    {
                        long i = worldObj.getTotalWorldTime();

                        if (i - punchCooldown > 5L && !flag)
                        {
                            punchCooldown = i;
                        }
                        else
                        {
                            dropBlock();
                            playParticles();
                            setDead();
                        }

                        return false;
                    }
                }
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight Args: distance
     */
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

        if (Double.isNaN(d0) || d0 == 0.0D)
        {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    private void playParticles()
    {
        if (worldObj instanceof WorldServer)
        {
            ((WorldServer) worldObj).spawnParticle(EnumParticleTypes.BLOCK_DUST, posX, posY + (double) height / 1.5D, posZ, 10, width / 4.0F, height / 4.0F, width / 4.0F, 0.05D, Block.getStateId(Blocks.planks.getDefaultState()));
        }
    }

    private void damageArmorStand(float p_175406_1_)
    {
        float f = getHealth();
        f = f - p_175406_1_;

        if (f <= 0.5F)
        {
            dropContents();
            setDead();
        }
        else
        {
            setHealth(f);
        }
    }

    private void dropBlock()
    {
        Block.spawnAsEntity(worldObj, new BlockPos(this), new ItemStack(Items.armor_stand));
        dropContents();
    }

    private void dropContents()
    {
        for (int i = 0; i < contents.length; ++i)
        {
            if (contents[i] != null && contents[i].stackSize > 0)
            {
                if (contents[i] != null)
                {
                    Block.spawnAsEntity(worldObj, (new BlockPos(this)).up(), contents[i]);
                }

                contents[i] = null;
            }
        }
    }

    protected float func_110146_f(float p_110146_1_, float p_110146_2_)
    {
        prevRenderYawOffset = prevRotationYaw;
        renderYawOffset = rotationYaw;
        return 0.0F;
    }

    public float getEyeHeight()
    {
        return isChild() ? height * 0.5F : height * 0.9F;
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    public void moveEntityWithHeading(float strafe, float forward)
    {
        if (!hasNoGravity())
        {
            super.moveEntityWithHeading(strafe, forward);
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();
        Rotations rotations = dataWatcher.getWatchableObjectRotations(11);

        if (!headRotation.equals(rotations))
        {
            setHeadRotation(rotations);
        }

        Rotations rotations1 = dataWatcher.getWatchableObjectRotations(12);

        if (!bodyRotation.equals(rotations1))
        {
            setBodyRotation(rotations1);
        }

        Rotations rotations2 = dataWatcher.getWatchableObjectRotations(13);

        if (!leftArmRotation.equals(rotations2))
        {
            setLeftArmRotation(rotations2);
        }

        Rotations rotations3 = dataWatcher.getWatchableObjectRotations(14);

        if (!rightArmRotation.equals(rotations3))
        {
            setRightArmRotation(rotations3);
        }

        Rotations rotations4 = dataWatcher.getWatchableObjectRotations(15);

        if (!leftLegRotation.equals(rotations4))
        {
            setLeftLegRotation(rotations4);
        }

        Rotations rotations5 = dataWatcher.getWatchableObjectRotations(16);

        if (!rightLegRotation.equals(rotations5))
        {
            setRightLegRotation(rotations5);
        }

        boolean flag = func_181026_s();

        if (!field_181028_bj && flag)
        {
            func_181550_a(false);
        }
        else
        {
            if (!field_181028_bj || flag)
            {
                return;
            }

            func_181550_a(true);
        }

        field_181028_bj = flag;
    }

    private void func_181550_a(boolean p_181550_1_)
    {
        double d0 = posX;
        double d1 = posY;
        double d2 = posZ;

        if (p_181550_1_)
        {
            setSize(0.5F, 1.975F);
        }
        else
        {
            setSize(0.0F, 0.0F);
        }

        setPosition(d0, d1, d2);
    }

    /**
     * Clears potion metadata values if the entity has no potion effects. Otherwise, updates potion effect color,
     * ambience, and invisibility metadata values
     */
    protected void updatePotionMetadata()
    {
        setInvisible(canInteract);
    }

    public void setInvisible(boolean invisible)
    {
        canInteract = invisible;
        super.setInvisible(invisible);
    }

    /**
     * If Animal, checks if the age timer is negative
     */
    public boolean isChild()
    {
        return isSmall();
    }

    /**
     * Called by the /kill command.
     */
    public void onKillCommand()
    {
        setDead();
    }

    public boolean isImmuneToExplosions()
    {
        return isInvisible();
    }

    private void setSmall(boolean p_175420_1_)
    {
        byte b0 = dataWatcher.getWatchableObjectByte(10);

        if (p_175420_1_)
        {
            b0 = (byte)(b0 | 1);
        }
        else
        {
            b0 = (byte)(b0 & -2);
        }

        dataWatcher.updateObject(10, Byte.valueOf(b0));
    }

    public boolean isSmall()
    {
        return (dataWatcher.getWatchableObjectByte(10) & 1) != 0;
    }

    private void setNoGravity(boolean p_175425_1_)
    {
        byte b0 = dataWatcher.getWatchableObjectByte(10);

        if (p_175425_1_)
        {
            b0 = (byte)(b0 | 2);
        }
        else
        {
            b0 = (byte)(b0 & -3);
        }

        dataWatcher.updateObject(10, Byte.valueOf(b0));
    }

    public boolean hasNoGravity()
    {
        return (dataWatcher.getWatchableObjectByte(10) & 2) != 0;
    }

    private void setShowArms(boolean p_175413_1_)
    {
        byte b0 = dataWatcher.getWatchableObjectByte(10);

        if (p_175413_1_)
        {
            b0 = (byte)(b0 | 4);
        }
        else
        {
            b0 = (byte)(b0 & -5);
        }

        dataWatcher.updateObject(10, Byte.valueOf(b0));
    }

    public boolean getShowArms()
    {
        return (dataWatcher.getWatchableObjectByte(10) & 4) != 0;
    }

    private void setNoBasePlate(boolean p_175426_1_)
    {
        byte b0 = dataWatcher.getWatchableObjectByte(10);

        if (p_175426_1_)
        {
            b0 = (byte)(b0 | 8);
        }
        else
        {
            b0 = (byte)(b0 & -9);
        }

        dataWatcher.updateObject(10, Byte.valueOf(b0));
    }

    public boolean hasNoBasePlate()
    {
        return (dataWatcher.getWatchableObjectByte(10) & 8) != 0;
    }

    private void func_181027_m(boolean p_181027_1_)
    {
        byte b0 = dataWatcher.getWatchableObjectByte(10);

        if (p_181027_1_)
        {
            b0 = (byte)(b0 | 16);
        }
        else
        {
            b0 = (byte)(b0 & -17);
        }

        dataWatcher.updateObject(10, Byte.valueOf(b0));
    }

    public boolean func_181026_s()
    {
        return (dataWatcher.getWatchableObjectByte(10) & 16) != 0;
    }

    public void setHeadRotation(Rotations p_175415_1_)
    {
        headRotation = p_175415_1_;
        dataWatcher.updateObject(11, p_175415_1_);
    }

    public void setBodyRotation(Rotations p_175424_1_)
    {
        bodyRotation = p_175424_1_;
        dataWatcher.updateObject(12, p_175424_1_);
    }

    public void setLeftArmRotation(Rotations p_175405_1_)
    {
        leftArmRotation = p_175405_1_;
        dataWatcher.updateObject(13, p_175405_1_);
    }

    public void setRightArmRotation(Rotations p_175428_1_)
    {
        rightArmRotation = p_175428_1_;
        dataWatcher.updateObject(14, p_175428_1_);
    }

    public void setLeftLegRotation(Rotations p_175417_1_)
    {
        leftLegRotation = p_175417_1_;
        dataWatcher.updateObject(15, p_175417_1_);
    }

    public void setRightLegRotation(Rotations p_175427_1_)
    {
        rightLegRotation = p_175427_1_;
        dataWatcher.updateObject(16, p_175427_1_);
    }

    public Rotations getHeadRotation()
    {
        return headRotation;
    }

    public Rotations getBodyRotation()
    {
        return bodyRotation;
    }

    public Rotations getLeftArmRotation()
    {
        return leftArmRotation;
    }

    public Rotations getRightArmRotation()
    {
        return rightArmRotation;
    }

    public Rotations getLeftLegRotation()
    {
        return leftLegRotation;
    }

    public Rotations getRightLegRotation()
    {
        return rightLegRotation;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return super.canBeCollidedWith() && !func_181026_s();
    }
}
