package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S12PacketEntityVelocity implements Packet<INetHandlerPlayClient>
{
    private int entityID;
    private int motionX;
    private int motionY;
    private int motionZ;

    public S12PacketEntityVelocity()
    {
    }

    public S12PacketEntityVelocity(Entity entityIn)
    {
        this(entityIn.getEntityId(), entityIn.motionX, entityIn.motionY, entityIn.motionZ);
    }

    public S12PacketEntityVelocity(int entityIDIn, double motionXIn, double motionYIn, double motionZIn)
    {
        entityID = entityIDIn;
        double d0 = 3.9D;

        if (motionXIn < -d0)
        {
            motionXIn = -d0;
        }

        if (motionYIn < -d0)
        {
            motionYIn = -d0;
        }

        if (motionZIn < -d0)
        {
            motionZIn = -d0;
        }

        if (motionXIn > d0)
        {
            motionXIn = d0;
        }

        if (motionYIn > d0)
        {
            motionYIn = d0;
        }

        if (motionZIn > d0)
        {
            motionZIn = d0;
        }

        motionX = (int)(motionXIn * 8000.0D);
        motionY = (int)(motionYIn * 8000.0D);
        motionZ = (int)(motionZIn * 8000.0D);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityID = buf.readVarIntFromBuffer();
        motionX = buf.readShort();
        motionY = buf.readShort();
        motionZ = buf.readShort();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(entityID);
        buf.writeShort(motionX);
        buf.writeShort(motionY);
        buf.writeShort(motionZ);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleEntityVelocity(this);
    }

    public int getEntityID()
    {
        return entityID;
    }

    public int getMotionX()
    {
        return motionX;
    }

    public int getMotionY()
    {
        return motionY;
    }

    public int getMotionZ()
    {
        return motionZ;
    }
}
