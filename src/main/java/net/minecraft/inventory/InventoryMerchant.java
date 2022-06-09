package net.minecraft.inventory;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class InventoryMerchant implements IInventory
{
    private final IMerchant theMerchant;
    private final ItemStack[] theInventory = new ItemStack[3];
    private final EntityPlayer thePlayer;
    private MerchantRecipe currentRecipe;
    private int currentRecipeIndex;

    public InventoryMerchant(EntityPlayer thePlayerIn, IMerchant theMerchantIn)
    {
        thePlayer = thePlayerIn;
        theMerchant = theMerchantIn;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory()
    {
        return theInventory.length;
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index)
    {
        return theInventory[index];
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count)
    {
        if (theInventory[index] != null)
        {
            if (index == 2)
            {
                ItemStack itemstack2 = theInventory[index];
                theInventory[index] = null;
                return itemstack2;
            }
            else if (theInventory[index].stackSize <= count)
            {
                ItemStack itemstack1 = theInventory[index];
                theInventory[index] = null;

                if (inventoryResetNeededOnSlotChange(index))
                {
                    resetRecipeAndSlots();
                }

                return itemstack1;
            }
            else
            {
                ItemStack itemstack = theInventory[index].splitStack(count);

                if (theInventory[index].stackSize == 0)
                {
                    theInventory[index] = null;
                }

                if (inventoryResetNeededOnSlotChange(index))
                {
                    resetRecipeAndSlots();
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
     * if par1 slot has changed, does resetRecipeAndSlots need to be called?
     */
    private boolean inventoryResetNeededOnSlotChange(int p_70469_1_)
    {
        return p_70469_1_ == 0 || p_70469_1_ == 1;
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeStackFromSlot(int index)
    {
        if (theInventory[index] != null)
        {
            ItemStack itemstack = theInventory[index];
            theInventory[index] = null;
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
        theInventory[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit())
        {
            stack.stackSize = getInventoryStackLimit();
        }

        if (inventoryResetNeededOnSlotChange(index))
        {
            resetRecipeAndSlots();
        }
    }

    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
    public String getName()
    {
        return "mob.villager";
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        return false;
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public IChatComponent getDisplayName()
    {
        return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName(), new Object[0]);
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
        return theMerchant.getCustomer() == player;
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
        return true;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void markDirty()
    {
        resetRecipeAndSlots();
    }

    public void resetRecipeAndSlots()
    {
        currentRecipe = null;
        ItemStack itemstack = theInventory[0];
        ItemStack itemstack1 = theInventory[1];

        if (itemstack == null)
        {
            itemstack = itemstack1;
            itemstack1 = null;
        }

        if (itemstack == null)
        {
            setInventorySlotContents(2, null);
        }
        else
        {
            MerchantRecipeList merchantrecipelist = theMerchant.getRecipes(thePlayer);

            if (merchantrecipelist != null)
            {
                MerchantRecipe merchantrecipe = merchantrecipelist.canRecipeBeUsed(itemstack, itemstack1, currentRecipeIndex);

                if (merchantrecipe != null && !merchantrecipe.isRecipeDisabled())
                {
                    currentRecipe = merchantrecipe;
                    setInventorySlotContents(2, merchantrecipe.getItemToSell().copy());
                }
                else if (itemstack1 != null)
                {
                    merchantrecipe = merchantrecipelist.canRecipeBeUsed(itemstack1, itemstack, currentRecipeIndex);

                    if (merchantrecipe != null && !merchantrecipe.isRecipeDisabled())
                    {
                        currentRecipe = merchantrecipe;
                        setInventorySlotContents(2, merchantrecipe.getItemToSell().copy());
                    }
                    else
                    {
                        setInventorySlotContents(2, null);
                    }
                }
                else
                {
                    setInventorySlotContents(2, null);
                }
            }
        }

        theMerchant.verifySellingItem(getStackInSlot(2));
    }

    public MerchantRecipe getCurrentRecipe()
    {
        return currentRecipe;
    }

    public void setCurrentRecipeIndex(int currentRecipeIndexIn)
    {
        currentRecipeIndex = currentRecipeIndexIn;
        resetRecipeAndSlots();
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
        for (int i = 0; i < theInventory.length; ++i)
        {
            theInventory[i] = null;
        }
    }
}
