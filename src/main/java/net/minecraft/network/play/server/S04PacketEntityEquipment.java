package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S04PacketEntityEquipment implements Packet<INetHandlerPlayClient>
{
    private int entityID;
    private int equipmentSlot;
    private ItemStack itemStack;

    public S04PacketEntityEquipment()
    {
    }

    public S04PacketEntityEquipment(int entityIDIn, int p_i45221_2_, ItemStack itemStackIn)
    {
        entityID = entityIDIn;
        equipmentSlot = p_i45221_2_;
        itemStack = itemStackIn == null ? null : itemStackIn.copy();
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityID = buf.readVarIntFromBuffer();
        equipmentSlot = buf.readShort();
        itemStack = buf.readItemStackFromBuffer();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(entityID);
        buf.writeShort(equipmentSlot);
        buf.writeItemStackToBuffer(itemStack);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleEntityEquipment(this);
    }

    public ItemStack getItemStack()
    {
        return itemStack;
    }

    public int getEntityID()
    {
        return entityID;
    }

    public int getEquipmentSlot()
    {
        return equipmentSlot;
    }
}
