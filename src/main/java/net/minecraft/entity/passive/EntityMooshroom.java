package net.minecraft.entity.passive;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityMooshroom extends EntityCow
{
    public EntityMooshroom(World worldIn)
    {
        super(worldIn);
        setSize(0.9F, 1.3F);
        spawnableBlock = Blocks.mycelium;
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean interact(EntityPlayer player)
    {
        ItemStack itemstack = player.inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() == Items.bowl && getGrowingAge() >= 0)
        {
            if (itemstack.stackSize == 1)
            {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(Items.mushroom_stew));
                return true;
            }

            if (player.inventory.addItemStackToInventory(new ItemStack(Items.mushroom_stew)) && !player.capabilities.isCreativeMode)
            {
                player.inventory.decrStackSize(player.inventory.currentItem, 1);
                return true;
            }
        }

        if (itemstack != null && itemstack.getItem() == Items.shears && getGrowingAge() >= 0)
        {
            setDead();
            worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, posX, posY + (double)(height / 2.0F), posZ, 0.0D, 0.0D, 0.0D);

            if (!worldObj.isRemote)
            {
                EntityCow entitycow = new EntityCow(worldObj);
                entitycow.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
                entitycow.setHealth(getHealth());
                entitycow.renderYawOffset = renderYawOffset;

                if (hasCustomName())
                {
                    entitycow.setCustomNameTag(getCustomNameTag());
                }

                worldObj.spawnEntityInWorld(entitycow);

                for (int i = 0; i < 5; ++i)
                {
                    worldObj.spawnEntityInWorld(new EntityItem(worldObj, posX, posY + (double) height, posZ, new ItemStack(Blocks.red_mushroom)));
                }

                itemstack.damageItem(1, player);
                playSound("mob.sheep.shear", 1.0F, 1.0F);
            }

            return true;
        }
        else
        {
            return super.interact(player);
        }
    }

    public EntityMooshroom createChild(EntityAgeable ageable)
    {
        return new EntityMooshroom(worldObj);
    }
}
