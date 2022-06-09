package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.AchievementList;

public class ContainerBrewingStand extends Container
{
    private final IInventory tileBrewingStand;

    /** Instance of Slot. */
    private final Slot theSlot;
    private int brewTime;

    public ContainerBrewingStand(InventoryPlayer playerInventory, IInventory tileBrewingStandIn)
    {
        tileBrewingStand = tileBrewingStandIn;
        addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 0, 56, 46));
        addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 1, 79, 53));
        addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 2, 102, 46));
        theSlot = addSlotToContainer(new ContainerBrewingStand.Ingredient(tileBrewingStandIn, 3, 79, 17));

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
        listener.func_175173_a(this, tileBrewingStand);
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

            if (brewTime != tileBrewingStand.getField(0))
            {
                icrafting.sendProgressBarUpdate(this, 0, tileBrewingStand.getField(0));
            }
        }

        brewTime = tileBrewingStand.getField(0);
    }

    public void updateProgressBar(int id, int data)
    {
        tileBrewingStand.setField(id, data);
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return tileBrewingStand.isUseableByPlayer(playerIn);
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

            if ((index < 0 || index > 2) && index != 3)
            {
                if (!theSlot.getHasStack() && theSlot.isItemValid(itemstack1))
                {
                    if (!mergeItemStack(itemstack1, 3, 4, false))
                    {
                        return null;
                    }
                }
                else if (ContainerBrewingStand.Potion.canHoldPotion(itemstack))
                {
                    if (!mergeItemStack(itemstack1, 0, 3, false))
                    {
                        return null;
                    }
                }
                else if (index >= 4 && index < 31)
                {
                    if (!mergeItemStack(itemstack1, 31, 40, false))
                    {
                        return null;
                    }
                }
                else if (index >= 31 && index < 40)
                {
                    if (!mergeItemStack(itemstack1, 4, 31, false))
                    {
                        return null;
                    }
                }
                else if (!mergeItemStack(itemstack1, 4, 40, false))
                {
                    return null;
                }
            }
            else
            {
                if (!mergeItemStack(itemstack1, 4, 40, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
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

    class Ingredient extends Slot
    {
        public Ingredient(IInventory inventoryIn, int index, int xPosition, int yPosition)
        {
            super(inventoryIn, index, xPosition, yPosition);
        }

        public boolean isItemValid(ItemStack stack)
        {
            return stack != null && stack.getItem().isPotionIngredient(stack);
        }

        public int getSlotStackLimit()
        {
            return 64;
        }
    }

    static class Potion extends Slot
    {
        private final EntityPlayer player;

        public Potion(EntityPlayer playerIn, IInventory inventoryIn, int index, int xPosition, int yPosition)
        {
            super(inventoryIn, index, xPosition, yPosition);
            player = playerIn;
        }

        public boolean isItemValid(ItemStack stack)
        {
            return Potion.canHoldPotion(stack);
        }

        public int getSlotStackLimit()
        {
            return 1;
        }

        public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
        {
            if (stack.getItem() == Items.potionitem && stack.getMetadata() > 0)
            {
                player.triggerAchievement(AchievementList.potion);
            }

            super.onPickupFromSlot(playerIn, stack);
        }

        public static boolean canHoldPotion(ItemStack stack)
        {
            return stack != null && (stack.getItem() == Items.potionitem || stack.getItem() == Items.glass_bottle);
        }
    }
}
