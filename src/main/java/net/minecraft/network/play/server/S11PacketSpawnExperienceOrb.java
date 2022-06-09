package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.MathHelper;

public class S11PacketSpawnExperienceOrb implements Packet<INetHandlerPlayClient>
{
    private int entityID;
    private int posX;
    private int posY;
    private int posZ;
    private int xpValue;

    public S11PacketSpawnExperienceOrb()
    {
    }

    public S11PacketSpawnExperienceOrb(EntityXPOrb xpOrb)
    {
        entityID = xpOrb.getEntityId();
        posX = MathHelper.floor_double(xpOrb.posX * 32.0D);
        posY = MathHelper.floor_double(xpOrb.posY * 32.0D);
        posZ = MathHelper.floor_double(xpOrb.posZ * 32.0D);
        xpValue = xpOrb.getXpValue();
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityID = buf.readVarIntFromBuffer();
        posX = buf.readInt();
        posY = buf.readInt();
        posZ = buf.readInt();
        xpValue = buf.readShort();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(entityID);
        buf.writeInt(posX);
        buf.writeInt(posY);
        buf.writeInt(posZ);
        buf.writeShort(xpValue);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleSpawnExperienceOrb(this);
    }

    public int getEntityID()
    {
        return entityID;
    }

    public int getX()
    {
        return posX;
    }

    public int getY()
    {
        return posY;
    }

    public int getZ()
    {
        return posZ;
    }

    public int getXPValue()
    {
        return xpValue;
    }
}
