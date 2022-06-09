package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S08PacketPlayerPosLook implements Packet<INetHandlerPlayClient>
{
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private Set<S08PacketPlayerPosLook.EnumFlags> field_179835_f;

    public S08PacketPlayerPosLook()
    {
    }

    public S08PacketPlayerPosLook(double xIn, double yIn, double zIn, float yawIn, float pitchIn, Set<S08PacketPlayerPosLook.EnumFlags> p_i45993_9_)
    {
        x = xIn;
        y = yIn;
        z = zIn;
        yaw = yawIn;
        pitch = pitchIn;
        field_179835_f = p_i45993_9_;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        field_179835_f = S08PacketPlayerPosLook.EnumFlags.func_180053_a(buf.readUnsignedByte());
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeByte(S08PacketPlayerPosLook.EnumFlags.func_180056_a(field_179835_f));
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handlePlayerPosLook(this);
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public float getYaw()
    {
        return yaw;
    }

    public float getPitch()
    {
        return pitch;
    }

    public Set<S08PacketPlayerPosLook.EnumFlags> func_179834_f()
    {
        return field_179835_f;
    }

    public static enum EnumFlags
    {
        X(0),
        Y(1),
        Z(2),
        Y_ROT(3),
        X_ROT(4);

        private final int field_180058_f;

        private EnumFlags(int p_i45992_3_)
        {
            field_180058_f = p_i45992_3_;
        }

        private int func_180055_a()
        {
            return 1 << field_180058_f;
        }

        private boolean func_180054_b(int p_180054_1_)
        {
            return (p_180054_1_ & func_180055_a()) == func_180055_a();
        }

        public static Set<S08PacketPlayerPosLook.EnumFlags> func_180053_a(int p_180053_0_)
        {
            Set<S08PacketPlayerPosLook.EnumFlags> set = EnumSet.noneOf(S08PacketPlayerPosLook.EnumFlags.class);

            for (S08PacketPlayerPosLook.EnumFlags s08packetplayerposlook$enumflags : EnumFlags.values())
            {
                if (s08packetplayerposlook$enumflags.func_180054_b(p_180053_0_))
                {
                    set.add(s08packetplayerposlook$enumflags);
                }
            }

            return set;
        }

        public static int func_180056_a(Set<S08PacketPlayerPosLook.EnumFlags> p_180056_0_)
        {
            int i = 0;

            for (S08PacketPlayerPosLook.EnumFlags s08packetplayerposlook$enumflags : p_180056_0_)
            {
                i |= s08packetplayerposlook$enumflags.func_180055_a();
            }

            return i;
        }
    }
}
