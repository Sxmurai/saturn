package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntityAIHarvestFarmland extends EntityAIMoveToBlock
{
    /** Villager that is harvesting */
    private final EntityVillager theVillager;
    private boolean hasFarmItem;
    private boolean field_179503_e;
    private int field_179501_f;

    public EntityAIHarvestFarmland(EntityVillager theVillagerIn, double speedIn)
    {
        super(theVillagerIn, speedIn, 16);
        theVillager = theVillagerIn;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (runDelay <= 0)
        {
            if (!theVillager.worldObj.getGameRules().getBoolean("mobGriefing"))
            {
                return false;
            }

            field_179501_f = -1;
            hasFarmItem = theVillager.isFarmItemInInventory();
            field_179503_e = theVillager.func_175557_cr();
        }

        return super.shouldExecute();
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return field_179501_f >= 0 && super.continueExecuting();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        super.startExecuting();
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        super.resetTask();
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        super.updateTask();
        theVillager.getLookHelper().setLookPosition((double) destinationBlock.getX() + 0.5D, destinationBlock.getY() + 1, (double) destinationBlock.getZ() + 0.5D, 10.0F, (float) theVillager.getVerticalFaceSpeed());

        if (getIsAboveDestination())
        {
            World world = theVillager.worldObj;
            BlockPos blockpos = destinationBlock.up();
            IBlockState iblockstate = world.getBlockState(blockpos);
            Block block = iblockstate.getBlock();

            if (field_179501_f == 0 && block instanceof BlockCrops && iblockstate.getValue(BlockCrops.AGE).intValue() == 7)
            {
                world.destroyBlock(blockpos, true);
            }
            else if (field_179501_f == 1 && block == Blocks.air)
            {
                InventoryBasic inventorybasic = theVillager.getVillagerInventory();

                for (int i = 0; i < inventorybasic.getSizeInventory(); ++i)
                {
                    ItemStack itemstack = inventorybasic.getStackInSlot(i);
                    boolean flag = false;

                    if (itemstack != null)
                    {
                        if (itemstack.getItem() == Items.wheat_seeds)
                        {
                            world.setBlockState(blockpos, Blocks.wheat.getDefaultState(), 3);
                            flag = true;
                        }
                        else if (itemstack.getItem() == Items.potato)
                        {
                            world.setBlockState(blockpos, Blocks.potatoes.getDefaultState(), 3);
                            flag = true;
                        }
                        else if (itemstack.getItem() == Items.carrot)
                        {
                            world.setBlockState(blockpos, Blocks.carrots.getDefaultState(), 3);
                            flag = true;
                        }
                    }

                    if (flag)
                    {
                        --itemstack.stackSize;

                        if (itemstack.stackSize <= 0)
                        {
                            inventorybasic.setInventorySlotContents(i, null);
                        }

                        break;
                    }
                }
            }

            field_179501_f = -1;
            runDelay = 10;
        }
    }

    /**
     * Return true to set given position as destination
     */
    protected boolean shouldMoveTo(World worldIn, BlockPos pos)
    {
        Block block = worldIn.getBlockState(pos).getBlock();

        if (block == Blocks.farmland)
        {
            pos = pos.up();
            IBlockState iblockstate = worldIn.getBlockState(pos);
            block = iblockstate.getBlock();

            if (block instanceof BlockCrops && iblockstate.getValue(BlockCrops.AGE).intValue() == 7 && field_179503_e && (field_179501_f == 0 || field_179501_f < 0))
            {
                field_179501_f = 0;
                return true;
            }

            if (block == Blocks.air && hasFarmItem && (field_179501_f == 1 || field_179501_f < 0))
            {
                field_179501_f = 1;
                return true;
            }
        }

        return false;
    }
}
