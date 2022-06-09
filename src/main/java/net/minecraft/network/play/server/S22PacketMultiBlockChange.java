package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

public class S22PacketMultiBlockChange implements Packet<INetHandlerPlayClient>
{
    private ChunkCoordIntPair chunkPosCoord;
    private S22PacketMultiBlockChange.BlockUpdateData[] changedBlocks;

    public S22PacketMultiBlockChange()
    {
    }

    public S22PacketMultiBlockChange(int p_i45181_1_, short[] crammedPositionsIn, Chunk chunkIn)
    {
        chunkPosCoord = new ChunkCoordIntPair(chunkIn.xPosition, chunkIn.zPosition);
        changedBlocks = new S22PacketMultiBlockChange.BlockUpdateData[p_i45181_1_];

        for (int i = 0; i < changedBlocks.length; ++i)
        {
            changedBlocks[i] = new S22PacketMultiBlockChange.BlockUpdateData(crammedPositionsIn[i], chunkIn);
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        chunkPosCoord = new ChunkCoordIntPair(buf.readInt(), buf.readInt());
        changedBlocks = new S22PacketMultiBlockChange.BlockUpdateData[buf.readVarIntFromBuffer()];

        for (int i = 0; i < changedBlocks.length; ++i)
        {
            changedBlocks[i] = new S22PacketMultiBlockChange.BlockUpdateData(buf.readShort(), (IBlockState)Block.BLOCK_STATE_IDS.getByValue(buf.readVarIntFromBuffer()));
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeInt(chunkPosCoord.chunkXPos);
        buf.writeInt(chunkPosCoord.chunkZPos);
        buf.writeVarIntToBuffer(changedBlocks.length);

        for (S22PacketMultiBlockChange.BlockUpdateData s22packetmultiblockchange$blockupdatedata : changedBlocks)
        {
            buf.writeShort(s22packetmultiblockchange$blockupdatedata.func_180089_b());
            buf.writeVarIntToBuffer(Block.BLOCK_STATE_IDS.get(s22packetmultiblockchange$blockupdatedata.getBlockState()));
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleMultiBlockChange(this);
    }

    public S22PacketMultiBlockChange.BlockUpdateData[] getChangedBlocks()
    {
        return changedBlocks;
    }

    public class BlockUpdateData
    {
        private final short chunkPosCrammed;
        private final IBlockState blockState;

        public BlockUpdateData(short p_i45984_2_, IBlockState state)
        {
            chunkPosCrammed = p_i45984_2_;
            blockState = state;
        }

        public BlockUpdateData(short p_i45985_2_, Chunk chunkIn)
        {
            chunkPosCrammed = p_i45985_2_;
            blockState = chunkIn.getBlockState(getPos());
        }

        public BlockPos getPos()
        {
            return new BlockPos(chunkPosCoord.getBlock(chunkPosCrammed >> 12 & 15, chunkPosCrammed & 255, chunkPosCrammed >> 8 & 15));
        }

        public short func_180089_b()
        {
            return chunkPosCrammed;
        }

        public IBlockState getBlockState()
        {
            return blockState;
        }
    }
}
