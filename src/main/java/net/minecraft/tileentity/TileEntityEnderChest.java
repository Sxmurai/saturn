package net.minecraft.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ITickable;

public class TileEntityEnderChest extends TileEntity implements ITickable
{
    public float lidAngle;

    /** The angle of the ender chest lid last tick */
    public float prevLidAngle;
    public int numPlayersUsing;
    private int ticksSinceSync;

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        if (++ticksSinceSync % 20 * 4 == 0)
        {
            worldObj.addBlockEvent(pos, Blocks.ender_chest, 1, numPlayersUsing);
        }

        prevLidAngle = lidAngle;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        float f = 0.1F;

        if (numPlayersUsing > 0 && lidAngle == 0.0F)
        {
            double d0 = (double)i + 0.5D;
            double d1 = (double)k + 0.5D;
            worldObj.playSoundEffect(d0, (double)j + 0.5D, d1, "random.chestopen", 0.5F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (numPlayersUsing == 0 && lidAngle > 0.0F || numPlayersUsing > 0 && lidAngle < 1.0F)
        {
            float f2 = lidAngle;

            if (numPlayersUsing > 0)
            {
                lidAngle += f;
            }
            else
            {
                lidAngle -= f;
            }

            if (lidAngle > 1.0F)
            {
                lidAngle = 1.0F;
            }

            float f1 = 0.5F;

            if (lidAngle < f1 && f2 >= f1)
            {
                double d3 = (double)i + 0.5D;
                double d2 = (double)k + 0.5D;
                worldObj.playSoundEffect(d3, (double)j + 0.5D, d2, "random.chestclosed", 0.5F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
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

    /**
     * invalidates a tile entity
     */
    public void invalidate()
    {
        updateContainingBlockInfo();
        super.invalidate();
    }

    public void openChest()
    {
        ++numPlayersUsing;
        worldObj.addBlockEvent(pos, Blocks.ender_chest, 1, numPlayersUsing);
    }

    public void closeChest()
    {
        --numPlayersUsing;
        worldObj.addBlockEvent(pos, Blocks.ender_chest, 1, numPlayersUsing);
    }

    public boolean canBeUsed(EntityPlayer p_145971_1_)
    {
        return worldObj.getTileEntity(pos) == this && p_145971_1_.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }
}
