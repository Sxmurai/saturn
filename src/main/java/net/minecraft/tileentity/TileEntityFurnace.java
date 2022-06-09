package net.minecraft.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.MathHelper;

public class TileEntityFurnace extends TileEntityLockable implements ITickable, ISidedInventory
{
    private static final int[] slotsTop = new int[] {0};
    private static final int[] slotsBottom = new int[] {2, 1};
    private static final int[] slotsSides = new int[] {1};

    /**
     * The ItemStacks that hold the items currently being used in the furnace
     */
    private ItemStack[] furnaceItemStacks = new ItemStack[3];

    /** The number of ticks that the furnace will keep burning */
    private int furnaceBurnTime;

    /**
     * The number of ticks that a fresh copy of the currently-burning item would keep the furnace burning for
     */
    private int currentItemBurnTime;
    private int cookTime;
    private int totalCookTime;
    private String furnaceCustomName;

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory()
    {
        return furnaceItemStacks.length;
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index)
    {
        return furnaceItemStacks[index];
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count)
    {
        if (furnaceItemStacks[index] != null)
        {
            if (furnaceItemStacks[index].stackSize <= count)
            {
                ItemStack itemstack1 = furnaceItemStacks[index];
                furnaceItemStacks[index] = null;
                return itemstack1;
            }
            else
            {
                ItemStack itemstack = furnaceItemStacks[index].splitStack(count);

                if (furnaceItemStacks[index].stackSize == 0)
                {
                    furnaceItemStacks[index] = null;
                }

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
        if (furnaceItemStacks[index] != null)
        {
            ItemStack itemstack = furnaceItemStacks[index];
            furnaceItemStacks[index] = null;
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
        boolean flag = stack != null && stack.isItemEqual(furnaceItemStacks[index]) && ItemStack.areItemStackTagsEqual(stack, furnaceItemStacks[index]);
        furnaceItemStacks[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit())
        {
            stack.stackSize = getInventoryStackLimit();
        }

        if (index == 0 && !flag)
        {
            totalCookTime = getCookTime(stack);
            cookTime = 0;
            markDirty();
        }
    }

    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
    public String getName()
    {
        return hasCustomName() ? furnaceCustomName : "container.furnace";
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        return furnaceCustomName != null && furnaceCustomName.length() > 0;
    }

    public void setCustomInventoryName(String p_145951_1_)
    {
        furnaceCustomName = p_145951_1_;
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        furnaceItemStacks = new ItemStack[getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot");

            if (j >= 0 && j < furnaceItemStacks.length)
            {
                furnaceItemStacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }

        furnaceBurnTime = compound.getShort("BurnTime");
        cookTime = compound.getShort("CookTime");
        totalCookTime = compound.getShort("CookTimeTotal");
        currentItemBurnTime = TileEntityFurnace.getItemBurnTime(furnaceItemStacks[1]);

        if (compound.hasKey("CustomName", 8))
        {
            furnaceCustomName = compound.getString("CustomName");
        }
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setShort("BurnTime", (short) furnaceBurnTime);
        compound.setShort("CookTime", (short) cookTime);
        compound.setShort("CookTimeTotal", (short) totalCookTime);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < furnaceItemStacks.length; ++i)
        {
            if (furnaceItemStacks[i] != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                furnaceItemStacks[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Items", nbttaglist);

        if (hasCustomName())
        {
            compound.setString("CustomName", furnaceCustomName);
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
     * Furnace isBurning
     */
    public boolean isBurning()
    {
        return furnaceBurnTime > 0;
    }

    public static boolean isBurning(IInventory p_174903_0_)
    {
        return p_174903_0_.getField(0) > 0;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        boolean flag = isBurning();
        boolean flag1 = false;

        if (isBurning())
        {
            --furnaceBurnTime;
        }

        if (!worldObj.isRemote)
        {
            if (isBurning() || furnaceItemStacks[1] != null && furnaceItemStacks[0] != null)
            {
                if (!isBurning() && canSmelt())
                {
                    currentItemBurnTime = furnaceBurnTime = TileEntityFurnace.getItemBurnTime(furnaceItemStacks[1]);

                    if (isBurning())
                    {
                        flag1 = true;

                        if (furnaceItemStacks[1] != null)
                        {
                            --furnaceItemStacks[1].stackSize;

                            if (furnaceItemStacks[1].stackSize == 0)
                            {
                                Item item = furnaceItemStacks[1].getItem().getContainerItem();
                                furnaceItemStacks[1] = item != null ? new ItemStack(item) : null;
                            }
                        }
                    }
                }

                if (isBurning() && canSmelt())
                {
                    ++cookTime;

                    if (cookTime == totalCookTime)
                    {
                        cookTime = 0;
                        totalCookTime = getCookTime(furnaceItemStacks[0]);
                        smeltItem();
                        flag1 = true;
                    }
                }
                else
                {
                    cookTime = 0;
                }
            }
            else if (!isBurning() && cookTime > 0)
            {
                cookTime = MathHelper.clamp_int(cookTime - 2, 0, totalCookTime);
            }

            if (flag != isBurning())
            {
                flag1 = true;
                BlockFurnace.setState(isBurning(), worldObj, pos);
            }
        }

        if (flag1)
        {
            markDirty();
        }
    }

    public int getCookTime(ItemStack stack)
    {
        return 200;
    }

    /**
     * Returns true if the furnace can smelt an item, i.e. has a source item, destination stack isn't full, etc.
     */
    private boolean canSmelt()
    {
        if (furnaceItemStacks[0] == null)
        {
            return false;
        }
        else
        {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(furnaceItemStacks[0]);
            return itemstack != null && (furnaceItemStacks[2] == null || (furnaceItemStacks[2].isItemEqual(itemstack) && (furnaceItemStacks[2].stackSize < getInventoryStackLimit() && furnaceItemStacks[2].stackSize < furnaceItemStacks[2].getMaxStackSize() || furnaceItemStacks[2].stackSize < itemstack.getMaxStackSize())));
        }
    }

    /**
     * Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack
     */
    public void smeltItem()
    {
        if (canSmelt())
        {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(furnaceItemStacks[0]);

            if (furnaceItemStacks[2] == null)
            {
                furnaceItemStacks[2] = itemstack.copy();
            }
            else if (furnaceItemStacks[2].getItem() == itemstack.getItem())
            {
                ++furnaceItemStacks[2].stackSize;
            }

            if (furnaceItemStacks[0].getItem() == Item.getItemFromBlock(Blocks.sponge) && furnaceItemStacks[0].getMetadata() == 1 && furnaceItemStacks[1] != null && furnaceItemStacks[1].getItem() == Items.bucket)
            {
                furnaceItemStacks[1] = new ItemStack(Items.water_bucket);
            }

            --furnaceItemStacks[0].stackSize;

            if (furnaceItemStacks[0].stackSize <= 0)
            {
                furnaceItemStacks[0] = null;
            }
        }
    }

    /**
     * Returns the number of ticks that the supplied fuel item will keep the furnace burning, or 0 if the item isn't
     * fuel
     */
    public static int getItemBurnTime(ItemStack p_145952_0_)
    {
        if (p_145952_0_ == null)
        {
            return 0;
        }
        else
        {
            Item item = p_145952_0_.getItem();

            if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.air)
            {
                Block block = Block.getBlockFromItem(item);

                if (block == Blocks.wooden_slab)
                {
                    return 150;
                }

                if (block.getMaterial() == Material.wood)
                {
                    return 300;
                }

                if (block == Blocks.coal_block)
                {
                    return 16000;
                }
            }

            return item instanceof ItemTool && ((ItemTool)item).getToolMaterialName().equals("WOOD") ? 200 : (item instanceof ItemSword && ((ItemSword)item).getToolMaterialName().equals("WOOD") ? 200 : (item instanceof ItemHoe && ((ItemHoe)item).getMaterialName().equals("WOOD") ? 200 : (item == Items.stick ? 100 : (item == Items.coal ? 1600 : (item == Items.lava_bucket ? 20000 : (item == Item.getItemFromBlock(Blocks.sapling) ? 100 : (item == Items.blaze_rod ? 2400 : 0)))))));
        }
    }

    public static boolean isItemFuel(ItemStack p_145954_0_)
    {
        return TileEntityFurnace.getItemBurnTime(p_145954_0_) > 0;
    }

    /**
     * Do not make give this method the name canInteractWith because it clashes with Container
     */
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(pos) == this && player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }

    public void openInventory(EntityPlayer player)
    {
    }

    public void closeInventory(EntityPlayer player)
    {
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return index != 2 && (index != 1 || TileEntityFurnace.isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack));
    }

    public int[] getSlotsForFace(EnumFacing side)
    {
        return side == EnumFacing.DOWN ? TileEntityFurnace.slotsBottom : (side == EnumFacing.UP ? TileEntityFurnace.slotsTop : TileEntityFurnace.slotsSides);
    }

    /**
     * Returns true if automation can insert the given item in the given slot from the given side. Args: slot, item,
     * side
     */
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction)
    {
        return isItemValidForSlot(index, itemStackIn);
    }

    /**
     * Returns true if automation can extract the given item in the given slot from the given side. Args: slot, item,
     * side
     */
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        if (direction == EnumFacing.DOWN && index == 1)
        {
            Item item = stack.getItem();

            return item == Items.water_bucket || item == Items.bucket;
        }

        return true;
    }

    public String getGuiID()
    {
        return "minecraft:furnace";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerFurnace(playerInventory, this);
    }

    public int getField(int id)
    {
        switch (id)
        {
            case 0:
                return furnaceBurnTime;

            case 1:
                return currentItemBurnTime;

            case 2:
                return cookTime;

            case 3:
                return totalCookTime;

            default:
                return 0;
        }
    }

    public void setField(int id, int value)
    {
        switch (id)
        {
            case 0:
                furnaceBurnTime = value;
                break;

            case 1:
                currentItemBurnTime = value;
                break;

            case 2:
                cookTime = value;
                break;

            case 3:
                totalCookTime = value;
        }
    }

    public int getFieldCount()
    {
        return 4;
    }

    public void clear()
    {
        for (int i = 0; i < furnaceItemStacks.length; ++i)
        {
            furnaceItemStacks[i] = null;
        }
    }
}
