package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockLever extends Block
{
    public static final PropertyEnum<BlockLever.EnumOrientation> FACING = PropertyEnum.create("facing", BlockLever.EnumOrientation.class);
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    protected BlockLever()
    {
        super(Material.circuits);
        setDefaultState(blockState.getBaseState().withProperty(BlockLever.FACING, BlockLever.EnumOrientation.NORTH).withProperty(BlockLever.POWERED, Boolean.valueOf(false)));
        setCreativeTab(CreativeTabs.tabRedstone);
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

    /**
     * Check whether this Block can be placed on the given side
     */
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side)
    {
        return BlockLever.func_181090_a(worldIn, pos, side.getOpposite());
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        for (EnumFacing enumfacing : EnumFacing.values())
        {
            if (BlockLever.func_181090_a(worldIn, pos, enumfacing))
            {
                return true;
            }
        }

        return false;
    }

    protected static boolean func_181090_a(World p_181090_0_, BlockPos p_181090_1_, EnumFacing p_181090_2_)
    {
        return BlockButton.func_181088_a(p_181090_0_, p_181090_1_, p_181090_2_);
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        IBlockState iblockstate = getDefaultState().withProperty(BlockLever.POWERED, Boolean.valueOf(false));

        if (BlockLever.func_181090_a(worldIn, pos, facing.getOpposite()))
        {
            return iblockstate.withProperty(BlockLever.FACING, BlockLever.EnumOrientation.forFacings(facing, placer.getHorizontalFacing()));
        }
        else
        {
            for (Object enumfacing : EnumFacing.Plane.HORIZONTAL)
            {
                if (enumfacing != facing && BlockLever.func_181090_a(worldIn, pos, ((EnumFacing) enumfacing).getOpposite()))
                {
                    return iblockstate.withProperty(BlockLever.FACING, BlockLever.EnumOrientation.forFacings((EnumFacing) enumfacing, placer.getHorizontalFacing()));
                }
            }

            if (World.doesBlockHaveSolidTopSurface(worldIn, pos.down()))
            {
                return iblockstate.withProperty(BlockLever.FACING, BlockLever.EnumOrientation.forFacings(EnumFacing.UP, placer.getHorizontalFacing()));
            }
            else
            {
                return iblockstate;
            }
        }
    }

    public static int getMetadataForFacing(EnumFacing facing)
    {
        switch (facing)
        {
            case DOWN:
                return 0;

            case UP:
                return 5;

            case NORTH:
                return 4;

            case SOUTH:
                return 3;

            case WEST:
                return 2;

            case EAST:
                return 1;

            default:
                return -1;
        }
    }

    /**
     * Called when a neighboring block changes.
     */
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        if (func_181091_e(worldIn, pos, state) && !BlockLever.func_181090_a(worldIn, pos, state.getValue(BlockLever.FACING).getFacing().getOpposite()))
        {
            dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }

    private boolean func_181091_e(World p_181091_1_, BlockPos p_181091_2_, IBlockState p_181091_3_)
    {
        if (canPlaceBlockAt(p_181091_1_, p_181091_2_))
        {
            return true;
        }
        else
        {
            dropBlockAsItem(p_181091_1_, p_181091_2_, p_181091_3_, 0);
            p_181091_1_.setBlockToAir(p_181091_2_);
            return false;
        }
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        float f = 0.1875F;

        switch (worldIn.getBlockState(pos).getValue(BlockLever.FACING))
        {
            case EAST:
                setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
                break;

            case WEST:
                setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
                break;

            case SOUTH:
                setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
                break;

            case NORTH:
                setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
                break;

            case UP_Z:
            case UP_X:
                f = 0.25F;
                setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.6F, 0.5F + f);
                break;

            case DOWN_X:
            case DOWN_Z:
                f = 0.25F;
                setBlockBounds(0.5F - f, 0.4F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
        }
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            return true;
        }
        else
        {
            state = state.cycleProperty(BlockLever.POWERED);
            worldIn.setBlockState(pos, state, 3);
            worldIn.playSoundEffect((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, "random.click", 0.3F, state.getValue(BlockLever.POWERED).booleanValue() ? 0.6F : 0.5F);
            worldIn.notifyNeighborsOfStateChange(pos, this);
            EnumFacing enumfacing = state.getValue(BlockLever.FACING).getFacing();
            worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing.getOpposite()), this);
            return true;
        }
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if (state.getValue(BlockLever.POWERED).booleanValue())
        {
            worldIn.notifyNeighborsOfStateChange(pos, this);
            EnumFacing enumfacing = state.getValue(BlockLever.FACING).getFacing();
            worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing.getOpposite()), this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    public int getWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side)
    {
        return state.getValue(BlockLever.POWERED).booleanValue() ? 15 : 0;
    }

    public int getStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side)
    {
        return !state.getValue(BlockLever.POWERED).booleanValue() ? 0 : (state.getValue(BlockLever.FACING).getFacing() == side ? 15 : 0);
    }

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower()
    {
        return true;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(BlockLever.FACING, BlockLever.EnumOrientation.byMetadata(meta & 7)).withProperty(BlockLever.POWERED, Boolean.valueOf((meta & 8) > 0));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | state.getValue(BlockLever.FACING).getMetadata();

        if (state.getValue(BlockLever.POWERED).booleanValue())
        {
            i |= 8;
        }

        return i;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockLever.FACING, BlockLever.POWERED);
    }

    public static enum EnumOrientation implements IStringSerializable
    {
        DOWN_X(0, "down_x", EnumFacing.DOWN),
        EAST(1, "east", EnumFacing.EAST),
        WEST(2, "west", EnumFacing.WEST),
        SOUTH(3, "south", EnumFacing.SOUTH),
        NORTH(4, "north", EnumFacing.NORTH),
        UP_Z(5, "up_z", EnumFacing.UP),
        UP_X(6, "up_x", EnumFacing.UP),
        DOWN_Z(7, "down_z", EnumFacing.DOWN);

        private static final BlockLever.EnumOrientation[] META_LOOKUP = new BlockLever.EnumOrientation[EnumOrientation.values().length];
        private final int meta;
        private final String name;
        private final EnumFacing facing;

        private EnumOrientation(int meta, String name, EnumFacing facing)
        {
            this.meta = meta;
            this.name = name;
            this.facing = facing;
        }

        public int getMetadata()
        {
            return meta;
        }

        public EnumFacing getFacing()
        {
            return facing;
        }

        public String toString()
        {
            return name;
        }

        public static BlockLever.EnumOrientation byMetadata(int meta)
        {
            if (meta < 0 || meta >= EnumOrientation.META_LOOKUP.length)
            {
                meta = 0;
            }

            return EnumOrientation.META_LOOKUP[meta];
        }

        public static BlockLever.EnumOrientation forFacings(EnumFacing clickedSide, EnumFacing entityFacing)
        {
            switch (clickedSide)
            {
                case DOWN:
                    switch (entityFacing.getAxis())
                    {
                        case X:
                            return EnumOrientation.DOWN_X;

                        case Z:
                            return EnumOrientation.DOWN_Z;

                        default:
                            throw new IllegalArgumentException("Invalid entityFacing " + entityFacing + " for facing " + clickedSide);
                    }

                case UP:
                    switch (entityFacing.getAxis())
                    {
                        case X:
                            return EnumOrientation.UP_X;

                        case Z:
                            return EnumOrientation.UP_Z;

                        default:
                            throw new IllegalArgumentException("Invalid entityFacing " + entityFacing + " for facing " + clickedSide);
                    }

                case NORTH:
                    return EnumOrientation.NORTH;

                case SOUTH:
                    return EnumOrientation.SOUTH;

                case WEST:
                    return EnumOrientation.WEST;

                case EAST:
                    return EnumOrientation.EAST;

                default:
                    throw new IllegalArgumentException("Invalid facing: " + clickedSide);
            }
        }

        public String getName()
        {
            return name;
        }

        static {
            for (BlockLever.EnumOrientation blocklever$enumorientation : EnumOrientation.values())
            {
                EnumOrientation.META_LOOKUP[blocklever$enumorientation.getMetadata()] = blocklever$enumorientation;
            }
        }
    }
}
