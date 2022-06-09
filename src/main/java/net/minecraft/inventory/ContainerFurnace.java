package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;

public class ContainerFurnace extends Container
{
    private final IInventory tileFurnace;
    private int field_178152_f;
    private int field_178153_g;
    private int field_178154_h;
    private int field_178155_i;

    public ContainerFurnace(InventoryPlayer playerInventory, IInventory furnaceInventory)
    {
        tileFurnace = furnaceInventory;
        addSlotToContainer(new Slot(furnaceInventory, 0, 56, 17));
        addSlotToContainer(new SlotFurnaceFuel(furnaceInventory, 1, 56, 53));
        addSlotToContainer(new SlotFurnaceOutput(playerInventory.player, furnaceInventory, 2, 116, 35));

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k)
        {
            addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public void onCraftGuiOpened(ICrafting listener)
    {
        super.onCraftGuiOpened(listener);
        listener.func_175173_a(this, tileFurnace);
    }

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for (int i = 0; i < crafters.size(); ++i)
        {
            ICrafting icrafting = crafters.get(i);

            if (field_178152_f != tileFurnace.getField(2))
            {
                icrafting.sendProgressBarUpdate(this, 2, tileFurnace.getField(2));
            }

            if (field_178154_h != tileFurnace.getField(0))
            {
                icrafting.sendProgressBarUpdate(this, 0, tileFurnace.getField(0));
            }

            if (field_178155_i != tileFurnace.getField(1))
            {
                icrafting.sendProgressBarUpdate(this, 1, tileFurnace.getField(1));
            }

            if (field_178153_g != tileFurnace.getField(3))
            {
                icrafting.sendProgressBarUpdate(this, 3, tileFurnace.getField(3));
            }
        }

        field_178152_f = tileFurnace.getField(2);
        field_178154_h = tileFurnace.getField(0);
        field_178155_i = tileFurnace.getField(1);
        field_178153_g = tileFurnace.getField(3);
    }

    public void updateProgressBar(int id, int data)
    {
        tileFurnace.setField(id, data);
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return tileFurnace.isUseableByPlayer(playerIn);
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

            if (index == 2)
            {
                if (!mergeItemStack(itemstack1, 3, 39, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index != 1 && index != 0)
            {
                if (FurnaceRecipes.instance().getSmeltingResult(itemstack1) != null)
                {
                    if (!mergeItemStack(itemstack1, 0, 1, false))
                    {
                        return null;
                    }
                }
                else if (TileEntityFurnace.isItemFuel(itemstack1))
                {
                    if (!mergeItemStack(itemstack1, 1, 2, false))
                    {
                        return null;
                    }
                }
                else if (index >= 3 && index < 30)
                {
                    if (!mergeItemStack(itemstack1, 30, 39, false))
                    {
                        return null;
                    }
                }
                else if (index >= 30 && index < 39 && !mergeItemStack(itemstack1, 3, 30, false))
                {
                    return null;
                }
            }
            else if (!mergeItemStack(itemstack1, 3, 39, false))
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

            if (itemstack1.stackSize == itemstack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemstack;
    }
}
