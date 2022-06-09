package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S1BPacketEntityAttach implements Packet<INetHandlerPlayClient>
{
    private int leash;
    private int entityId;
    private int vehicleEntityId;

    public S1BPacketEntityAttach()
    {
    }

    public S1BPacketEntityAttach(int leashIn, Entity entityIn, Entity vehicle)
    {
        leash = leashIn;
        entityId = entityIn.getEntityId();
        vehicleEntityId = vehicle != null ? vehicle.getEntityId() : -1;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityId = buf.readInt();
        vehicleEntityId = buf.readInt();
        leash = buf.readUnsignedByte();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeInt(entityId);
        buf.writeInt(vehicleEntityId);
        buf.writeByte(leash);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleEntityAttach(this);
    }

    public int getLeash()
    {
        return leash;
    }

    public int getEntityId()
    {
        return entityId;
    }

    public int getVehicleEntityId()
    {
        return vehicleEntityId;
    }
}
