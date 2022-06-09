package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BlockTorch extends Block
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing", new Predicate<EnumFacing>()
    {
        public boolean apply(EnumFacing p_apply_1_)
        {
            return p_apply_1_ != EnumFacing.DOWN;
        }
    });

    protected BlockTorch()
    {
        super(Material.circuits);
        setDefaultState(blockState.getBaseState().withProperty(BlockTorch.FACING, EnumFacing.UP));
        setTickRandomly(true);
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        return null;
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

    private boolean canPlaceOn(World worldIn, BlockPos pos)
    {
        if (World.doesBlockHaveSolidTopSurface(worldIn, pos))
        {
            return true;
        }
        else
        {
            Block block = worldIn.getBlockState(pos).getBlock();
            return block instanceof BlockFence || block == Blocks.glass || block == Blocks.cobblestone_wall || block == Blocks.stained_glass;
        }
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        for (EnumFacing enumfacing : BlockTorch.FACING.getAllowedValues())
        {
            if (canPlaceAt(worldIn, pos, enumfacing))
            {
                return true;
            }
        }

        return false;
    }

    private boolean canPlaceAt(World worldIn, BlockPos pos, EnumFacing facing)
    {
        BlockPos blockpos = pos.offset(facing.getOpposite());
        boolean flag = facing.getAxis().isHorizontal();
        return flag && worldIn.isBlockNormalCube(blockpos, true) || facing.equals(EnumFacing.UP) && canPlaceOn(worldIn, blockpos);
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        if (canPlaceAt(worldIn, pos, facing))
        {
            return getDefaultState().withProperty(BlockTorch.FACING, facing);
        }
        else
        {
            for (Object enumfacing : EnumFacing.Plane.HORIZONTAL)
            {
                if (worldIn.isBlockNormalCube(pos.offset(((EnumFacing) enumfacing).getOpposite()), true))
                {
                    return getDefaultState().withProperty(BlockTorch.FACING, (EnumFacing) enumfacing);
                }
            }

            return getDefaultState();
        }
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        checkForDrop(worldIn, pos, state);
    }

    /**
     * Called when a neighboring block changes.
     */
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        onNeighborChangeInternal(worldIn, pos, state);
    }

    protected boolean onNeighborChangeInternal(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!checkForDrop(worldIn, pos, state))
        {
            return true;
        }
        else
        {
            EnumFacing enumfacing = state.getValue(BlockTorch.FACING);
            EnumFacing.Axis enumfacing$axis = enumfacing.getAxis();
            EnumFacing enumfacing1 = enumfacing.getOpposite();
            boolean flag = false;

            if (enumfacing$axis.isHorizontal() && !worldIn.isBlockNormalCube(pos.offset(enumfacing1), true))
            {
                flag = true;
            }
            else if (enumfacing$axis.isVertical() && !canPlaceOn(worldIn, pos.offset(enumfacing1)))
            {
                flag = true;
            }

            if (flag)
            {
                dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    protected boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state)
    {
        if (state.getBlock() == this && canPlaceAt(worldIn, pos, state.getValue(BlockTorch.FACING)))
        {
            return true;
        }
        else
        {
            if (worldIn.getBlockState(pos).getBlock() == this)
            {
                dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }

            return false;
        }
    }

    /**
     * Ray traces through the blocks collision from start vector to end vector returning a ray trace hit.
     */
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end)
    {
        EnumFacing enumfacing = worldIn.getBlockState(pos).getValue(BlockTorch.FACING);
        float f = 0.15F;

        if (enumfacing == EnumFacing.EAST)
        {
            setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
        }
        else if (enumfacing == EnumFacing.WEST)
        {
            setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
        }
        else if (enumfacing == EnumFacing.SOUTH)
        {
            setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
        }
        else if (enumfacing == EnumFacing.NORTH)
        {
            setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
        }
        else
        {
            f = 0.1F;
            setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.6F, 0.5F + f);
        }

        return super.collisionRayTrace(worldIn, pos, start, end);
    }

    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        EnumFacing enumfacing = state.getValue(BlockTorch.FACING);
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.7D;
        double d2 = (double)pos.getZ() + 0.5D;
        double d3 = 0.22D;
        double d4 = 0.27D;

        if (enumfacing.getAxis().isHorizontal())
        {
            EnumFacing enumfacing1 = enumfacing.getOpposite();
            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4 * (double)enumfacing1.getFrontOffsetX(), d1 + d3, d2 + d4 * (double)enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
            worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + d4 * (double)enumfacing1.getFrontOffsetX(), d1 + d3, d2 + d4 * (double)enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
        }
        else
        {
            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            worldIn.spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
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
        IBlockState iblockstate = getDefaultState();

        switch (meta)
        {
            case 1:
                iblockstate = iblockstate.withProperty(BlockTorch.FACING, EnumFacing.EAST);
                break;

            case 2:
                iblockstate = iblockstate.withProperty(BlockTorch.FACING, EnumFacing.WEST);
                break;

            case 3:
                iblockstate = iblockstate.withProperty(BlockTorch.FACING, EnumFacing.SOUTH);
                break;

            case 4:
                iblockstate = iblockstate.withProperty(BlockTorch.FACING, EnumFacing.NORTH);
                break;

            case 5:
            default:
                iblockstate = iblockstate.withProperty(BlockTorch.FACING, EnumFacing.UP);
        }

        return iblockstate;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;

        switch (state.getValue(BlockTorch.FACING))
        {
            case EAST:
                i = i | 1;
                break;

            case WEST:
                i = i | 2;
                break;

            case SOUTH:
                i = i | 3;
                break;

            case NORTH:
                i = i | 4;
                break;

            case DOWN:
            case UP:
            default:
                i = i | 5;
        }

        return i;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockTorch.FACING);
    }
}
