package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class C0BPacketEntityAction implements Packet<INetHandlerPlayServer>
{
    private int entityID;
    private C0BPacketEntityAction.Action action;
    private int auxData;

    public C0BPacketEntityAction()
    {
    }

    public C0BPacketEntityAction(Entity entity, C0BPacketEntityAction.Action action)
    {
        this(entity, action, 0);
    }

    public C0BPacketEntityAction(Entity entity, C0BPacketEntityAction.Action action, int auxData)
    {
        entityID = entity.getEntityId();
        this.action = action;
        this.auxData = auxData;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        entityID = buf.readVarIntFromBuffer();
        action = buf.readEnumValue(Action.class);
        auxData = buf.readVarIntFromBuffer();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(entityID);
        buf.writeEnumValue(action);
        buf.writeVarIntToBuffer(auxData);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processEntityAction(this);
    }

    public C0BPacketEntityAction.Action getAction()
    {
        return action;
    }

    public int getAuxData()
    {
        return auxData;
    }

    public static enum Action
    {
        START_SNEAKING,
        STOP_SNEAKING,
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        RIDING_JUMP,
        OPEN_INVENTORY
    }
}
