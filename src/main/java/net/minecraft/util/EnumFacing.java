package net.minecraft.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public enum EnumFacing implements IStringSerializable
{
    DOWN("DOWN", 0, 0, 1, -1, "down", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Y, new Vec3i(0, -1, 0)),
    UP("UP", 1, 1, 0, -1, "up", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Y, new Vec3i(0, 1, 0)),
    NORTH("NORTH", 2, 2, 3, 2, "north", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Z, new Vec3i(0, 0, -1)),
    SOUTH("SOUTH", 3, 3, 2, 0, "south", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Z, new Vec3i(0, 0, 1)),
    WEST("WEST", 4, 4, 5, 1, "west", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X, new Vec3i(-1, 0, 0)),
    EAST("EAST", 5, 5, 4, 3, "east", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X, new Vec3i(1, 0, 0));

    /** Ordering index for D-U-N-S-W-E */
    private final int index;

    /** Index of the opposite Facing in the VALUES array */
    private final int opposite;

    /** Ordering index for the HORIZONTALS field (S-W-N-E) */
    private final int horizontalIndex;
    private final String name;
    private final EnumFacing.Axis axis;
    private final EnumFacing.AxisDirection axisDirection;

    /** Normalized Vector that points in the direction of this Facing */
    private final Vec3i directionVec;

    /** All facings in D-U-N-S-W-E order */
    public static final EnumFacing[] VALUES = new EnumFacing[6];

    /** All Facings with horizontal axis in order S-W-N-E */
    private static final EnumFacing[] HORIZONTALS = new EnumFacing[4];
    private static final Map NAME_LOOKUP = Maps.newHashMap();
    private static final EnumFacing[] $VALUES = new EnumFacing[]{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
    private static final String __OBFID = "CL_00001201";

    private EnumFacing(String p_i17_3_, int p_i17_4_, int p_i17_5_, int p_i17_6_, int p_i17_7_, String p_i17_8_, EnumFacing.AxisDirection p_i17_9_, EnumFacing.Axis p_i17_10_, Vec3i p_i17_11_)
    {
        index = p_i17_5_;
        horizontalIndex = p_i17_7_;
        opposite = p_i17_6_;
        name = p_i17_8_;
        axis = p_i17_10_;
        axisDirection = p_i17_9_;
        directionVec = p_i17_11_;
    }

    /**
     * Get the Index of this Facing (0-5). The order is D-U-N-S-W-E
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * Get the index of this horizontal facing (0-3). The order is S-W-N-E
     */
    public int getHorizontalIndex()
    {
        return horizontalIndex;
    }

    /**
     * Get the AxisDirection of this Facing.
     */
    public EnumFacing.AxisDirection getAxisDirection()
    {
        return axisDirection;
    }

    /**
     * Get the opposite Facing (e.g. DOWN => UP)
     */
    public EnumFacing getOpposite()
    {
        return EnumFacing.VALUES[opposite];
    }

    /**
     * Rotate this Facing around the given axis clockwise. If this facing cannot be rotated around the given axis,
     * returns this facing without rotating.
     */
    public EnumFacing rotateAround(EnumFacing.Axis axis)
    {
        switch (EnumFacing.EnumFacing$1.field_179515_a[axis.ordinal()])
        {
            case 1:
                if (this != EnumFacing.WEST && this != EnumFacing.EAST)
                {
                    return rotateX();
                }

                return this;

            case 2:
                if (this != EnumFacing.UP && this != EnumFacing.DOWN)
                {
                    return rotateY();
                }

                return this;

            case 3:
                if (this != EnumFacing.NORTH && this != EnumFacing.SOUTH)
                {
                    return rotateZ();
                }

                return this;

            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + axis);
        }
    }

    /**
     * Rotate this Facing around the Y axis clockwise (NORTH => EAST => SOUTH => WEST => NORTH)
     */
    public EnumFacing rotateY()
    {
        switch (EnumFacing.EnumFacing$1.field_179513_b[ordinal()])
        {
            case 1:
                return EnumFacing.EAST;

            case 2:
                return EnumFacing.SOUTH;

            case 3:
                return EnumFacing.WEST;

            case 4:
                return EnumFacing.NORTH;

            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }

    /**
     * Rotate this Facing around the X axis (NORTH => DOWN => SOUTH => UP => NORTH)
     */
    private EnumFacing rotateX()
    {
        switch (EnumFacing.EnumFacing$1.field_179513_b[ordinal()])
        {
            case 1:
                return EnumFacing.DOWN;

            case 2:
            case 4:
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);

            case 3:
                return EnumFacing.UP;

            case 5:
                return EnumFacing.NORTH;

            case 6:
                return EnumFacing.SOUTH;
        }
    }

    /**
     * Rotate this Facing around the Z axis (EAST => DOWN => WEST => UP => EAST)
     */
    private EnumFacing rotateZ()
    {
        switch (EnumFacing.EnumFacing$1.field_179513_b[ordinal()])
        {
            case 2:
                return EnumFacing.DOWN;

            case 3:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);

            case 4:
                return EnumFacing.UP;

            case 5:
                return EnumFacing.EAST;

            case 6:
                return EnumFacing.WEST;
        }
    }

    /**
     * Rotate this Facing around the Y axis counter-clockwise (NORTH => WEST => SOUTH => EAST => NORTH)
     */
    public EnumFacing rotateYCCW()
    {
        switch (EnumFacing.EnumFacing$1.field_179513_b[ordinal()])
        {
            case 1:
                return EnumFacing.WEST;

            case 2:
                return EnumFacing.NORTH;

            case 3:
                return EnumFacing.EAST;

            case 4:
                return EnumFacing.SOUTH;

            default:
                throw new IllegalStateException("Unable to get CCW facing of " + this);
        }
    }

    /**
     * Returns a offset that addresses the block in front of this facing.
     */
    public int getFrontOffsetX()
    {
        return axis == EnumFacing.Axis.X ? axisDirection.getOffset() : 0;
    }

    public int getFrontOffsetY()
    {
        return axis == EnumFacing.Axis.Y ? axisDirection.getOffset() : 0;
    }

    /**
     * Returns a offset that addresses the block in front of this facing.
     */
    public int getFrontOffsetZ()
    {
        return axis == EnumFacing.Axis.Z ? axisDirection.getOffset() : 0;
    }

    /**
     * Same as getName, but does not override the method from Enum.
     */
    public String getName2()
    {
        return name;
    }

    public EnumFacing.Axis getAxis()
    {
        return axis;
    }

    /**
     * Get the facing specified by the given name
     */
    public static EnumFacing byName(String name)
    {
        return name == null ? null : (EnumFacing) EnumFacing.NAME_LOOKUP.get(name.toLowerCase());
    }

    /**
     * Get a Facing by it's index (0-5). The order is D-U-N-S-W-E. Named getFront for legacy reasons.
     */
    public static EnumFacing getFront(int index)
    {
        return EnumFacing.VALUES[MathHelper.abs_int(index % EnumFacing.VALUES.length)];
    }

    /**
     * Get a Facing by it's horizontal index (0-3). The order is S-W-N-E.
     */
    public static EnumFacing getHorizontal(int p_176731_0_)
    {
        return EnumFacing.HORIZONTALS[MathHelper.abs_int(p_176731_0_ % EnumFacing.HORIZONTALS.length)];
    }

    /**
     * Get the Facing corresponding to the given angle (0-360). An angle of 0 is SOUTH, an angle of 90 would be WEST.
     */
    public static EnumFacing fromAngle(double angle)
    {
        return EnumFacing.getHorizontal(MathHelper.floor_double(angle / 90.0D + 0.5D) & 3);
    }

    /**
     * Choose a random Facing using the given Random
     */
    public static EnumFacing random(Random rand)
    {
        return EnumFacing.values()[rand.nextInt(EnumFacing.values().length)];
    }

    public static EnumFacing getFacingFromVector(float p_176737_0_, float p_176737_1_, float p_176737_2_)
    {
        EnumFacing enumfacing = EnumFacing.NORTH;
        float f = Float.MIN_VALUE;

        for (EnumFacing enumfacing1 : EnumFacing.values())
        {
            float f1 = p_176737_0_ * (float)enumfacing1.directionVec.getX() + p_176737_1_ * (float)enumfacing1.directionVec.getY() + p_176737_2_ * (float)enumfacing1.directionVec.getZ();

            if (f1 > f)
            {
                f = f1;
                enumfacing = enumfacing1;
            }
        }

        return enumfacing;
    }

    public String toString()
    {
        return name;
    }

    public String getName()
    {
        return name;
    }

    public static EnumFacing func_181076_a(EnumFacing.AxisDirection p_181076_0_, EnumFacing.Axis p_181076_1_)
    {
        for (EnumFacing enumfacing : EnumFacing.values())
        {
            if (enumfacing.getAxisDirection() == p_181076_0_ && enumfacing.getAxis() == p_181076_1_)
            {
                return enumfacing;
            }
        }

        throw new IllegalArgumentException("No such direction: " + p_181076_0_ + " " + p_181076_1_);
    }

    /**
     * Get a normalized Vector that points in the direction of this Facing.
     */
    public Vec3i getDirectionVec()
    {
        return directionVec;
    }

    static {
        for (EnumFacing enumfacing : EnumFacing.values())
        {
            EnumFacing.VALUES[enumfacing.index] = enumfacing;

            if (enumfacing.getAxis().isHorizontal())
            {
                EnumFacing.HORIZONTALS[enumfacing.horizontalIndex] = enumfacing;
            }

            EnumFacing.NAME_LOOKUP.put(enumfacing.getName2().toLowerCase(), enumfacing);
        }
    }

    static final class EnumFacing$1 {
        static final int[] field_179515_a;
        static final int[] field_179513_b;
        static final int[] field_179514_c = new int[EnumFacing.Plane.values().length];
        private static final String __OBFID = "CL_00002322";

        static {
            try {
                EnumFacing$1.field_179514_c[EnumFacing.Plane.HORIZONTAL.ordinal()] = 1;
            }
            catch (NoSuchFieldError var11)
            {
            }

            try {
                EnumFacing$1.field_179514_c[EnumFacing.Plane.VERTICAL.ordinal()] = 2;
            }
            catch (NoSuchFieldError var10)
            {
            }

            field_179513_b = new int[EnumFacing.values().length];

            try {
                EnumFacing$1.field_179513_b[EnumFacing.NORTH.ordinal()] = 1;
            }
            catch (NoSuchFieldError var9)
            {
            }

            try {
                EnumFacing$1.field_179513_b[EnumFacing.EAST.ordinal()] = 2;
            }
            catch (NoSuchFieldError var8)
            {
            }

            try {
                EnumFacing$1.field_179513_b[EnumFacing.SOUTH.ordinal()] = 3;
            }
            catch (NoSuchFieldError var7)
            {
            }

            try {
                EnumFacing$1.field_179513_b[EnumFacing.WEST.ordinal()] = 4;
            }
            catch (NoSuchFieldError var6)
            {
            }

            try {
                EnumFacing$1.field_179513_b[EnumFacing.UP.ordinal()] = 5;
            }
            catch (NoSuchFieldError var5)
            {
            }

            try {
                EnumFacing$1.field_179513_b[EnumFacing.DOWN.ordinal()] = 6;
            }
            catch (NoSuchFieldError var4)
            {
            }

            field_179515_a = new int[EnumFacing.Axis.values().length];

            try {
                EnumFacing$1.field_179515_a[EnumFacing.Axis.X.ordinal()] = 1;
            }
            catch (NoSuchFieldError var3)
            {
            }

            try {
                EnumFacing$1.field_179515_a[EnumFacing.Axis.Y.ordinal()] = 2;
            }
            catch (NoSuchFieldError var2)
            {
            }

            try {
                EnumFacing$1.field_179515_a[EnumFacing.Axis.Z.ordinal()] = 3;
            }
            catch (NoSuchFieldError var1)
            {
            }
        }
    }

    public static enum Axis implements Predicate, IStringSerializable {
        X("X", 0, "x", EnumFacing.Plane.HORIZONTAL),
        Y("Y", 1, "y", EnumFacing.Plane.VERTICAL),
        Z("Z", 2, "z", EnumFacing.Plane.HORIZONTAL);

        private static final Map NAME_LOOKUP = Maps.newHashMap();
        private final String name;
        private final EnumFacing.Plane plane;
        private static final EnumFacing.Axis[] $VALUES = new EnumFacing.Axis[]{Axis.X, Axis.Y, Axis.Z};
        private static final String __OBFID = "CL_00002321";

        private Axis(String p_i14_3_, int p_i14_4_, String p_i14_5_, EnumFacing.Plane p_i14_6_)
        {
            name = p_i14_5_;
            plane = p_i14_6_;
        }

        public static EnumFacing.Axis byName(String name)
        {
            return name == null ? null : (EnumFacing.Axis) Axis.NAME_LOOKUP.get(name.toLowerCase());
        }

        public String getName2()
        {
            return name;
        }

        public boolean isVertical()
        {
            return plane == EnumFacing.Plane.VERTICAL;
        }

        public boolean isHorizontal()
        {
            return plane == EnumFacing.Plane.HORIZONTAL;
        }

        public String toString()
        {
            return name;
        }

        public boolean apply(EnumFacing p_apply_1_)
        {
            return p_apply_1_ != null && p_apply_1_.getAxis() == this;
        }

        public EnumFacing.Plane getPlane()
        {
            return plane;
        }

        public String getName()
        {
            return name;
        }

        public boolean apply(Object p_apply_1_)
        {
            return apply((EnumFacing)p_apply_1_);
        }

        static {
            for (EnumFacing.Axis enumfacing$axis : Axis.values())
            {
                Axis.NAME_LOOKUP.put(enumfacing$axis.getName2().toLowerCase(), enumfacing$axis);
            }
        }
    }

    public static enum AxisDirection {
        POSITIVE("POSITIVE", 0, 1, "Towards positive"),
        NEGATIVE("NEGATIVE", 1, -1, "Towards negative");

        private final int offset;
        private final String description;
        private static final EnumFacing.AxisDirection[] $VALUES = new EnumFacing.AxisDirection[]{AxisDirection.POSITIVE, AxisDirection.NEGATIVE};
        private static final String __OBFID = "CL_00002320";

        private AxisDirection(String p_i15_3_, int p_i15_4_, int p_i15_5_, String p_i15_6_)
        {
            offset = p_i15_5_;
            description = p_i15_6_;
        }

        public int getOffset()
        {
            return offset;
        }

        public String toString()
        {
            return description;
        }
    }

    public static enum Plane implements Predicate, Iterable {
        HORIZONTAL("HORIZONTAL", 0),
        VERTICAL("VERTICAL", 1);

        private static final EnumFacing.Plane[] $VALUES = new EnumFacing.Plane[]{Plane.HORIZONTAL, Plane.VERTICAL};
        private static final String __OBFID = "CL_00002319";

        private Plane(String p_i16_3_, int p_i16_4_)
        {
        }

        public EnumFacing[] facings()
        {
            switch (EnumFacing.EnumFacing$1.field_179514_c[ordinal()])
            {
                case 1:
                    return new EnumFacing[] {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
                case 2:
                    return new EnumFacing[] {EnumFacing.UP, EnumFacing.DOWN};
                default:
                    throw new Error("Someone's been tampering with the universe!");
            }
        }

        public EnumFacing random(Random rand)
        {
            EnumFacing[] aenumfacing = facings();
            return aenumfacing[rand.nextInt(aenumfacing.length)];
        }

        public boolean apply(EnumFacing p_apply_1_)
        {
            return p_apply_1_ != null && p_apply_1_.getAxis().getPlane() == this;
        }

        public Iterator iterator()
        {
            return Iterators.forArray(facings());
        }

        public boolean apply(Object p_apply_1_)
        {
            return apply((EnumFacing)p_apply_1_);
        }
    }
}
