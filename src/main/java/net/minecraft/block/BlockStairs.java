package net.minecraft.block;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStairs extends Block
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyEnum<BlockStairs.EnumHalf> HALF = PropertyEnum.create("half", BlockStairs.EnumHalf.class);
    public static final PropertyEnum<BlockStairs.EnumShape> SHAPE = PropertyEnum.create("shape", BlockStairs.EnumShape.class);
    private static final int[][] field_150150_a = new int[][] {{4, 5}, {5, 7}, {6, 7}, {4, 6}, {0, 1}, {1, 3}, {2, 3}, {0, 2}};
    private final Block modelBlock;
    private final IBlockState modelState;
    private boolean hasRaytraced;
    private int rayTracePass;

    protected BlockStairs(IBlockState modelState)
    {
        super(modelState.getBlock().blockMaterial);
        setDefaultState(blockState.getBaseState().withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT));
        modelBlock = modelState.getBlock();
        this.modelState = modelState;
        setHardness(modelBlock.blockHardness);
        setResistance(modelBlock.blockResistance / 3.0F);
        setStepSound(modelBlock.stepSound);
        setLightOpacity(255);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        if (hasRaytraced)
        {
            setBlockBounds(0.5F * (float)(rayTracePass % 2), 0.5F * (float)(rayTracePass / 4 % 2), 0.5F * (float)(rayTracePass / 2 % 2), 0.5F + 0.5F * (float)(rayTracePass % 2), 0.5F + 0.5F * (float)(rayTracePass / 4 % 2), 0.5F + 0.5F * (float)(rayTracePass / 2 % 2));
        }
        else
        {
            setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube()
    {
        return false;
    }

    public boolean isFullCube()
    {
        return false;
    }

    /**
     * Set the block bounds as the collision bounds for the stairs at the given position
     */
    public void setBaseCollisionBounds(IBlockAccess worldIn, BlockPos pos)
    {
        if (worldIn.getBlockState(pos).getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP)
        {
            setBlockBounds(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
        else
        {
            setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        }
    }

    /**
     * Checks if a block is stairs
     */
    public static boolean isBlockStairs(Block blockIn)
    {
        return blockIn instanceof BlockStairs;
    }

    /**
     * Check whether there is a stair block at the given position and it has the same properties as the given BlockState
     */
    public static boolean isSameStair(IBlockAccess worldIn, BlockPos pos, IBlockState state)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        return BlockStairs.isBlockStairs(block) && iblockstate.getValue(BlockStairs.HALF) == state.getValue(BlockStairs.HALF) && iblockstate.getValue(BlockStairs.FACING) == state.getValue(BlockStairs.FACING);
    }

    public int func_176307_f(IBlockAccess blockAccess, BlockPos pos)
    {
        IBlockState iblockstate = blockAccess.getBlockState(pos);
        EnumFacing enumfacing = iblockstate.getValue(BlockStairs.FACING);
        BlockStairs.EnumHalf blockstairs$enumhalf = iblockstate.getValue(BlockStairs.HALF);
        boolean flag = blockstairs$enumhalf == BlockStairs.EnumHalf.TOP;

        if (enumfacing == EnumFacing.EAST)
        {
            IBlockState iblockstate1 = blockAccess.getBlockState(pos.east());
            Block block = iblockstate1.getBlock();

            if (BlockStairs.isBlockStairs(block) && blockstairs$enumhalf == iblockstate1.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing1 = iblockstate1.getValue(BlockStairs.FACING);

                if (enumfacing1 == EnumFacing.NORTH && !BlockStairs.isSameStair(blockAccess, pos.south(), iblockstate))
                {
                    return flag ? 1 : 2;
                }

                if (enumfacing1 == EnumFacing.SOUTH && !BlockStairs.isSameStair(blockAccess, pos.north(), iblockstate))
                {
                    return flag ? 2 : 1;
                }
            }
        }
        else if (enumfacing == EnumFacing.WEST)
        {
            IBlockState iblockstate2 = blockAccess.getBlockState(pos.west());
            Block block1 = iblockstate2.getBlock();

            if (BlockStairs.isBlockStairs(block1) && blockstairs$enumhalf == iblockstate2.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing2 = iblockstate2.getValue(BlockStairs.FACING);

                if (enumfacing2 == EnumFacing.NORTH && !BlockStairs.isSameStair(blockAccess, pos.south(), iblockstate))
                {
                    return flag ? 2 : 1;
                }

                if (enumfacing2 == EnumFacing.SOUTH && !BlockStairs.isSameStair(blockAccess, pos.north(), iblockstate))
                {
                    return flag ? 1 : 2;
                }
            }
        }
        else if (enumfacing == EnumFacing.SOUTH)
        {
            IBlockState iblockstate3 = blockAccess.getBlockState(pos.south());
            Block block2 = iblockstate3.getBlock();

            if (BlockStairs.isBlockStairs(block2) && blockstairs$enumhalf == iblockstate3.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing3 = iblockstate3.getValue(BlockStairs.FACING);

                if (enumfacing3 == EnumFacing.WEST && !BlockStairs.isSameStair(blockAccess, pos.east(), iblockstate))
                {
                    return flag ? 2 : 1;
                }

                if (enumfacing3 == EnumFacing.EAST && !BlockStairs.isSameStair(blockAccess, pos.west(), iblockstate))
                {
                    return flag ? 1 : 2;
                }
            }
        }
        else if (enumfacing == EnumFacing.NORTH)
        {
            IBlockState iblockstate4 = blockAccess.getBlockState(pos.north());
            Block block3 = iblockstate4.getBlock();

            if (BlockStairs.isBlockStairs(block3) && blockstairs$enumhalf == iblockstate4.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing4 = iblockstate4.getValue(BlockStairs.FACING);

                if (enumfacing4 == EnumFacing.WEST && !BlockStairs.isSameStair(blockAccess, pos.east(), iblockstate))
                {
                    return flag ? 1 : 2;
                }

                if (enumfacing4 == EnumFacing.EAST && !BlockStairs.isSameStair(blockAccess, pos.west(), iblockstate))
                {
                    return flag ? 2 : 1;
                }
            }
        }

        return 0;
    }

    public int func_176305_g(IBlockAccess blockAccess, BlockPos pos)
    {
        IBlockState iblockstate = blockAccess.getBlockState(pos);
        EnumFacing enumfacing = iblockstate.getValue(BlockStairs.FACING);
        BlockStairs.EnumHalf blockstairs$enumhalf = iblockstate.getValue(BlockStairs.HALF);
        boolean flag = blockstairs$enumhalf == BlockStairs.EnumHalf.TOP;

        if (enumfacing == EnumFacing.EAST)
        {
            IBlockState iblockstate1 = blockAccess.getBlockState(pos.west());
            Block block = iblockstate1.getBlock();

            if (BlockStairs.isBlockStairs(block) && blockstairs$enumhalf == iblockstate1.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing1 = iblockstate1.getValue(BlockStairs.FACING);

                if (enumfacing1 == EnumFacing.NORTH && !BlockStairs.isSameStair(blockAccess, pos.north(), iblockstate))
                {
                    return flag ? 1 : 2;
                }

                if (enumfacing1 == EnumFacing.SOUTH && !BlockStairs.isSameStair(blockAccess, pos.south(), iblockstate))
                {
                    return flag ? 2 : 1;
                }
            }
        }
        else if (enumfacing == EnumFacing.WEST)
        {
            IBlockState iblockstate2 = blockAccess.getBlockState(pos.east());
            Block block1 = iblockstate2.getBlock();

            if (BlockStairs.isBlockStairs(block1) && blockstairs$enumhalf == iblockstate2.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing2 = iblockstate2.getValue(BlockStairs.FACING);

                if (enumfacing2 == EnumFacing.NORTH && !BlockStairs.isSameStair(blockAccess, pos.north(), iblockstate))
                {
                    return flag ? 2 : 1;
                }

                if (enumfacing2 == EnumFacing.SOUTH && !BlockStairs.isSameStair(blockAccess, pos.south(), iblockstate))
                {
                    return flag ? 1 : 2;
                }
            }
        }
        else if (enumfacing == EnumFacing.SOUTH)
        {
            IBlockState iblockstate3 = blockAccess.getBlockState(pos.north());
            Block block2 = iblockstate3.getBlock();

            if (BlockStairs.isBlockStairs(block2) && blockstairs$enumhalf == iblockstate3.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing3 = iblockstate3.getValue(BlockStairs.FACING);

                if (enumfacing3 == EnumFacing.WEST && !BlockStairs.isSameStair(blockAccess, pos.west(), iblockstate))
                {
                    return flag ? 2 : 1;
                }

                if (enumfacing3 == EnumFacing.EAST && !BlockStairs.isSameStair(blockAccess, pos.east(), iblockstate))
                {
                    return flag ? 1 : 2;
                }
            }
        }
        else if (enumfacing == EnumFacing.NORTH)
        {
            IBlockState iblockstate4 = blockAccess.getBlockState(pos.south());
            Block block3 = iblockstate4.getBlock();

            if (BlockStairs.isBlockStairs(block3) && blockstairs$enumhalf == iblockstate4.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing4 = iblockstate4.getValue(BlockStairs.FACING);

                if (enumfacing4 == EnumFacing.WEST && !BlockStairs.isSameStair(blockAccess, pos.west(), iblockstate))
                {
                    return flag ? 1 : 2;
                }

                if (enumfacing4 == EnumFacing.EAST && !BlockStairs.isSameStair(blockAccess, pos.east(), iblockstate))
                {
                    return flag ? 2 : 1;
                }
            }
        }

        return 0;
    }

    public boolean func_176306_h(IBlockAccess blockAccess, BlockPos pos)
    {
        IBlockState iblockstate = blockAccess.getBlockState(pos);
        EnumFacing enumfacing = iblockstate.getValue(BlockStairs.FACING);
        BlockStairs.EnumHalf blockstairs$enumhalf = iblockstate.getValue(BlockStairs.HALF);
        boolean flag = blockstairs$enumhalf == BlockStairs.EnumHalf.TOP;
        float f = 0.5F;
        float f1 = 1.0F;

        if (flag)
        {
            f = 0.0F;
            f1 = 0.5F;
        }

        float f2 = 0.0F;
        float f3 = 1.0F;
        float f4 = 0.0F;
        float f5 = 0.5F;
        boolean flag1 = true;

        if (enumfacing == EnumFacing.EAST)
        {
            f2 = 0.5F;
            f5 = 1.0F;
            IBlockState iblockstate1 = blockAccess.getBlockState(pos.east());
            Block block = iblockstate1.getBlock();

            if (BlockStairs.isBlockStairs(block) && blockstairs$enumhalf == iblockstate1.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing1 = iblockstate1.getValue(BlockStairs.FACING);

                if (enumfacing1 == EnumFacing.NORTH && !BlockStairs.isSameStair(blockAccess, pos.south(), iblockstate))
                {
                    f5 = 0.5F;
                    flag1 = false;
                }
                else if (enumfacing1 == EnumFacing.SOUTH && !BlockStairs.isSameStair(blockAccess, pos.north(), iblockstate))
                {
                    f4 = 0.5F;
                    flag1 = false;
                }
            }
        }
        else if (enumfacing == EnumFacing.WEST)
        {
            f3 = 0.5F;
            f5 = 1.0F;
            IBlockState iblockstate2 = blockAccess.getBlockState(pos.west());
            Block block1 = iblockstate2.getBlock();

            if (BlockStairs.isBlockStairs(block1) && blockstairs$enumhalf == iblockstate2.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing2 = iblockstate2.getValue(BlockStairs.FACING);

                if (enumfacing2 == EnumFacing.NORTH && !BlockStairs.isSameStair(blockAccess, pos.south(), iblockstate))
                {
                    f5 = 0.5F;
                    flag1 = false;
                }
                else if (enumfacing2 == EnumFacing.SOUTH && !BlockStairs.isSameStair(blockAccess, pos.north(), iblockstate))
                {
                    f4 = 0.5F;
                    flag1 = false;
                }
            }
        }
        else if (enumfacing == EnumFacing.SOUTH)
        {
            f4 = 0.5F;
            f5 = 1.0F;
            IBlockState iblockstate3 = blockAccess.getBlockState(pos.south());
            Block block2 = iblockstate3.getBlock();

            if (BlockStairs.isBlockStairs(block2) && blockstairs$enumhalf == iblockstate3.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing3 = iblockstate3.getValue(BlockStairs.FACING);

                if (enumfacing3 == EnumFacing.WEST && !BlockStairs.isSameStair(blockAccess, pos.east(), iblockstate))
                {
                    f3 = 0.5F;
                    flag1 = false;
                }
                else if (enumfacing3 == EnumFacing.EAST && !BlockStairs.isSameStair(blockAccess, pos.west(), iblockstate))
                {
                    f2 = 0.5F;
                    flag1 = false;
                }
            }
        }
        else if (enumfacing == EnumFacing.NORTH)
        {
            IBlockState iblockstate4 = blockAccess.getBlockState(pos.north());
            Block block3 = iblockstate4.getBlock();

            if (BlockStairs.isBlockStairs(block3) && blockstairs$enumhalf == iblockstate4.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing4 = iblockstate4.getValue(BlockStairs.FACING);

                if (enumfacing4 == EnumFacing.WEST && !BlockStairs.isSameStair(blockAccess, pos.east(), iblockstate))
                {
                    f3 = 0.5F;
                    flag1 = false;
                }
                else if (enumfacing4 == EnumFacing.EAST && !BlockStairs.isSameStair(blockAccess, pos.west(), iblockstate))
                {
                    f2 = 0.5F;
                    flag1 = false;
                }
            }
        }

        setBlockBounds(f2, f, f4, f3, f1, f5);
        return flag1;
    }

    public boolean func_176304_i(IBlockAccess blockAccess, BlockPos pos)
    {
        IBlockState iblockstate = blockAccess.getBlockState(pos);
        EnumFacing enumfacing = iblockstate.getValue(BlockStairs.FACING);
        BlockStairs.EnumHalf blockstairs$enumhalf = iblockstate.getValue(BlockStairs.HALF);
        boolean flag = blockstairs$enumhalf == BlockStairs.EnumHalf.TOP;
        float f = 0.5F;
        float f1 = 1.0F;

        if (flag)
        {
            f = 0.0F;
            f1 = 0.5F;
        }

        float f2 = 0.0F;
        float f3 = 0.5F;
        float f4 = 0.5F;
        float f5 = 1.0F;
        boolean flag1 = false;

        if (enumfacing == EnumFacing.EAST)
        {
            IBlockState iblockstate1 = blockAccess.getBlockState(pos.west());
            Block block = iblockstate1.getBlock();

            if (BlockStairs.isBlockStairs(block) && blockstairs$enumhalf == iblockstate1.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing1 = iblockstate1.getValue(BlockStairs.FACING);

                if (enumfacing1 == EnumFacing.NORTH && !BlockStairs.isSameStair(blockAccess, pos.north(), iblockstate))
                {
                    f4 = 0.0F;
                    f5 = 0.5F;
                    flag1 = true;
                }
                else if (enumfacing1 == EnumFacing.SOUTH && !BlockStairs.isSameStair(blockAccess, pos.south(), iblockstate))
                {
                    f4 = 0.5F;
                    f5 = 1.0F;
                    flag1 = true;
                }
            }
        }
        else if (enumfacing == EnumFacing.WEST)
        {
            IBlockState iblockstate2 = blockAccess.getBlockState(pos.east());
            Block block1 = iblockstate2.getBlock();

            if (BlockStairs.isBlockStairs(block1) && blockstairs$enumhalf == iblockstate2.getValue(BlockStairs.HALF))
            {
                f2 = 0.5F;
                f3 = 1.0F;
                EnumFacing enumfacing2 = iblockstate2.getValue(BlockStairs.FACING);

                if (enumfacing2 == EnumFacing.NORTH && !BlockStairs.isSameStair(blockAccess, pos.north(), iblockstate))
                {
                    f4 = 0.0F;
                    f5 = 0.5F;
                    flag1 = true;
                }
                else if (enumfacing2 == EnumFacing.SOUTH && !BlockStairs.isSameStair(blockAccess, pos.south(), iblockstate))
                {
                    f4 = 0.5F;
                    f5 = 1.0F;
                    flag1 = true;
                }
            }
        }
        else if (enumfacing == EnumFacing.SOUTH)
        {
            IBlockState iblockstate3 = blockAccess.getBlockState(pos.north());
            Block block2 = iblockstate3.getBlock();

            if (BlockStairs.isBlockStairs(block2) && blockstairs$enumhalf == iblockstate3.getValue(BlockStairs.HALF))
            {
                f4 = 0.0F;
                f5 = 0.5F;
                EnumFacing enumfacing3 = iblockstate3.getValue(BlockStairs.FACING);

                if (enumfacing3 == EnumFacing.WEST && !BlockStairs.isSameStair(blockAccess, pos.west(), iblockstate))
                {
                    flag1 = true;
                }
                else if (enumfacing3 == EnumFacing.EAST && !BlockStairs.isSameStair(blockAccess, pos.east(), iblockstate))
                {
                    f2 = 0.5F;
                    f3 = 1.0F;
                    flag1 = true;
                }
            }
        }
        else if (enumfacing == EnumFacing.NORTH)
        {
            IBlockState iblockstate4 = blockAccess.getBlockState(pos.south());
            Block block3 = iblockstate4.getBlock();

            if (BlockStairs.isBlockStairs(block3) && blockstairs$enumhalf == iblockstate4.getValue(BlockStairs.HALF))
            {
                EnumFacing enumfacing4 = iblockstate4.getValue(BlockStairs.FACING);

                if (enumfacing4 == EnumFacing.WEST && !BlockStairs.isSameStair(blockAccess, pos.west(), iblockstate))
                {
                    flag1 = true;
                }
                else if (enumfacing4 == EnumFacing.EAST && !BlockStairs.isSameStair(blockAccess, pos.east(), iblockstate))
                {
                    f2 = 0.5F;
                    f3 = 1.0F;
                    flag1 = true;
                }
            }
        }

        if (flag1)
        {
            setBlockBounds(f2, f, f4, f3, f1, f5);
        }

        return flag1;
    }

    /**
     * Add all collision boxes of this Block to the list that intersect with the given mask.
     */
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity)
    {
        setBaseCollisionBounds(worldIn, pos);
        super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        boolean flag = func_176306_h(worldIn, pos);
        super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);

        if (flag && func_176304_i(worldIn, pos))
        {
            super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        }

        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        modelBlock.randomDisplayTick(worldIn, pos, state, rand);
    }

    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        modelBlock.onBlockClicked(worldIn, pos, playerIn);
    }

    /**
     * Called when a player destroys this Block
     */
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state)
    {
        modelBlock.onBlockDestroyedByPlayer(worldIn, pos, state);
    }

    public int getMixedBrightnessForBlock(IBlockAccess worldIn, BlockPos pos)
    {
        return modelBlock.getMixedBrightnessForBlock(worldIn, pos);
    }

    /**
     * Returns how much this block can resist explosions from the passed in entity.
     */
    public float getExplosionResistance(Entity exploder)
    {
        return modelBlock.getExplosionResistance(exploder);
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return modelBlock.getBlockLayer();
    }

    /**
     * How many world ticks before ticking
     */
    public int tickRate(World worldIn)
    {
        return modelBlock.tickRate(worldIn);
    }

    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos)
    {
        return modelBlock.getSelectedBoundingBox(worldIn, pos);
    }

    public Vec3 modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3 motion)
    {
        return modelBlock.modifyAcceleration(worldIn, pos, entityIn, motion);
    }

    /**
     * Returns if this block is collidable (only used by Fire). Args: x, y, z
     */
    public boolean isCollidable()
    {
        return modelBlock.isCollidable();
    }

    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid)
    {
        return modelBlock.canCollideCheck(state, hitIfLiquid);
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return modelBlock.canPlaceBlockAt(worldIn, pos);
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        onNeighborBlockChange(worldIn, pos, modelState, Blocks.air);
        modelBlock.onBlockAdded(worldIn, pos, modelState);
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        modelBlock.breakBlock(worldIn, pos, modelState);
    }

    /**
     * Triggered whenever an entity collides with this block (enters into the block)
     */
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn)
    {
        modelBlock.onEntityCollidedWithBlock(worldIn, pos, entityIn);
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        modelBlock.updateTick(worldIn, pos, state, rand);
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return modelBlock.onBlockActivated(worldIn, pos, modelState, playerIn, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
    }

    /**
     * Called when this Block is destroyed by an Explosion
     */
    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn)
    {
        modelBlock.onBlockDestroyedByExplosion(worldIn, pos, explosionIn);
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IBlockState state)
    {
        return modelBlock.getMapColor(modelState);
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        IBlockState iblockstate = super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
        iblockstate = iblockstate.withProperty(BlockStairs.FACING, placer.getHorizontalFacing()).withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT);
        return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double)hitY <= 0.5D) ? iblockstate.withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM) : iblockstate.withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
    }

    /**
     * Ray traces through the blocks collision from start vector to end vector returning a ray trace hit.
     */
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end)
    {
        MovingObjectPosition[] amovingobjectposition = new MovingObjectPosition[8];
        IBlockState iblockstate = worldIn.getBlockState(pos);
        int i = iblockstate.getValue(BlockStairs.FACING).getHorizontalIndex();
        boolean flag = iblockstate.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP;
        int[] aint = BlockStairs.field_150150_a[i + (flag ? 4 : 0)];
        hasRaytraced = true;

        for (int j = 0; j < 8; ++j)
        {
            rayTracePass = j;

            if (Arrays.binarySearch(aint, j) < 0)
            {
                amovingobjectposition[j] = super.collisionRayTrace(worldIn, pos, start, end);
            }
        }

        for (int k : aint)
        {
            amovingobjectposition[k] = null;
        }

        MovingObjectPosition movingobjectposition1 = null;
        double d1 = 0.0D;

        for (MovingObjectPosition movingobjectposition : amovingobjectposition)
        {
            if (movingobjectposition != null)
            {
                double d0 = movingobjectposition.hitVec.squareDistanceTo(end);

                if (d0 > d1)
                {
                    movingobjectposition1 = movingobjectposition;
                    d1 = d0;
                }
            }
        }

        return movingobjectposition1;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        IBlockState iblockstate = getDefaultState().withProperty(BlockStairs.HALF, (meta & 4) > 0 ? BlockStairs.EnumHalf.TOP : BlockStairs.EnumHalf.BOTTOM);
        iblockstate = iblockstate.withProperty(BlockStairs.FACING, EnumFacing.getFront(5 - (meta & 3)));
        return iblockstate;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;

        if (state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP)
        {
            i |= 4;
        }

        i = i | 5 - state.getValue(BlockStairs.FACING).getIndex();
        return i;
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        if (func_176306_h(worldIn, pos))
        {
            switch (func_176305_g(worldIn, pos))
            {
                case 0:
                    state = state.withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT);
                    break;

                case 1:
                    state = state.withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.INNER_RIGHT);
                    break;

                case 2:
                    state = state.withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.INNER_LEFT);
            }
        }
        else
        {
            switch (func_176307_f(worldIn, pos))
            {
                case 0:
                    state = state.withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT);
                    break;

                case 1:
                    state = state.withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.OUTER_RIGHT);
                    break;

                case 2:
                    state = state.withProperty(BlockStairs.SHAPE, BlockStairs.EnumShape.OUTER_LEFT);
            }
        }

        return state;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockStairs.FACING, BlockStairs.HALF, BlockStairs.SHAPE);
    }

    public static enum EnumHalf implements IStringSerializable
    {
        TOP("top"),
        BOTTOM("bottom");

        private final String name;

        private EnumHalf(String name)
        {
            this.name = name;
        }

        public String toString()
        {
            return name;
        }

        public String getName()
        {
            return name;
        }
    }

    public static enum EnumShape implements IStringSerializable
    {
        STRAIGHT("straight"),
        INNER_LEFT("inner_left"),
        INNER_RIGHT("inner_right"),
        OUTER_LEFT("outer_left"),
        OUTER_RIGHT("outer_right");

        private final String name;

        private EnumShape(String name)
        {
            this.name = name;
        }

        public String toString()
        {
            return name;
        }

        public String getName()
        {
            return name;
        }
    }
}
