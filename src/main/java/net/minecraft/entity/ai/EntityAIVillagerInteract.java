package net.minecraft.entity.ai;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

public class EntityAIVillagerInteract extends EntityAIWatchClosest2
{
    /** The delay before the villager throws an itemstack (in ticks) */
    private int interactionDelay;
    private final EntityVillager villager;

    public EntityAIVillagerInteract(EntityVillager villagerIn)
    {
        super(villagerIn, EntityVillager.class, 3.0F, 0.02F);
        villager = villagerIn;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        super.startExecuting();

        if (villager.canAbondonItems() && closestEntity instanceof EntityVillager && ((EntityVillager) closestEntity).func_175557_cr())
        {
            interactionDelay = 10;
        }
        else
        {
            interactionDelay = 0;
        }
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        super.updateTask();

        if (interactionDelay > 0)
        {
            --interactionDelay;

            if (interactionDelay == 0)
            {
                InventoryBasic inventorybasic = villager.getVillagerInventory();

                for (int i = 0; i < inventorybasic.getSizeInventory(); ++i)
                {
                    ItemStack itemstack = inventorybasic.getStackInSlot(i);
                    ItemStack itemstack1 = null;

                    if (itemstack != null)
                    {
                        Item item = itemstack.getItem();

                        if ((item == Items.bread || item == Items.potato || item == Items.carrot) && itemstack.stackSize > 3)
                        {
                            int l = itemstack.stackSize / 2;
                            itemstack.stackSize -= l;
                            itemstack1 = new ItemStack(item, l, itemstack.getMetadata());
                        }
                        else if (item == Items.wheat && itemstack.stackSize > 5)
                        {
                            int j = itemstack.stackSize / 2 / 3 * 3;
                            int k = j / 3;
                            itemstack.stackSize -= j;
                            itemstack1 = new ItemStack(Items.bread, k, 0);
                        }

                        if (itemstack.stackSize <= 0)
                        {
                            inventorybasic.setInventorySlotContents(i, null);
                        }
                    }

                    if (itemstack1 != null)
                    {
                        double d0 = villager.posY - 0.30000001192092896D + (double) villager.getEyeHeight();
                        EntityItem entityitem = new EntityItem(villager.worldObj, villager.posX, d0, villager.posZ, itemstack1);
                        float f = 0.3F;
                        float f1 = villager.rotationYawHead;
                        float f2 = villager.rotationPitch;
                        entityitem.motionX = -MathHelper.sin(f1 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI) * f;
                        entityitem.motionZ = MathHelper.cos(f1 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI) * f;
                        entityitem.motionY = -MathHelper.sin(f2 / 180.0F * (float)Math.PI) * f + 0.1F;
                        entityitem.setDefaultPickupDelay();
                        villager.worldObj.spawnEntityInWorld(entityitem);
                        break;
                    }
                }
            }
        }
    }
}
