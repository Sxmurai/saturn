package net.minecraft.tileentity;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;

public class TileEntityFlowerPot extends TileEntity
{
    private Item flowerPotItem;
    private int flowerPotData;

    public TileEntityFlowerPot()
    {
    }

    public TileEntityFlowerPot(Item potItem, int potData)
    {
        flowerPotItem = potItem;
        flowerPotData = potData;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        ResourceLocation resourcelocation = Item.itemRegistry.getNameForObject(flowerPotItem);
        compound.setString("Item", resourcelocation == null ? "" : resourcelocation.toString());
        compound.setInteger("Data", flowerPotData);
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey("Item", 8))
        {
            flowerPotItem = Item.getByNameOrId(compound.getString("Item"));
        }
        else
        {
            flowerPotItem = Item.getItemById(compound.getInteger("Item"));
        }

        flowerPotData = compound.getInteger("Data");
    }

    /**
     * Allows for a specialized description packet to be created. This is often used to sync tile entity data from the
     * server to the client easily. For example this is used by signs to synchronise the text to be displayed.
     */
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        writeToNBT(nbttagcompound);
        nbttagcompound.removeTag("Item");
        nbttagcompound.setInteger("Item", Item.getIdFromItem(flowerPotItem));
        return new S35PacketUpdateTileEntity(pos, 5, nbttagcompound);
    }

    public void setFlowerPotData(Item potItem, int potData)
    {
        flowerPotItem = potItem;
        flowerPotData = potData;
    }

    public Item getFlowerPotItem()
    {
        return flowerPotItem;
    }

    public int getFlowerPotData()
    {
        return flowerPotData;
    }
}
