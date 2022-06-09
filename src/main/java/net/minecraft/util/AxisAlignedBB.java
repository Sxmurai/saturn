package net.minecraft.util;

public class AxisAlignedBB
{
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AxisAlignedBB(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        minX = Math.min(x1, x2);
        minY = Math.min(y1, y2);
        minZ = Math.min(z1, z2);
        maxX = Math.max(x1, x2);
        maxY = Math.max(y1, y2);
        maxZ = Math.max(z1, z2);
    }

    public AxisAlignedBB(BlockPos pos1, BlockPos pos2)
    {
        minX = pos1.getX();
        minY = pos1.getY();
        minZ = pos1.getZ();
        maxX = pos2.getX();
        maxY = pos2.getY();
        maxZ = pos2.getZ();
    }

    /**
     * Adds the coordinates to the bounding box extending it if the point lies outside the current ranges. Args: x, y, z
     */
    public AxisAlignedBB addCoord(double x, double y, double z)
    {
        double d0 = minX;
        double d1 = minY;
        double d2 = minZ;
        double d3 = maxX;
        double d4 = maxY;
        double d5 = maxZ;

        if (x < 0.0D)
        {
            d0 += x;
        }
        else if (x > 0.0D)
        {
            d3 += x;
        }

        if (y < 0.0D)
        {
            d1 += y;
        }
        else if (y > 0.0D)
        {
            d4 += y;
        }

        if (z < 0.0D)
        {
            d2 += z;
        }
        else if (z > 0.0D)
        {
            d5 += z;
        }

        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    /**
     * Returns a bounding box expanded by the specified vector (if negative numbers are given it will shrink). Args: x,
     * y, z
     */
    public AxisAlignedBB expand(double x, double y, double z)
    {
        double d0 = minX - x;
        double d1 = minY - y;
        double d2 = minZ - z;
        double d3 = maxX + x;
        double d4 = maxY + y;
        double d5 = maxZ + z;
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    public AxisAlignedBB union(AxisAlignedBB other)
    {
        double d0 = Math.min(minX, other.minX);
        double d1 = Math.min(minY, other.minY);
        double d2 = Math.min(minZ, other.minZ);
        double d3 = Math.max(maxX, other.maxX);
        double d4 = Math.max(maxY, other.maxY);
        double d5 = Math.max(maxZ, other.maxZ);
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    /**
     * returns an AABB with corners x1, y1, z1 and x2, y2, z2
     */
    public static AxisAlignedBB fromBounds(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        double d0 = Math.min(x1, x2);
        double d1 = Math.min(y1, y2);
        double d2 = Math.min(z1, z2);
        double d3 = Math.max(x1, x2);
        double d4 = Math.max(y1, y2);
        double d5 = Math.max(z1, z2);
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    /**
     * Offsets the current bounding box by the specified coordinates. Args: x, y, z
     */
    public AxisAlignedBB offset(double x, double y, double z)
    {
        return new AxisAlignedBB(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
    }

    /**
     * if instance and the argument bounding boxes overlap in the Y and Z dimensions, calculate the offset between them
     * in the X dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateXOffset(AxisAlignedBB other, double offsetX)
    {
        if (other.maxY > minY && other.minY < maxY && other.maxZ > minZ && other.minZ < maxZ)
        {
            if (offsetX > 0.0D && other.maxX <= minX)
            {
                double d1 = minX - other.maxX;

                if (d1 < offsetX)
                {
                    offsetX = d1;
                }
            }
            else if (offsetX < 0.0D && other.minX >= maxX)
            {
                double d0 = maxX - other.minX;

                if (d0 > offsetX)
                {
                    offsetX = d0;
                }
            }

            return offsetX;
        }
        else
        {
            return offsetX;
        }
    }

    /**
     * if instance and the argument bounding boxes overlap in the X and Z dimensions, calculate the offset between them
     * in the Y dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateYOffset(AxisAlignedBB other, double offsetY)
    {
        if (other.maxX > minX && other.minX < maxX && other.maxZ > minZ && other.minZ < maxZ)
        {
            if (offsetY > 0.0D && other.maxY <= minY)
            {
                double d1 = minY - other.maxY;

                if (d1 < offsetY)
                {
                    offsetY = d1;
                }
            }
            else if (offsetY < 0.0D && other.minY >= maxY)
            {
                double d0 = maxY - other.minY;

                if (d0 > offsetY)
                {
                    offsetY = d0;
                }
            }

            return offsetY;
        }
        else
        {
            return offsetY;
        }
    }

    /**
     * if instance and the argument bounding boxes overlap in the Y and X dimensions, calculate the offset between them
     * in the Z dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateZOffset(AxisAlignedBB other, double offsetZ)
    {
        if (other.maxX > minX && other.minX < maxX && other.maxY > minY && other.minY < maxY)
        {
            if (offsetZ > 0.0D && other.maxZ <= minZ)
            {
                double d1 = minZ - other.maxZ;

                if (d1 < offsetZ)
                {
                    offsetZ = d1;
                }
            }
            else if (offsetZ < 0.0D && other.minZ >= maxZ)
            {
                double d0 = maxZ - other.minZ;

                if (d0 > offsetZ)
                {
                    offsetZ = d0;
                }
            }

            return offsetZ;
        }
        else
        {
            return offsetZ;
        }
    }

    /**
     * Returns whether the given bounding box intersects with this one. Args: axisAlignedBB
     */
    public boolean intersectsWith(AxisAlignedBB other)
    {
        return other.maxX > minX && other.minX < maxX && (other.maxY > minY && other.minY < maxY && other.maxZ > minZ && other.minZ < maxZ);
    }

    /**
     * Returns if the supplied Vec3D is completely inside the bounding box
     */
    public boolean isVecInside(Vec3 vec)
    {
        return vec.xCoord > minX && vec.xCoord < maxX && (vec.yCoord > minY && vec.yCoord < maxY && vec.zCoord > minZ && vec.zCoord < maxZ);
    }

    /**
     * Returns the average length of the edges of the bounding box.
     */
    public double getAverageEdgeLength()
    {
        double d0 = maxX - minX;
        double d1 = maxY - minY;
        double d2 = maxZ - minZ;
        return (d0 + d1 + d2) / 3.0D;
    }

    /**
     * Returns a bounding box that is inset by the specified amounts
     */
    public AxisAlignedBB contract(double x, double y, double z)
    {
        double d0 = minX + x;
        double d1 = minY + y;
        double d2 = minZ + z;
        double d3 = maxX - x;
        double d4 = maxY - y;
        double d5 = maxZ - z;
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    public MovingObjectPosition calculateIntercept(Vec3 vecA, Vec3 vecB)
    {
        Vec3 vec3 = vecA.getIntermediateWithXValue(vecB, minX);
        Vec3 vec31 = vecA.getIntermediateWithXValue(vecB, maxX);
        Vec3 vec32 = vecA.getIntermediateWithYValue(vecB, minY);
        Vec3 vec33 = vecA.getIntermediateWithYValue(vecB, maxY);
        Vec3 vec34 = vecA.getIntermediateWithZValue(vecB, minZ);
        Vec3 vec35 = vecA.getIntermediateWithZValue(vecB, maxZ);

        if (!isVecInYZ(vec3))
        {
            vec3 = null;
        }

        if (!isVecInYZ(vec31))
        {
            vec31 = null;
        }

        if (!isVecInXZ(vec32))
        {
            vec32 = null;
        }

        if (!isVecInXZ(vec33))
        {
            vec33 = null;
        }

        if (!isVecInXY(vec34))
        {
            vec34 = null;
        }

        if (!isVecInXY(vec35))
        {
            vec35 = null;
        }

        Vec3 vec36 = null;

        if (vec3 != null)
        {
            vec36 = vec3;
        }

        if (vec31 != null && (vec36 == null || vecA.squareDistanceTo(vec31) < vecA.squareDistanceTo(vec36)))
        {
            vec36 = vec31;
        }

        if (vec32 != null && (vec36 == null || vecA.squareDistanceTo(vec32) < vecA.squareDistanceTo(vec36)))
        {
            vec36 = vec32;
        }

        if (vec33 != null && (vec36 == null || vecA.squareDistanceTo(vec33) < vecA.squareDistanceTo(vec36)))
        {
            vec36 = vec33;
        }

        if (vec34 != null && (vec36 == null || vecA.squareDistanceTo(vec34) < vecA.squareDistanceTo(vec36)))
        {
            vec36 = vec34;
        }

        if (vec35 != null && (vec36 == null || vecA.squareDistanceTo(vec35) < vecA.squareDistanceTo(vec36)))
        {
            vec36 = vec35;
        }

        if (vec36 == null)
        {
            return null;
        }
        else
        {
            EnumFacing enumfacing = null;

            if (vec36 == vec3)
            {
                enumfacing = EnumFacing.WEST;
            }
            else if (vec36 == vec31)
            {
                enumfacing = EnumFacing.EAST;
            }
            else if (vec36 == vec32)
            {
                enumfacing = EnumFacing.DOWN;
            }
            else if (vec36 == vec33)
            {
                enumfacing = EnumFacing.UP;
            }
            else if (vec36 == vec34)
            {
                enumfacing = EnumFacing.NORTH;
            }
            else
            {
                enumfacing = EnumFacing.SOUTH;
            }

            return new MovingObjectPosition(vec36, enumfacing);
        }
    }

    /**
     * Checks if the specified vector is within the YZ dimensions of the bounding box. Args: Vec3D
     */
    private boolean isVecInYZ(Vec3 vec)
    {
        return vec != null && vec.yCoord >= minY && vec.yCoord <= maxY && vec.zCoord >= minZ && vec.zCoord <= maxZ;
    }

    /**
     * Checks if the specified vector is within the XZ dimensions of the bounding box. Args: Vec3D
     */
    private boolean isVecInXZ(Vec3 vec)
    {
        return vec != null && vec.xCoord >= minX && vec.xCoord <= maxX && vec.zCoord >= minZ && vec.zCoord <= maxZ;
    }

    /**
     * Checks if the specified vector is within the XY dimensions of the bounding box. Args: Vec3D
     */
    private boolean isVecInXY(Vec3 vec)
    {
        return vec != null && vec.xCoord >= minX && vec.xCoord <= maxX && vec.yCoord >= minY && vec.yCoord <= maxY;
    }

    public String toString()
    {
        return "box[" + minX + ", " + minY + ", " + minZ + " -> " + maxX + ", " + maxY + ", " + maxZ + "]";
    }

    public boolean func_181656_b()
    {
        return Double.isNaN(minX) || Double.isNaN(minY) || Double.isNaN(minZ) || Double.isNaN(maxX) || Double.isNaN(maxY) || Double.isNaN(maxZ);
    }
}
