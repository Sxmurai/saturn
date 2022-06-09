package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockLadder extends Block
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    protected BlockLadder()
    {
        super(Material.circuits);
        setDefaultState(blockState.getBaseState().withProperty(BlockLadder.FACING, EnumFacing.NORTH));
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        setBlockBoundsBasedOnState(worldIn, pos);
        return super.getCollisionBoundingBox(worldIn, pos, state);
    }

    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos)
    {
        setBlockBoundsBasedOnState(worldIn, pos);
        return super.getSelectedBoundingBox(worldIn, pos);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() == this)
        {
            float f = 0.125F;

            switch (iblockstate.getValue(BlockLadder.FACING))
            {
                case NORTH:
                    setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
                    break;

                case SOUTH:
                    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
                    break;

                case WEST:
                    setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                    break;

                case EAST:
                default:
                    setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
            }
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

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos.west()).getBlock().isNormalCube() || (worldIn.getBlockState(pos.east()).getBlock().isNormalCube() || (worldIn.getBlockState(pos.north()).getBlock().isNormalCube() || worldIn.getBlockState(pos.south()).getBlock().isNormalCube()));
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        if (facing.getAxis().isHorizontal() && canBlockStay(worldIn, pos, facing))
        {
            return getDefaultState().withProperty(BlockLadder.FACING, facing);
        }
        else
        {
            for (Object enumfacing : EnumFacing.Plane.HORIZONTAL)
            {
                if (canBlockStay(worldIn, pos, (EnumFacing) enumfacing))
                {
                    return getDefaultState().withProperty(BlockLadder.FACING, (EnumFacing)enumfacing);
                }
            }

            return getDefaultState();
        }
    }

    /**
     * Called when a neighboring block changes.
     */
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        EnumFacing enumfacing = state.getValue(BlockLadder.FACING);

        if (!canBlockStay(worldIn, pos, enumfacing))
        {
            dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }

        super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
    }

    protected boolean canBlockStay(World worldIn, BlockPos pos, EnumFacing facing)
    {
        return worldIn.getBlockState(pos.offset(facing.getOpposite())).getBlock().isNormalCube();
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return getDefaultState().withProperty(BlockLadder.FACING, enumfacing);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(BlockLadder.FACING).getIndex();
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockLadder.FACING);
    }
}
