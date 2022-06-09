package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;

public class S19PacketEntityHeadLook implements Packet<INetHandlerPlayClient>
{
    private int entityId;
    private byte yaw;

    public S19PacketEntityHeadLook()
    {
    }

    public S19PacketEntityHeadLook(Entity entityIn, byte p_i45214_2_)
    {
        entityId = entityIn.getEntityId();
        yaw = p_i45214_2_;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityId = buf.readVarIntFromBuffer();
        yaw = buf.readByte();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(entityId);
        buf.writeByte(yaw);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleEntityHeadLook(this);
    }

    public Entity getEntity(World worldIn)
    {
        return worldIn.getEntityByID(entityId);
    }

    public byte getYaw()
    {
        return yaw;
    }
}
