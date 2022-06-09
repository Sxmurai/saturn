package net.minecraft.village;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class VillageDoorInfo
{
    /** a block representing the door. Could be either upper or lower part */
    private final BlockPos doorBlockPos;
    private final BlockPos insideBlock;

    /** the inside direction is where can see less sky */
    private final EnumFacing insideDirection;
    private int lastActivityTimestamp;
    private boolean isDetachedFromVillageFlag;
    private int doorOpeningRestrictionCounter;

    public VillageDoorInfo(BlockPos p_i45871_1_, int p_i45871_2_, int p_i45871_3_, int p_i45871_4_)
    {
        this(p_i45871_1_, VillageDoorInfo.getFaceDirection(p_i45871_2_, p_i45871_3_), p_i45871_4_);
    }

    private static EnumFacing getFaceDirection(int deltaX, int deltaZ)
    {
        return deltaX < 0 ? EnumFacing.WEST : (deltaX > 0 ? EnumFacing.EAST : (deltaZ < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH));
    }

    public VillageDoorInfo(BlockPos p_i45872_1_, EnumFacing p_i45872_2_, int p_i45872_3_)
    {
        doorBlockPos = p_i45872_1_;
        insideDirection = p_i45872_2_;
        insideBlock = p_i45872_1_.offset(p_i45872_2_, 2);
        lastActivityTimestamp = p_i45872_3_;
    }

    /**
     * Returns the squared distance between this door and the given coordinate.
     */
    public int getDistanceSquared(int p_75474_1_, int p_75474_2_, int p_75474_3_)
    {
        return (int) doorBlockPos.distanceSq(p_75474_1_, p_75474_2_, p_75474_3_);
    }

    public int getDistanceToDoorBlockSq(BlockPos p_179848_1_)
    {
        return (int)p_179848_1_.distanceSq(getDoorBlockPos());
    }

    public int getDistanceToInsideBlockSq(BlockPos p_179846_1_)
    {
        return (int) insideBlock.distanceSq(p_179846_1_);
    }

    public boolean func_179850_c(BlockPos p_179850_1_)
    {
        int i = p_179850_1_.getX() - doorBlockPos.getX();
        int j = p_179850_1_.getZ() - doorBlockPos.getY();
        return i * insideDirection.getFrontOffsetX() + j * insideDirection.getFrontOffsetZ() >= 0;
    }

    public void resetDoorOpeningRestrictionCounter()
    {
        doorOpeningRestrictionCounter = 0;
    }

    public void incrementDoorOpeningRestrictionCounter()
    {
        ++doorOpeningRestrictionCounter;
    }

    public int getDoorOpeningRestrictionCounter()
    {
        return doorOpeningRestrictionCounter;
    }

    public BlockPos getDoorBlockPos()
    {
        return doorBlockPos;
    }

    public BlockPos getInsideBlockPos()
    {
        return insideBlock;
    }

    public int getInsideOffsetX()
    {
        return insideDirection.getFrontOffsetX() * 2;
    }

    public int getInsideOffsetZ()
    {
        return insideDirection.getFrontOffsetZ() * 2;
    }

    public int getInsidePosY()
    {
        return lastActivityTimestamp;
    }

    public void func_179849_a(int p_179849_1_)
    {
        lastActivityTimestamp = p_179849_1_;
    }

    public boolean getIsDetachedFromVillageFlag()
    {
        return isDetachedFromVillageFlag;
    }

    public void setIsDetachedFromVillageFlag(boolean p_179853_1_)
    {
        isDetachedFromVillageFlag = p_179853_1_;
    }
}
