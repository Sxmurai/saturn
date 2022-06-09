package net.minecraft.util;

import com.google.common.base.Objects;

public class Vec3i implements Comparable<Vec3i>
{
    /** The Null vector constant (0, 0, 0) */
    public static final Vec3i NULL_VECTOR = new Vec3i(0, 0, 0);

    /** X coordinate */
    private final int x;

    /** Y coordinate */
    private final int y;

    /** Z coordinate */
    private final int z;

    public Vec3i(int xIn, int yIn, int zIn)
    {
        x = xIn;
        y = yIn;
        z = zIn;
    }

    public Vec3i(double xIn, double yIn, double zIn)
    {
        this(MathHelper.floor_double(xIn), MathHelper.floor_double(yIn), MathHelper.floor_double(zIn));
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (!(p_equals_1_ instanceof Vec3i))
        {
            return false;
        }
        else
        {
            Vec3i vec3i = (Vec3i)p_equals_1_;
            return getX() == vec3i.getX() && (getY() == vec3i.getY() && getZ() == vec3i.getZ());
        }
    }

    public int hashCode()
    {
        return (getY() + getZ() * 31) * 31 + getX();
    }

    public int compareTo(Vec3i p_compareTo_1_)
    {
        return getY() == p_compareTo_1_.getY() ? (getZ() == p_compareTo_1_.getZ() ? getX() - p_compareTo_1_.getX() : getZ() - p_compareTo_1_.getZ()) : getY() - p_compareTo_1_.getY();
    }

    /**
     * Get the X coordinate
     */
    public int getX()
    {
        return x;
    }

    /**
     * Get the Y coordinate
     */
    public int getY()
    {
        return y;
    }

    /**
     * Get the Z coordinate
     */
    public int getZ()
    {
        return z;
    }

    /**
     * Calculate the cross product of this and the given Vector
     */
    public Vec3i crossProduct(Vec3i vec)
    {
        return new Vec3i(getY() * vec.getZ() - getZ() * vec.getY(), getZ() * vec.getX() - getX() * vec.getZ(), getX() * vec.getY() - getY() * vec.getX());
    }

    /**
     * Calculate squared distance to the given coordinates
     */
    public double distanceSq(double toX, double toY, double toZ)
    {
        double d0 = (double) getX() - toX;
        double d1 = (double) getY() - toY;
        double d2 = (double) getZ() - toZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    /**
     * Compute square of distance from point x, y, z to center of this Block
     */
    public double distanceSqToCenter(double xIn, double yIn, double zIn)
    {
        double d0 = (double) getX() + 0.5D - xIn;
        double d1 = (double) getY() + 0.5D - yIn;
        double d2 = (double) getZ() + 0.5D - zIn;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    /**
     * Calculate squared distance to the given Vector
     */
    public double distanceSq(Vec3i to)
    {
        return distanceSq(to.getX(), to.getY(), to.getZ());
    }

    public String toString()
    {
        return Objects.toStringHelper(this).add("x", getX()).add("y", getY()).add("z", getZ()).toString();
    }
}
