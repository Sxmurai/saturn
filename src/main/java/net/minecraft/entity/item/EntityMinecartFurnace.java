package net.minecraft.entity.item;

import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityMinecartFurnace extends EntityMinecart
{
    private int fuel;
    public double pushX;
    public double pushZ;

    public EntityMinecartFurnace(World worldIn)
    {
        super(worldIn);
    }

    public EntityMinecartFurnace(World worldIn, double p_i1719_2_, double p_i1719_4_, double p_i1719_6_)
    {
        super(worldIn, p_i1719_2_, p_i1719_4_, p_i1719_6_);
    }

    public EntityMinecart.EnumMinecartType getMinecartType()
    {
        return EntityMinecart.EnumMinecartType.FURNACE;
    }

    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(16, new Byte((byte)0));
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (fuel > 0)
        {
            --fuel;
        }

        if (fuel <= 0)
        {
            pushX = pushZ = 0.0D;
        }

        setMinecartPowered(fuel > 0);

        if (isMinecartPowered() && rand.nextInt(4) == 0)
        {
            worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY + 0.8D, posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    /**
     * Get's the maximum speed for a minecart
     */
    protected double getMaximumSpeed()
    {
        return 0.2D;
    }

    public void killMinecart(DamageSource p_94095_1_)
    {
        super.killMinecart(p_94095_1_);

        if (!p_94095_1_.isExplosion() && worldObj.getGameRules().getBoolean("doEntityDrops"))
        {
            entityDropItem(new ItemStack(Blocks.furnace, 1), 0.0F);
        }
    }

    protected void func_180460_a(BlockPos p_180460_1_, IBlockState p_180460_2_)
    {
        super.func_180460_a(p_180460_1_, p_180460_2_);
        double d0 = pushX * pushX + pushZ * pushZ;

        if (d0 > 1.0E-4D && motionX * motionX + motionZ * motionZ > 0.001D)
        {
            d0 = MathHelper.sqrt_double(d0);
            pushX /= d0;
            pushZ /= d0;

            if (pushX * motionX + pushZ * motionZ < 0.0D)
            {
                pushX = 0.0D;
                pushZ = 0.0D;
            }
            else
            {
                double d1 = d0 / getMaximumSpeed();
                pushX *= d1;
                pushZ *= d1;
            }
        }
    }

    protected void applyDrag()
    {
        double d0 = pushX * pushX + pushZ * pushZ;

        if (d0 > 1.0E-4D)
        {
            d0 = MathHelper.sqrt_double(d0);
            pushX /= d0;
            pushZ /= d0;
            double d1 = 1.0D;
            motionX *= 0.800000011920929D;
            motionY *= 0.0D;
            motionZ *= 0.800000011920929D;
            motionX += pushX * d1;
            motionZ += pushZ * d1;
        }
        else
        {
            motionX *= 0.9800000190734863D;
            motionY *= 0.0D;
            motionZ *= 0.9800000190734863D;
        }

        super.applyDrag();
    }

    /**
     * First layer of player interaction
     */
    public boolean interactFirst(EntityPlayer playerIn)
    {
        ItemStack itemstack = playerIn.inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() == Items.coal)
        {
            if (!playerIn.capabilities.isCreativeMode && --itemstack.stackSize == 0)
            {
                playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, null);
            }

            fuel += 3600;
        }

        pushX = posX - playerIn.posX;
        pushZ = posZ - playerIn.posZ;
        return true;
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setDouble("PushX", pushX);
        tagCompound.setDouble("PushZ", pushZ);
        tagCompound.setShort("Fuel", (short) fuel);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        pushX = tagCompund.getDouble("PushX");
        pushZ = tagCompund.getDouble("PushZ");
        fuel = tagCompund.getShort("Fuel");
    }

    protected boolean isMinecartPowered()
    {
        return (dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    protected void setMinecartPowered(boolean p_94107_1_)
    {
        if (p_94107_1_)
        {
            dataWatcher.updateObject(16, Byte.valueOf((byte)(dataWatcher.getWatchableObjectByte(16) | 1)));
        }
        else
        {
            dataWatcher.updateObject(16, Byte.valueOf((byte)(dataWatcher.getWatchableObjectByte(16) & -2)));
        }
    }

    public IBlockState getDefaultDisplayTile()
    {
        return (isMinecartPowered() ? Blocks.lit_furnace : Blocks.furnace).getDefaultState().withProperty(BlockFurnace.FACING, EnumFacing.NORTH);
    }
}
