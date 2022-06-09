package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;
import org.apache.commons.lang3.StringUtils;

public class C14PacketTabComplete implements Packet<INetHandlerPlayServer>
{
    private String message;
    private BlockPos targetBlock;

    public C14PacketTabComplete()
    {
    }

    public C14PacketTabComplete(String msg)
    {
        this(msg, null);
    }

    public C14PacketTabComplete(String msg, BlockPos target)
    {
        message = msg;
        targetBlock = target;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        message = buf.readStringFromBuffer(32767);
        boolean flag = buf.readBoolean();

        if (flag)
        {
            targetBlock = buf.readBlockPos();
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(StringUtils.substring(message, 0, 32767));
        boolean flag = targetBlock != null;
        buf.writeBoolean(flag);

        if (flag)
        {
            buf.writeBlockPos(targetBlock);
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processTabComplete(this);
    }

    public String getMessage()
    {
        return message;
    }

    public BlockPos getTargetBlock()
    {
        return targetBlock;
    }
}
