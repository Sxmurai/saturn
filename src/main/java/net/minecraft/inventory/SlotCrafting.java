package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.stats.AchievementList;

public class SlotCrafting extends Slot
{
    /** The craft matrix inventory linked to this result slot. */
    private final InventoryCrafting craftMatrix;

    /** The player that is using the GUI where this slot resides. */
    private final EntityPlayer thePlayer;

    /**
     * The number of items that have been crafted so far. Gets passed to ItemStack.onCrafting before being reset.
     */
    private int amountCrafted;

    public SlotCrafting(EntityPlayer player, InventoryCrafting craftingInventory, IInventory p_i45790_3_, int slotIndex, int xPosition, int yPosition)
    {
        super(p_i45790_3_, slotIndex, xPosition, yPosition);
        thePlayer = player;
        craftMatrix = craftingInventory;
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    public boolean isItemValid(ItemStack stack)
    {
        return false;
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    public ItemStack decrStackSize(int amount)
    {
        if (getHasStack())
        {
            amountCrafted += Math.min(amount, getStack().stackSize);
        }

        return super.decrStackSize(amount);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     */
    protected void onCrafting(ItemStack stack, int amount)
    {
        amountCrafted += amount;
        onCrafting(stack);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
     */
    protected void onCrafting(ItemStack stack)
    {
        if (amountCrafted > 0)
        {
            stack.onCrafting(thePlayer.worldObj, thePlayer, amountCrafted);
        }

        amountCrafted = 0;

        if (stack.getItem() == Item.getItemFromBlock(Blocks.crafting_table))
        {
            thePlayer.triggerAchievement(AchievementList.buildWorkBench);
        }

        if (stack.getItem() instanceof ItemPickaxe)
        {
            thePlayer.triggerAchievement(AchievementList.buildPickaxe);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.furnace))
        {
            thePlayer.triggerAchievement(AchievementList.buildFurnace);
        }

        if (stack.getItem() instanceof ItemHoe)
        {
            thePlayer.triggerAchievement(AchievementList.buildHoe);
        }

        if (stack.getItem() == Items.bread)
        {
            thePlayer.triggerAchievement(AchievementList.makeBread);
        }

        if (stack.getItem() == Items.cake)
        {
            thePlayer.triggerAchievement(AchievementList.bakeCake);
        }

        if (stack.getItem() instanceof ItemPickaxe && ((ItemPickaxe)stack.getItem()).getToolMaterial() != Item.ToolMaterial.WOOD)
        {
            thePlayer.triggerAchievement(AchievementList.buildBetterPickaxe);
        }

        if (stack.getItem() instanceof ItemSword)
        {
            thePlayer.triggerAchievement(AchievementList.buildSword);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.enchanting_table))
        {
            thePlayer.triggerAchievement(AchievementList.enchantments);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.bookshelf))
        {
            thePlayer.triggerAchievement(AchievementList.bookcase);
        }

        if (stack.getItem() == Items.golden_apple && stack.getMetadata() == 1)
        {
            thePlayer.triggerAchievement(AchievementList.overpowered);
        }
    }

    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
    {
        onCrafting(stack);
        ItemStack[] aitemstack = CraftingManager.getInstance().func_180303_b(craftMatrix, playerIn.worldObj);

        for (int i = 0; i < aitemstack.length; ++i)
        {
            ItemStack itemstack = craftMatrix.getStackInSlot(i);
            ItemStack itemstack1 = aitemstack[i];

            if (itemstack != null)
            {
                craftMatrix.decrStackSize(i, 1);
            }

            if (itemstack1 != null)
            {
                if (craftMatrix.getStackInSlot(i) == null)
                {
                    craftMatrix.setInventorySlotContents(i, itemstack1);
                }
                else if (!thePlayer.inventory.addItemStackToInventory(itemstack1))
                {
                    thePlayer.dropPlayerItemWithRandomChoice(itemstack1, false);
                }
            }
        }
    }
}
