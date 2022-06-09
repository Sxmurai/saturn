package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockRedstoneDiode extends BlockDirectional
{
    /** Tells whether the repeater is powered or not */
    protected final boolean isRepeaterPowered;

    protected BlockRedstoneDiode(boolean powered)
    {
        super(Material.circuits);
        isRepeaterPowered = powered;
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
    }

    public boolean isFullCube()
    {
        return false;
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) && super.canPlaceBlockAt(worldIn, pos);
    }

    public boolean canBlockStay(World worldIn, BlockPos pos)
    {
        return World.doesBlockHaveSolidTopSurface(worldIn, pos.down());
    }

    /**
     * Called randomly when setTickRandomly is set to true (used by e.g. crops to grow, etc.)
     */
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!isLocked(worldIn, pos, state))
        {
            boolean flag = shouldBePowered(worldIn, pos, state);

            if (isRepeaterPowered && !flag)
            {
                worldIn.setBlockState(pos, getUnpoweredState(state), 2);
            }
            else if (!isRepeaterPowered)
            {
                worldIn.setBlockState(pos, getPoweredState(state), 2);

                if (!flag)
                {
                    worldIn.updateBlockTick(pos, getPoweredState(state).getBlock(), getTickDelay(state), -1);
                }
            }
        }
    }

    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return side.getAxis() != EnumFacing.Axis.Y;
    }

    protected boolean isPowered(IBlockState state)
    {
        return isRepeaterPowered;
    }

    public int getStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side)
    {
        return getWeakPower(worldIn, pos, state, side);
    }

    public int getWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side)
    {
        return !isPowered(state) ? 0 : (state.getValue(BlockDirectional.FACING) == side ? getActiveSignal(worldIn, pos, state) : 0);
    }

    /**
     * Called when a neighboring block changes.
     */
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        if (canBlockStay(worldIn, pos))
        {
            updateState(worldIn, pos, state);
        }
        else
        {
            dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);

            for (EnumFacing enumfacing : EnumFacing.values())
            {
                worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
            }
        }
    }

    protected void updateState(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!isLocked(worldIn, pos, state))
        {
            boolean flag = shouldBePowered(worldIn, pos, state);

            if ((isRepeaterPowered && !flag || !isRepeaterPowered && flag) && !worldIn.isBlockTickPending(pos, this))
            {
                int i = -1;

                if (isFacingTowardsRepeater(worldIn, pos, state))
                {
                    i = -3;
                }
                else if (isRepeaterPowered)
                {
                    i = -2;
                }

                worldIn.updateBlockTick(pos, this, getDelay(state), i);
            }
        }
    }

    public boolean isLocked(IBlockAccess worldIn, BlockPos pos, IBlockState state)
    {
        return false;
    }

    protected boolean shouldBePowered(World worldIn, BlockPos pos, IBlockState state)
    {
        return calculateInputStrength(worldIn, pos, state) > 0;
    }

    protected int calculateInputStrength(World worldIn, BlockPos pos, IBlockState state)
    {
        EnumFacing enumfacing = state.getValue(BlockDirectional.FACING);
        BlockPos blockpos = pos.offset(enumfacing);
        int i = worldIn.getRedstonePower(blockpos, enumfacing);

        if (i >= 15)
        {
            return i;
        }
        else
        {
            IBlockState iblockstate = worldIn.getBlockState(blockpos);
            return Math.max(i, iblockstate.getBlock() == Blocks.redstone_wire ? iblockstate.getValue(BlockRedstoneWire.POWER).intValue() : 0);
        }
    }

    protected int getPowerOnSides(IBlockAccess worldIn, BlockPos pos, IBlockState state)
    {
        EnumFacing enumfacing = state.getValue(BlockDirectional.FACING);
        EnumFacing enumfacing1 = enumfacing.rotateY();
        EnumFacing enumfacing2 = enumfacing.rotateYCCW();
        return Math.max(getPowerOnSide(worldIn, pos.offset(enumfacing1), enumfacing1), getPowerOnSide(worldIn, pos.offset(enumfacing2), enumfacing2));
    }

    protected int getPowerOnSide(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        return canPowerSide(block) ? (block == Blocks.redstone_wire ? iblockstate.getValue(BlockRedstoneWire.POWER).intValue() : worldIn.getStrongPower(pos, side)) : 0;
    }

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower()
    {
        return true;
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return getDefaultState().withProperty(BlockDirectional.FACING, placer.getHorizontalFacing().getOpposite());
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if (shouldBePowered(worldIn, pos, state))
        {
            worldIn.scheduleUpdate(pos, this, 1);
        }
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        notifyNeighbors(worldIn, pos, state);
    }

    protected void notifyNeighbors(World worldIn, BlockPos pos, IBlockState state)
    {
        EnumFacing enumfacing = state.getValue(BlockDirectional.FACING);
        BlockPos blockpos = pos.offset(enumfacing.getOpposite());
        worldIn.notifyBlockOfStateChange(blockpos, this);
        worldIn.notifyNeighborsOfStateExcept(blockpos, this, enumfacing);
    }

    /**
     * Called when a player destroys this Block
     */
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state)
    {
        if (isRepeaterPowered)
        {
            for (EnumFacing enumfacing : EnumFacing.values())
            {
                worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
            }
        }

        super.onBlockDestroyedByPlayer(worldIn, pos, state);
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube()
    {
        return false;
    }

    protected boolean canPowerSide(Block blockIn)
    {
        return blockIn.canProvidePower();
    }

    protected int getActiveSignal(IBlockAccess worldIn, BlockPos pos, IBlockState state)
    {
        return 15;
    }

    public static boolean isRedstoneRepeaterBlockID(Block blockIn)
    {
        return Blocks.unpowered_repeater.isAssociated(blockIn) || Blocks.unpowered_comparator.isAssociated(blockIn);
    }

    public boolean isAssociated(Block other)
    {
        return other == getPoweredState(getDefaultState()).getBlock() || other == getUnpoweredState(getDefaultState()).getBlock();
    }

    public boolean isFacingTowardsRepeater(World worldIn, BlockPos pos, IBlockState state)
    {
        EnumFacing enumfacing = state.getValue(BlockDirectional.FACING).getOpposite();
        BlockPos blockpos = pos.offset(enumfacing);
        return BlockRedstoneDiode.isRedstoneRepeaterBlockID(worldIn.getBlockState(blockpos).getBlock()) && worldIn.getBlockState(blockpos).getValue(BlockDirectional.FACING) != enumfacing;
    }

    protected int getTickDelay(IBlockState state)
    {
        return getDelay(state);
    }

    protected abstract int getDelay(IBlockState state);

    protected abstract IBlockState getPoweredState(IBlockState unpoweredState);

    protected abstract IBlockState getUnpoweredState(IBlockState poweredState);

    public boolean isAssociatedBlock(Block other)
    {
        return isAssociated(other);
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT;
    }
}
