package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class ContainerHopper extends Container
{
    private final IInventory hopperInventory;

    public ContainerHopper(InventoryPlayer playerInventory, IInventory hopperInventoryIn, EntityPlayer player)
    {
        hopperInventory = hopperInventoryIn;
        hopperInventoryIn.openInventory(player);
        int i = 51;

        for (int j = 0; j < hopperInventoryIn.getSizeInventory(); ++j)
        {
            addSlotToContainer(new Slot(hopperInventoryIn, j, 44 + j * 18, 20));
        }

        for (int l = 0; l < 3; ++l)
        {
            for (int k = 0; k < 9; ++k)
            {
                addSlotToContainer(new Slot(playerInventory, k + l * 9 + 9, 8 + k * 18, l * 18 + i));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 58 + i));
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return hopperInventory.isUseableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < hopperInventory.getSizeInventory())
            {
                if (!mergeItemStack(itemstack1, hopperInventory.getSizeInventory(), inventorySlots.size(), true))
                {
                    return null;
                }
            }
            else if (!mergeItemStack(itemstack1, 0, hopperInventory.getSizeInventory(), false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack(null);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        hopperInventory.closeInventory(playerIn);
    }
}
