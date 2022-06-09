package net.minecraft.pathfinding;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

public abstract class PathNavigate
{
    protected EntityLiving theEntity;
    protected World worldObj;

    /** The PathEntity being followed. */
    protected PathEntity currentPath;
    protected double speed;

    /**
     * The number of blocks (extra) +/- in each axis that get pulled out as cache for the pathfinder's search space
     */
    private final IAttributeInstance pathSearchRange;

    /** Time, in number of ticks, following the current path */
    private int totalTicks;

    /**
     * The time when the last position check was done (to detect successful movement)
     */
    private int ticksAtLastPos;

    /**
     * Coordinates of the entity's position last time a check was done (part of monitoring getting 'stuck')
     */
    private Vec3 lastPosCheck = new Vec3(0.0D, 0.0D, 0.0D);
    private float heightRequirement = 1.0F;
    private final PathFinder pathFinder;

    public PathNavigate(EntityLiving entitylivingIn, World worldIn)
    {
        theEntity = entitylivingIn;
        worldObj = worldIn;
        pathSearchRange = entitylivingIn.getEntityAttribute(SharedMonsterAttributes.followRange);
        pathFinder = getPathFinder();
    }

    protected abstract PathFinder getPathFinder();

    /**
     * Sets the speed
     */
    public void setSpeed(double speedIn)
    {
        speed = speedIn;
    }

    /**
     * Gets the maximum distance that the path finding will search in.
     */
    public float getPathSearchRange()
    {
        return (float) pathSearchRange.getAttributeValue();
    }

    /**
     * Returns the path to the given coordinates. Args : x, y, z
     */
    public final PathEntity getPathToXYZ(double x, double y, double z)
    {
        return getPathToPos(new BlockPos(MathHelper.floor_double(x), (int)y, MathHelper.floor_double(z)));
    }

    /**
     * Returns path to given BlockPos
     */
    public PathEntity getPathToPos(BlockPos pos)
    {
        if (!canNavigate())
        {
            return null;
        }
        else
        {
            float f = getPathSearchRange();
            worldObj.theProfiler.startSection("pathfind");
            BlockPos blockpos = new BlockPos(theEntity);
            int i = (int)(f + 8.0F);
            ChunkCache chunkcache = new ChunkCache(worldObj, blockpos.add(-i, -i, -i), blockpos.add(i, i, i), 0);
            PathEntity pathentity = pathFinder.createEntityPathTo(chunkcache, theEntity, pos, f);
            worldObj.theProfiler.endSection();
            return pathentity;
        }
    }

    /**
     * Try to find and set a path to XYZ. Returns true if successful. Args : x, y, z, speed
     */
    public boolean tryMoveToXYZ(double x, double y, double z, double speedIn)
    {
        PathEntity pathentity = getPathToXYZ(MathHelper.floor_double(x), (int)y, MathHelper.floor_double(z));
        return setPath(pathentity, speedIn);
    }

    /**
     * Sets vertical space requirement for path
     */
    public void setHeightRequirement(float jumpHeight)
    {
        heightRequirement = jumpHeight;
    }

    /**
     * Returns the path to the given EntityLiving. Args : entity
     */
    public PathEntity getPathToEntityLiving(Entity entityIn)
    {
        if (!canNavigate())
        {
            return null;
        }
        else
        {
            float f = getPathSearchRange();
            worldObj.theProfiler.startSection("pathfind");
            BlockPos blockpos = (new BlockPos(theEntity)).up();
            int i = (int)(f + 16.0F);
            ChunkCache chunkcache = new ChunkCache(worldObj, blockpos.add(-i, -i, -i), blockpos.add(i, i, i), 0);
            PathEntity pathentity = pathFinder.createEntityPathTo(chunkcache, theEntity, entityIn, f);
            worldObj.theProfiler.endSection();
            return pathentity;
        }
    }

