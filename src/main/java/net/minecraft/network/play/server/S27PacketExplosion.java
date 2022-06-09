package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class S27PacketExplosion implements Packet<INetHandlerPlayClient>
{
    private double posX;
    private double posY;
    private double posZ;
    private float strength;
    private List<BlockPos> affectedBlockPositions;
    private float field_149152_f;
    private float field_149153_g;
    private float field_149159_h;

    public S27PacketExplosion()
    {
    }

    public S27PacketExplosion(double p_i45193_1_, double y, double z, float strengthIn, List<BlockPos> affectedBlocksIn, Vec3 p_i45193_9_)
    {
        posX = p_i45193_1_;
        posY = y;
        posZ = z;
        strength = strengthIn;
        affectedBlockPositions = Lists.newArrayList(affectedBlocksIn);

        if (p_i45193_9_ != null)
        {
            field_149152_f = (float)p_i45193_9_.xCoord;
            field_149153_g = (float)p_i45193_9_.yCoord;
            field_149159_h = (float)p_i45193_9_.zCoord;
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        posX = buf.readFloat();
        posY = buf.readFloat();
        posZ = buf.readFloat();
        strength = buf.readFloat();
        int i = buf.readInt();
        affectedBlockPositions = Lists.newArrayListWithCapacity(i);
        int j = (int) posX;
        int k = (int) posY;
        int l = (int) posZ;

        for (int i1 = 0; i1 < i; ++i1)
        {
            int j1 = buf.readByte() + j;
            int k1 = buf.readByte() + k;
            int l1 = buf.readByte() + l;
            affectedBlockPositions.add(new BlockPos(j1, k1, l1));
        }

        field_149152_f = buf.readFloat();
        field_149153_g = buf.readFloat();
        field_149159_h = buf.readFloat();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeFloat((float) posX);
        buf.writeFloat((float) posY);
        buf.writeFloat((float) posZ);
        buf.writeFloat(strength);
        buf.writeInt(affectedBlockPositions.size());
        int i = (int) posX;
        int j = (int) posY;
        int k = (int) posZ;

        for (BlockPos blockpos : affectedBlockPositions)
        {
            int l = blockpos.getX() - i;
            int i1 = blockpos.getY() - j;
            int j1 = blockpos.getZ() - k;
            buf.writeByte(l);
            buf.writeByte(i1);
            buf.writeByte(j1);
        }

        buf.writeFloat(field_149152_f);
        buf.writeFloat(field_149153_g);
        buf.writeFloat(field_149159_h);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleExplosion(this);
    }

    public float func_149149_c()
    {
        return field_149152_f;
    }

    public float func_149144_d()
    {
        return field_149153_g;
    }

    public float func_149147_e()
    {
        return field_149159_h;
    }

    public double getX()
    {
        return posX;
    }

    public double getY()
    {
        return posY;
    }

    public double getZ()
    {
        return posZ;
    }

    public float getStrength()
    {
        return strength;
    }

    public List<BlockPos> getAffectedBlockPositions()
    {
        return affectedBlockPositions;
    }
}
