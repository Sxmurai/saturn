package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityMinecartEmpty extends EntityMinecart
{
    public EntityMinecartEmpty(World worldIn)
    {
        super(worldIn);
    }

    public EntityMinecartEmpty(World worldIn, double p_i1723_2_, double p_i1723_4_, double p_i1723_6_)
    {
        super(worldIn, p_i1723_2_, p_i1723_4_, p_i1723_6_);
    }

    /**
     * First layer of player interaction
     */
    public boolean interactFirst(EntityPlayer playerIn)
    {
        if (riddenByEntity != null && riddenByEntity instanceof EntityPlayer && riddenByEntity != playerIn)
        {
            return true;
        }
        else if (riddenByEntity != null && riddenByEntity != playerIn)
        {
            return false;
        }
        else
        {
            if (!worldObj.isRemote)
            {
                playerIn.mountEntity(this);
            }

            return true;
        }
    }

    /**
     * Called every tick the minecart is on an activator rail. Args: x, y, z, is the rail receiving power
     */
    public void onActivatorRailPass(int x, int y, int z, boolean receivingPower)
    {
        if (receivingPower)
        {
            if (riddenByEntity != null)
            {
                riddenByEntity.mountEntity(null);
            }

            if (getRollingAmplitude() == 0)
            {
                setRollingDirection(-getRollingDirection());
                setRollingAmplitude(10);
                setDamage(50.0F);
                setBeenAttacked();
            }
        }
    }

    public EntityMinecart.EnumMinecartType getMinecartType()
    {
        return EntityMinecart.EnumMinecartType.RIDEABLE;
    }
}
