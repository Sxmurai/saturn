package net.minecraft.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class TileEntityChest extends TileEntityLockable implements ITickable, IInventory
{
    private ItemStack[] chestContents = new ItemStack[27];

    /** Determines if the check for adjacent chests has taken place. */
    public boolean adjacentChestChecked;

    /** Contains the chest tile located adjacent to this one (if any) */
    public TileEntityChest adjacentChestZNeg;

    /** Contains the chest tile located adjacent to this one (if any) */
    public TileEntityChest adjacentChestXPos;

    /** Contains the chest tile located adjacent to this one (if any) */
    public TileEntityChest adjacentChestXNeg;

    /** Contains the chest tile located adjacent to this one (if any) */
    public TileEntityChest adjacentChestZPos;

    /** The current angle of the lid (between 0 and 1) */
    public float lidAngle;

    /** The angle of the lid last tick */
    public float prevLidAngle;

    /** The number of players currently using this chest */
    public int numPlayersUsing;

    /** Server sync counter (once per 20 ticks) */
    private int ticksSinceSync;
    private int cachedChestType;
    private String customName;

    public TileEntityChest()
    {
        cachedChestType = -1;
    }

    public TileEntityChest(int chestType)
    {
        cachedChestType = chestType;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory()
    {
        return 27;
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index)
    {
        return chestContents[index];
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count)
    {
        if (chestContents[index] != null)
        {
            if (chestContents[index].stackSize <= count)
            {
                ItemStack itemstack1 = chestContents[index];
                chestContents[index] = null;
                markDirty();
                return itemstack1;
            }
            else
            {
                ItemStack itemstack = chestContents[index].splitStack(count);

                if (chestContents[index].stackSize == 0)
                {
                    chestContents[index] = null;
                }

                markDirty();
                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeStackFromSlot(int index)
    {
        if (chestContents[index] != null)
        {
            ItemStack itemstack = chestContents[index];
            chestContents[index] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        chestContents[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit())
        {
            stack.stackSize = getInventoryStackLimit();
        }

        markDirty();
    }

    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
    public String getName()
    {
        return hasCustomName() ? customName : "container.chest";
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        return customName != null && customName.length() > 0;
    }

    public void setCustomName(String name)
    {
        customName = name;
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        chestContents = new ItemStack[getSizeInventory()];

        if (compound.hasKey("CustomName", 8))
        {
            customName = compound.getString("CustomName");
        }

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < chestContents.length)
            {
                chestContents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < chestContents.length; ++i)
        {
            if (chestContents[i] != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                chestContents[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Items", nbttaglist);

        if (hasCustomName())
        {
            compound.setString("CustomName", customName);
        }
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    public int getInventoryStackLimit()
    {
        return 64;
    }

    /**
     * Do not make give this method the name canInteractWith because it clashes with Container
     */
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(pos) == this && player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }

    public void updateContainingBlockInfo()
    {
        super.updateContainingBlockInfo();
        adjacentChestChecked = false;
    }

    @SuppressWarnings("incomplete-switch")
    private void func_174910_a(TileEntityChest chestTe, EnumFacing side)
    {
        if (chestTe.isInvalid())
        {
            adjacentChestChecked = false;
        }
        else if (adjacentChestChecked)
        {
            switch (side)
            {
                case NORTH:
                    if (adjacentChestZNeg != chestTe)
                    {
                        adjacentChestChecked = false;
                    }

                    break;

                case SOUTH:
                    if (adjacentChestZPos != chestTe)
                    {
                        adjacentChestChecked = false;
                    }

                    break;

                case EAST:
                    if (adjacentChestXPos != chestTe)
                    {
                        adjacentChestChecked = false;
                    }

                    break;

                case WEST:
                    if (adjacentChestXNeg != chestTe)
                    {
                        adjacentChestChecked = false;
                    }
            }
        }
    }

    /**
     * Performs the check for adjacent chests to determine if this chest is double or not.
     */
    public void checkForAdjacentChests()
    {
        if (!adjacentChestChecked)
        {
            adjacentChestChecked = true;
            adjacentChestXNeg = getAdjacentChest(EnumFacing.WEST);
            adjacentChestXPos = getAdjacentChest(EnumFacing.EAST);
            adjacentChestZNeg = getAdjacentChest(EnumFacing.NORTH);
            adjacentChestZPos = getAdjacentChest(EnumFacing.SOUTH);
        }
    }

    protected TileEntityChest getAdjacentChest(EnumFacing side)
    {
        BlockPos blockpos = pos.offset(side);

        if (isChestAt(blockpos))
        {
            TileEntity tileentity = worldObj.getTileEntity(blockpos);

            if (tileentity instanceof TileEntityChest)
            {
                TileEntityChest tileentitychest = (TileEntityChest)tileentity;
                tileentitychest.func_174910_a(this, side.getOpposite());
                return tileentitychest;
            }
        }

        return null;
    }

    private boolean isChestAt(BlockPos posIn)
    {
        if (worldObj == null)
        {
            return false;
        }
        else
        {
            Block block = worldObj.getBlockState(posIn).getBlock();
            return block instanceof BlockChest && ((BlockChest)block).chestType == getChestType();
        }
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        checkForAdjacentChests();
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        ++ticksSinceSync;

        if (!worldObj.isRemote && numPlayersUsing != 0 && (ticksSinceSync + i + j + k) % 200 == 0)
        {
            numPlayersUsing = 0;
            float f = 5.0F;

            for (EntityPlayer entityplayer : worldObj.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB((float)i - f, (float)j - f, (float)k - f, (float)(i + 1) + f, (float)(j + 1) + f, (float)(k + 1) + f)))
            {
                if (entityplayer.openContainer instanceof ContainerChest)
                {
                    IInventory iinventory = ((ContainerChest)entityplayer.openContainer).getLowerChestInventory();

                    if (iinventory == this || iinventory instanceof InventoryLargeChest && ((InventoryLargeChest)iinventory).isPartOfLargeChest(this))
                    {
                        ++numPlayersUsing;
                    }
                }
            }
        }

        prevLidAngle = lidAngle;
        float f1 = 0.1F;

        if (numPlayersUsing > 0 && lidAngle == 0.0F && adjacentChestZNeg == null && adjacentChestXNeg == null)
        {
            double d1 = (double)i + 0.5D;
            double d2 = (double)k + 0.5D;

            if (adjacentChestZPos != null)
            {
                d2 += 0.5D;
            }

            if (adjacentChestXPos != null)
            {
                d1 += 0.5D;
            }

            worldObj.playSoundEffect(d1, (double)j + 0.5D, d2, "random.chestopen", 0.5F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (numPlayersUsing == 0 && lidAngle > 0.0F || numPlayersUsing > 0 && lidAngle < 1.0F)
        {
            float f2 = lidAngle;

            if (numPlayersUsing > 0)
            {
                lidAngle += f1;
            }
            else
            {
                lidAngle -= f1;
            }

            if (lidAngle > 1.0F)
            {
                lidAngle = 1.0F;
            }

            float f3 = 0.5F;

            if (lidAngle < f3 && f2 >= f3 && adjacentChestZNeg == null && adjacentChestXNeg == null)
            {
                double d3 = (double)i + 0.5D;
                double d0 = (double)k + 0.5D;

                if (adjacentChestZPos != null)
                {
                    d0 += 0.5D;
                }

                if (adjacentChestXPos != null)
                {
                    d3 += 0.5D;
                }

                worldObj.playSoundEffect(d3, (double)j + 0.5D, d0, "random.chestclosed", 0.5F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
            }

            if (lidAngle < 0.0F)
            {
                lidAngle = 0.0F;
            }
        }
    }

    public boolean receiveClientEvent(int id, int type)
    {
        if (id == 1)
        {
            numPlayersUsing = type;
            return true;
        }
        else
        {
            return super.receiveClientEvent(id, type);
        }
    }

    public void openInventory(EntityPlayer player)
    {
        if (!player.isSpectator())
        {
            if (numPlayersUsing < 0)
            {
                numPlayersUsing = 0;
            }

            ++numPlayersUsing;
            worldObj.addBlockEvent(pos, getBlockType(), 1, numPlayersUsing);
            worldObj.notifyNeighborsOfStateChange(pos, getBlockType());
            worldObj.notifyNeighborsOfStateChange(pos.down(), getBlockType());
        }
    }

    public void closeInventory(EntityPlayer player)
    {
        if (!player.isSpectator() && getBlockType() instanceof BlockChest)
        {
            --numPlayersUsing;
            worldObj.addBlockEvent(pos, getBlockType(), 1, numPlayersUsing);
            worldObj.notifyNeighborsOfStateChange(pos, getBlockType());
            worldObj.notifyNeighborsOfStateChange(pos.down(), getBlockType());
        }
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    /**
     * invalidates a tile entity
     */
    public void invalidate()
    {
        super.invalidate();
        updateContainingBlockInfo();
        checkForAdjacentChests();
    }

    public int getChestType()
    {
        if (cachedChestType == -1)
        {
            if (worldObj == null || !(getBlockType() instanceof BlockChest))
            {
                return 0;
            }

            cachedChestType = ((BlockChest) getBlockType()).chestType;
        }

        return cachedChestType;
    }

    public String getGuiID()
    {
        return "minecraft:chest";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerChest(playerInventory, this, playerIn);
    }

    public int getField(int id)
    {
        return 0;
    }

    public void setField(int id, int value)
    {
    }

    public int getFieldCount()
    {
        return 0;
    }

    public void clear()
    {
        for (int i = 0; i < chestContents.length; ++i)
        {
            chestContents[i] = null;
        }
    }
}
