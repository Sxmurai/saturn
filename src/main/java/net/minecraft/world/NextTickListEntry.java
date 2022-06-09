package net.minecraft.world;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

public class NextTickListEntry implements Comparable<NextTickListEntry>
{
    /** The id number for the next tick entry */
    private static long nextTickEntryID;
    private final Block block;
    public final BlockPos position;

    /** Time this tick is scheduled to occur at */
    public long scheduledTime;
    public int priority;

    /** The id of the tick entry */
    private final long tickEntryID;

    public NextTickListEntry(BlockPos p_i45745_1_, Block p_i45745_2_)
    {
        tickEntryID = NextTickListEntry.nextTickEntryID++;
        position = p_i45745_1_;
        block = p_i45745_2_;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (!(p_equals_1_ instanceof NextTickListEntry))
        {
            return false;
        }
        else
        {
            NextTickListEntry nextticklistentry = (NextTickListEntry)p_equals_1_;
            return position.equals(nextticklistentry.position) && Block.isEqualTo(block, nextticklistentry.block);
        }
    }

    public int hashCode()
    {
        return position.hashCode();
    }

    /**
     * Sets the scheduled time for this tick entry
     */
    public NextTickListEntry setScheduledTime(long p_77176_1_)
    {
        scheduledTime = p_77176_1_;
        return this;
    }

    public void setPriority(int p_82753_1_)
    {
        priority = p_82753_1_;
    }

    public int compareTo(NextTickListEntry p_compareTo_1_)
    {
        return scheduledTime < p_compareTo_1_.scheduledTime ? -1 : (scheduledTime > p_compareTo_1_.scheduledTime ? 1 : (priority != p_compareTo_1_.priority ? priority - p_compareTo_1_.priority : (tickEntryID < p_compareTo_1_.tickEntryID ? -1 : (tickEntryID > p_compareTo_1_.tickEntryID ? 1 : 0))));
    }

    public String toString()
    {
        return Block.getIdFromBlock(block) + ": " + position + ", " + scheduledTime + ", " + priority + ", " + tickEntryID;
    }

    public Block getBlock()
    {
        return block;
    }
}