    /**
     * Try to find and set a path to EntityLiving. Returns true if successful. Args : entity, speed
     */
    public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn)
    {
        PathEntity pathentity = getPathToEntityLiving(entityIn);
        return pathentity != null && setPath(pathentity, speedIn);
    }

    /**
     * Sets a new path. If it's diferent from the old path. Checks to adjust path for sun avoiding, and stores start
     * coords. Args : path, speed
     */
    public boolean setPath(PathEntity pathentityIn, double speedIn)
    {
        if (pathentityIn == null)
        {
            currentPath = null;
            return false;
        }
        else
        {
            if (!pathentityIn.isSamePath(currentPath))
            {
                currentPath = pathentityIn;
            }

            removeSunnyPath();

            if (currentPath.getCurrentPathLength() == 0)
            {
                return false;
            }
            else
            {
                speed = speedIn;
                Vec3 vec3 = getEntityPosition();
                ticksAtLastPos = totalTicks;
                lastPosCheck = vec3;
                return true;
            }
        }
    }

    /**
     * gets the actively used PathEntity
     */
    public PathEntity getPath()
    {
        return currentPath;
    }

    public void onUpdateNavigation()
    {
        ++totalTicks;

        if (!noPath())
        {
            if (canNavigate())
            {
                pathFollow();
            }
            else if (currentPath != null && currentPath.getCurrentPathIndex() < currentPath.getCurrentPathLength())
            {
                Vec3 vec3 = getEntityPosition();
                Vec3 vec31 = currentPath.getVectorFromIndex(theEntity, currentPath.getCurrentPathIndex());

                if (vec3.yCoord > vec31.yCoord && !theEntity.onGround && MathHelper.floor_double(vec3.xCoord) == MathHelper.floor_double(vec31.xCoord) && MathHelper.floor_double(vec3.zCoord) == MathHelper.floor_double(vec31.zCoord))
                {
                    currentPath.setCurrentPathIndex(currentPath.getCurrentPathIndex() + 1);
                }
            }

            if (!noPath())
            {
                Vec3 vec32 = currentPath.getPosition(theEntity);

                if (vec32 != null)
                {
                    AxisAlignedBB axisalignedbb1 = (new AxisAlignedBB(vec32.xCoord, vec32.yCoord, vec32.zCoord, vec32.xCoord, vec32.yCoord, vec32.zCoord)).expand(0.5D, 0.5D, 0.5D);
                    List<AxisAlignedBB> list = worldObj.getCollidingBoundingBoxes(theEntity, axisalignedbb1.addCoord(0.0D, -1.0D, 0.0D));
                    double d0 = -1.0D;
                    axisalignedbb1 = axisalignedbb1.offset(0.0D, 1.0D, 0.0D);

                    for (AxisAlignedBB axisalignedbb : list)
                    {
                        d0 = axisalignedbb.calculateYOffset(axisalignedbb1, d0);
                    }

                    theEntity.getMoveHelper().setMoveTo(vec32.xCoord, vec32.yCoord + d0, vec32.zCoord, speed);
                }
            }
        }
    }

    protected void pathFollow()
    {
        Vec3 vec3 = getEntityPosition();
        int i = currentPath.getCurrentPathLength();

        for (int j = currentPath.getCurrentPathIndex(); j < currentPath.getCurrentPathLength(); ++j)
        {
            if (currentPath.getPathPointFromIndex(j).yCoord != (int)vec3.yCoord)
            {
                i = j;
                break;
            }
        }

        float f = theEntity.width * theEntity.width * heightRequirement;

        for (int k = currentPath.getCurrentPathIndex(); k < i; ++k)
        {
            Vec3 vec31 = currentPath.getVectorFromIndex(theEntity, k);

            if (vec3.squareDistanceTo(vec31) < (double)f)
            {
                currentPath.setCurrentPathIndex(k + 1);
            }
        }

        int j1 = MathHelper.ceiling_float_int(theEntity.width);
        int k1 = (int) theEntity.height + 1;
        int l = j1;

        for (int i1 = i - 1; i1 >= currentPath.getCurrentPathIndex(); --i1)
        {
            if (isDirectPathBetweenPoints(vec3, currentPath.getVectorFromIndex(theEntity, i1), j1, k1, l))
            {
                currentPath.setCurrentPathIndex(i1);
                break;
            }
        }

        checkForStuck(vec3);
    }

    /**
     * Checks if entity haven't been moved when last checked and if so, clears current {@link
     * net.minecraft.pathfinding.PathEntity}
     */
    protected void checkForStuck(Vec3 positionVec3)
    {
        if (totalTicks - ticksAtLastPos > 100)
        {
            if (positionVec3.squareDistanceTo(lastPosCheck) < 2.25D)
            {
                clearPathEntity();
            }

            ticksAtLastPos = totalTicks;
            lastPosCheck = positionVec3;
        }
    }

    /**
     * If null path or reached the end
     */
    public boolean noPath()
    {
        return currentPath == null || currentPath.isFinished();
    }

    /**
     * sets active PathEntity to null
     */
    public void clearPathEntity()
    {
        currentPath = null;
    }

    protected abstract Vec3 getEntityPosition();

    /**
     * If on ground or swimming and can swim
     */
    protected abstract boolean canNavigate();

    /**
     * Returns true if the entity is in water or lava, false otherwise
     */
    protected boolean isInLiquid()
    {
        return theEntity.isInWater() || theEntity.isInLava();
    }

    /**
     * Trims path data from the end to the first sun covered block
     */
    protected void removeSunnyPath()
    {
    }

    /**
     * Returns true when an entity of specified size could safely walk in a straight line between the two points. Args:
     * pos1, pos2, entityXSize, entityYSize, entityZSize
     */
    protected abstract boolean isDirectPathBetweenPoints(Vec3 posVec31, Vec3 posVec32, int sizeX, int sizeY, int sizeZ);
}
