package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import java.util.Iterator;
import net.minecraft.entity.Entity;

public class BlockPos extends Vec3i
{
    /** The BlockPos with all coordinates 0 */
    public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
    private static final int NUM_X_BITS = 1 + MathHelper.calculateLogBaseTwo(MathHelper.roundUpToPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = BlockPos.NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - BlockPos.NUM_X_BITS - BlockPos.NUM_Z_BITS;
    private static final int Y_SHIFT = 0 + BlockPos.NUM_Z_BITS;
    private static final int X_SHIFT = BlockPos.Y_SHIFT + BlockPos.NUM_Y_BITS;
    private static final long X_MASK = (1L << BlockPos.NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << BlockPos.NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << BlockPos.NUM_Z_BITS) - 1L;

    public BlockPos(int x, int y, int z)
    {
        super(x, y, z);
    }

    public BlockPos(double x, double y, double z)
    {
        super(x, y, z);
    }

    public BlockPos(Entity source)
    {
        this(source.posX, source.posY, source.posZ);
    }

    public BlockPos(Vec3 source)
    {
        this(source.xCoord, source.yCoord, source.zCoord);
    }

    public BlockPos(Vec3i source)
    {
        this(source.getX(), source.getY(), source.getZ());
    }

    /**
     * Add the given coordinates to the coordinates of this BlockPos
     */
    public BlockPos add(double x, double y, double z)
    {
        return x == 0.0D && y == 0.0D && z == 0.0D ? this : new BlockPos((double) getX() + x, (double) getY() + y, (double) getZ() + z);
    }

    /**
     * Add the given coordinates to the coordinates of this BlockPos
     */
    public BlockPos add(int x, int y, int z)
    {
        return x == 0 && y == 0 && z == 0 ? this : new BlockPos(getX() + x, getY() + y, getZ() + z);
    }

    /**
     * Add the given Vector to this BlockPos
     */
    public BlockPos add(Vec3i vec)
    {
        return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? this : new BlockPos(getX() + vec.getX(), getY() + vec.getY(), getZ() + vec.getZ());
    }

    /**
     * Subtract the given Vector from this BlockPos
     */
    public BlockPos subtract(Vec3i vec)
    {
        return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? this : new BlockPos(getX() - vec.getX(), getY() - vec.getY(), getZ() - vec.getZ());
    }

    /**
     * Offset this BlockPos 1 block up
     */
    public BlockPos up()
    {
        return up(1);
    }

    /**
     * Offset this BlockPos n blocks up
     */
    public BlockPos up(int n)
    {
        return offset(EnumFacing.UP, n);
    }

    /**
     * Offset this BlockPos 1 block down
     */
    public BlockPos down()
    {
        return down(1);
    }

    /**
     * Offset this BlockPos n blocks down
     */
    public BlockPos down(int n)
    {
        return offset(EnumFacing.DOWN, n);
    }

    /**
     * Offset this BlockPos 1 block in northern direction
     */
    public BlockPos north()
    {
        return north(1);
    }

    /**
     * Offset this BlockPos n blocks in northern direction
     */
    public BlockPos north(int n)
    {
        return offset(EnumFacing.NORTH, n);
    }

    /**
     * Offset this BlockPos 1 block in southern direction
     */
    public BlockPos south()
    {
        return south(1);
    }

    /**
     * Offset this BlockPos n blocks in southern direction
     */
    public BlockPos south(int n)
    {
        return offset(EnumFacing.SOUTH, n);
    }

    /**
     * Offset this BlockPos 1 block in western direction
     */
    public BlockPos west()
    {
        return west(1);
    }

    /**
     * Offset this BlockPos n blocks in western direction
     */
    public BlockPos west(int n)
    {
        return offset(EnumFacing.WEST, n);
    }

    /**
     * Offset this BlockPos 1 block in eastern direction
     */
    public BlockPos east()
    {
        return east(1);
    }

    /**
     * Offset this BlockPos n blocks in eastern direction
     */
    public BlockPos east(int n)
    {
        return offset(EnumFacing.EAST, n);
    }

    /**
     * Offset this BlockPos 1 block in the given direction
     */
    public BlockPos offset(EnumFacing facing)
    {
        return offset(facing, 1);
    }

    /**
     * Offsets this BlockPos n blocks in the given direction
     */
    public BlockPos offset(EnumFacing facing, int n)
    {
        return n == 0 ? this : new BlockPos(getX() + facing.getFrontOffsetX() * n, getY() + facing.getFrontOffsetY() * n, getZ() + facing.getFrontOffsetZ() * n);
    }

    /**
     * Calculate the cross product of this and the given Vector
     */
    public BlockPos crossProduct(Vec3i vec)
    {
        return new BlockPos(getY() * vec.getZ() - getZ() * vec.getY(), getZ() * vec.getX() - getX() * vec.getZ(), getX() * vec.getY() - getY() * vec.getX());
    }

    /**
     * Serialize this BlockPos into a long value
     */
    public long toLong()
    {
        return ((long) getX() & BlockPos.X_MASK) << BlockPos.X_SHIFT | ((long) getY() & BlockPos.Y_MASK) << BlockPos.Y_SHIFT | ((long) getZ() & BlockPos.Z_MASK) << 0;
    }

    /**
     * Create a BlockPos from a serialized long value (created by toLong)
     */
    public static BlockPos fromLong(long serialized)
    {
        int i = (int)(serialized << 64 - BlockPos.X_SHIFT - BlockPos.NUM_X_BITS >> 64 - BlockPos.NUM_X_BITS);
        int j = (int)(serialized << 64 - BlockPos.Y_SHIFT - BlockPos.NUM_Y_BITS >> 64 - BlockPos.NUM_Y_BITS);
        int k = (int)(serialized << 64 - BlockPos.NUM_Z_BITS >> 64 - BlockPos.NUM_Z_BITS);
        return new BlockPos(i, j, k);
    }

    public static Iterable<BlockPos> getAllInBox(BlockPos from, BlockPos to)
    {
        final BlockPos blockpos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos blockpos1 = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        return new Iterable<BlockPos>()
        {
            public Iterator<BlockPos> iterator()
            {
                return new AbstractIterator<BlockPos>()
                {
                    private BlockPos lastReturned = null;
                    protected BlockPos computeNext()
                    {
                        if (lastReturned == null)
                        {
                            lastReturned = blockpos;
                            return lastReturned;
                        }
                        else if (lastReturned.equals(blockpos1))
                        {
                            return endOfData();
                        }
                        else
                        {
                            int i = lastReturned.getX();
                            int j = lastReturned.getY();
                            int k = lastReturned.getZ();

                            if (i < blockpos1.getX())
                            {
                                ++i;
                            }
                            else if (j < blockpos1.getY())
                            {
                                i = blockpos.getX();
                                ++j;
                            }
                            else if (k < blockpos1.getZ())
                            {
                                i = blockpos.getX();
                                j = blockpos.getY();
                                ++k;
                            }

                            lastReturned = new BlockPos(i, j, k);
                            return lastReturned;
                        }
                    }
                };
            }
        };
    }

    public static Iterable<BlockPos.MutableBlockPos> getAllInBoxMutable(BlockPos from, BlockPos to)
    {
        final BlockPos blockpos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos blockpos1 = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        return new Iterable<BlockPos.MutableBlockPos>()
        {
            public Iterator<BlockPos.MutableBlockPos> iterator()
            {
                return new AbstractIterator<BlockPos.MutableBlockPos>()
                {
                    private BlockPos.MutableBlockPos theBlockPos = null;
                    protected BlockPos.MutableBlockPos computeNext()
                    {
                        if (theBlockPos == null)
                        {
                            theBlockPos = new BlockPos.MutableBlockPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                            return theBlockPos;
                        }
                        else if (theBlockPos.equals(blockpos1))
                        {
                            return endOfData();
                        }
                        else
                        {
                            int i = theBlockPos.getX();
                            int j = theBlockPos.getY();
                            int k = theBlockPos.getZ();

                            if (i < blockpos1.getX())
                            {
                                ++i;
                            }
                            else if (j < blockpos1.getY())
                            {
                                i = blockpos.getX();
                                ++j;
                            }
                            else if (k < blockpos1.getZ())
                            {
                                i = blockpos.getX();
                                j = blockpos.getY();
                                ++k;
                            }

                            theBlockPos.x = i;
                            theBlockPos.y = j;
                            theBlockPos.z = k;
                            return theBlockPos;
                        }
                    }
                };
            }
        };
    }

    public static final class MutableBlockPos extends BlockPos
    {
        private int x;
        private int y;
        private int z;

        public MutableBlockPos()
        {
            this(0, 0, 0);
        }

        public MutableBlockPos(int x_, int y_, int z_)
        {
            super(0, 0, 0);
            x = x_;
            y = y_;
            z = z_;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }

        public int getZ()
        {
            return z;
        }

        public BlockPos.MutableBlockPos func_181079_c(int p_181079_1_, int p_181079_2_, int p_181079_3_)
        {
            x = p_181079_1_;
            y = p_181079_2_;
            z = p_181079_3_;
            return this;
        }
    }
}
