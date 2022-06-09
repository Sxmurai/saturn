package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.MathHelper;

public class S2CPacketSpawnGlobalEntity implements Packet<INetHandlerPlayClient>
{
    private int entityId;
    private int x;
    private int y;
    private int z;
    private int type;

    public S2CPacketSpawnGlobalEntity()
    {
    }

    public S2CPacketSpawnGlobalEntity(Entity entityIn)
    {
        entityId = entityIn.getEntityId();
        x = MathHelper.floor_double(entityIn.posX * 32.0D);
        y = MathHelper.floor_double(entityIn.posY * 32.0D);
        z = MathHelper.floor_double(entityIn.posZ * 32.0D);

        if (entityIn instanceof EntityLightningBolt)
        {
            type = 1;
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityId = buf.readVarIntFromBuffer();
        type = buf.readByte();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(entityId);
        buf.writeByte(type);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleSpawnGlobalEntity(this);
    }

    public int func_149052_c()
    {
        return entityId;
    }

    public int func_149051_d()
    {
        return x;
    }

    public int func_149050_e()
    {
        return y;
    }

    public int func_149049_f()
    {
        return z;
    }

    public int func_149053_g()
    {
        return type;
    }
}
