package net.minecraft.tileentity;

import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IInteractionObject;

public class TileEntityEnchantmentTable extends TileEntity implements ITickable, IInteractionObject
{
    public int tickCount;
    public float pageFlip;
    public float pageFlipPrev;
    public float field_145932_k;
    public float field_145929_l;
    public float bookSpread;
    public float bookSpreadPrev;
    public float bookRotation;
    public float bookRotationPrev;
    public float field_145924_q;
    private static final Random rand = new Random();
    private String customName;

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (hasCustomName())
        {
            compound.setString("CustomName", customName);
        }
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey("CustomName", 8))
        {
            customName = compound.getString("CustomName");
        }
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        bookSpreadPrev = bookSpread;
        bookRotationPrev = bookRotation;
        EntityPlayer entityplayer = worldObj.getClosestPlayer((float) pos.getX() + 0.5F, (float) pos.getY() + 0.5F, (float) pos.getZ() + 0.5F, 3.0D);

        if (entityplayer != null)
        {
            double d0 = entityplayer.posX - (double)((float) pos.getX() + 0.5F);
            double d1 = entityplayer.posZ - (double)((float) pos.getZ() + 0.5F);
            field_145924_q = (float)MathHelper.func_181159_b(d1, d0);
            bookSpread += 0.1F;

            if (bookSpread < 0.5F || TileEntityEnchantmentTable.rand.nextInt(40) == 0)
            {
                float f1 = field_145932_k;

                while (true)
                {
                    field_145932_k += (float)(TileEntityEnchantmentTable.rand.nextInt(4) - TileEntityEnchantmentTable.rand.nextInt(4));

                    if (f1 != field_145932_k)
                    {
                        break;
                    }
                }
            }
        }
        else
        {
            field_145924_q += 0.02F;
            bookSpread -= 0.1F;
        }

        while (bookRotation >= (float)Math.PI)
        {
            bookRotation -= ((float)Math.PI * 2F);
        }

        while (bookRotation < -(float)Math.PI)
        {
            bookRotation += ((float)Math.PI * 2F);
        }

        while (field_145924_q >= (float)Math.PI)
        {
            field_145924_q -= ((float)Math.PI * 2F);
        }

        while (field_145924_q < -(float)Math.PI)
        {
            field_145924_q += ((float)Math.PI * 2F);
        }

        float f2;

        for (f2 = field_145924_q - bookRotation; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F))
        {
        }

        while (f2 < -(float)Math.PI)
        {
            f2 += ((float)Math.PI * 2F);
        }

        bookRotation += f2 * 0.4F;
        bookSpread = MathHelper.clamp_float(bookSpread, 0.0F, 1.0F);
        ++tickCount;
        pageFlipPrev = pageFlip;
        float f = (field_145932_k - pageFlip) * 0.4F;
        float f3 = 0.2F;
        f = MathHelper.clamp_float(f, -f3, f3);
        field_145929_l += (f - field_145929_l) * 0.9F;
        pageFlip += field_145929_l;
    }

    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
    public String getName()
    {
        return hasCustomName() ? customName : "container.enchant";
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        return customName != null && customName.length() > 0;
    }

    public void setCustomName(String customNameIn)
    {
        customName = customNameIn;
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public IChatComponent getDisplayName()
    {
        return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName(), new Object[0]);
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerEnchantment(playerInventory, worldObj, pos);
    }

    public String getGuiID()
    {
        return "minecraft:enchanting_table";
    }
}
