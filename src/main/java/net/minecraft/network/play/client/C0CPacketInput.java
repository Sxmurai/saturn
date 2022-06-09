package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class C0CPacketInput implements Packet<INetHandlerPlayServer>
{
    /** Positive for left strafe, negative for right */
    private float strafeSpeed;

    /** Positive for forward, negative for backward */
    private float forwardSpeed;
    private boolean jumping;
    private boolean sneaking;

    public C0CPacketInput()
    {
    }

    public C0CPacketInput(float strafeSpeed, float forwardSpeed, boolean jumping, boolean sneaking)
    {
        this.strafeSpeed = strafeSpeed;
        this.forwardSpeed = forwardSpeed;
        this.jumping = jumping;
        this.sneaking = sneaking;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        strafeSpeed = buf.readFloat();
        forwardSpeed = buf.readFloat();
        byte b0 = buf.readByte();
        jumping = (b0 & 1) > 0;
        sneaking = (b0 & 2) > 0;
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeFloat(strafeSpeed);
        buf.writeFloat(forwardSpeed);
        byte b0 = 0;

        if (jumping)
        {
            b0 = (byte)(b0 | 1);
        }

        if (sneaking)
        {
            b0 = (byte)(b0 | 2);
        }

        buf.writeByte(b0);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processInput(this);
    }

    public float getStrafeSpeed()
    {
        return strafeSpeed;
    }

    public float getForwardSpeed()
    {
        return forwardSpeed;
    }

    public boolean isJumping()
    {
        return jumping;
    }

    public boolean isSneaking()
    {
        return sneaking;
    }
}
