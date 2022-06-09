package net.minecraft.pathfinding;

import net.minecraft.util.MathHelper;

public class PathPoint
{
    /** The x coordinate of this point */
    public final int xCoord;

    /** The y coordinate of this point */
    public final int yCoord;

    /** The z coordinate of this point */
    public final int zCoord;

    /** A hash of the coordinates used to identify this point */
    private final int hash;

    /** The index of this point in its assigned path */
    int index = -1;

    /** The distance along the path to this point */
    float totalPathDistance;

    /** The linear distance to the next point */
    float distanceToNext;

    /** The distance to the target */
    float distanceToTarget;

    /** The point preceding this in its assigned path */
    PathPoint previous;

    /** True if the pathfinder has already visited this point */
    public boolean visited;

    public PathPoint(int x, int y, int z)
    {
        xCoord = x;
        yCoord = y;
        zCoord = z;
        hash = PathPoint.makeHash(x, y, z);
    }

    public static int makeHash(int x, int y, int z)
    {
        return y & 255 | (x & 32767) << 8 | (z & 32767) << 24 | (x < 0 ? Integer.MIN_VALUE : 0) | (z < 0 ? 32768 : 0);
    }

    /**
     * Returns the linear distance to another path point
     */
    public float distanceTo(PathPoint pathpointIn)
    {
        float f = (float)(pathpointIn.xCoord - xCoord);
        float f1 = (float)(pathpointIn.yCoord - yCoord);
        float f2 = (float)(pathpointIn.zCoord - zCoord);
        return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);
    }

    /**
     * Returns the squared distance to another path point
     */
    public float distanceToSquared(PathPoint pathpointIn)
    {
        float f = (float)(pathpointIn.xCoord - xCoord);
        float f1 = (float)(pathpointIn.yCoord - yCoord);
        float f2 = (float)(pathpointIn.zCoord - zCoord);
        return f * f + f1 * f1 + f2 * f2;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (!(p_equals_1_ instanceof PathPoint))
        {
            return false;
        }
        else
        {
            PathPoint pathpoint = (PathPoint)p_equals_1_;
            return hash == pathpoint.hash && xCoord == pathpoint.xCoord && yCoord == pathpoint.yCoord && zCoord == pathpoint.zCoord;
        }
    }

    public int hashCode()
    {
        return hash;
    }

    /**
     * Returns true if this point has already been assigned to a path
     */
    public boolean isAssigned()
    {
        return index >= 0;
    }

    public String toString()
    {
        return xCoord + ", " + yCoord + ", " + zCoord;
    }
}
