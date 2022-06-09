package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTrapDoor extends Block
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool OPEN = PropertyBool.create("open");
    public static final PropertyEnum<BlockTrapDoor.DoorHalf> HALF = PropertyEnum.create("half", BlockTrapDoor.DoorHalf.class);

    protected BlockTrapDoor(Material materialIn)
    {
        super(materialIn);
        setDefaultState(blockState.getBaseState().withProperty(BlockTrapDoor.FACING, EnumFacing.NORTH).withProperty(BlockTrapDoor.OPEN, Boolean.valueOf(false)).withProperty(BlockTrapDoor.HALF, BlockTrapDoor.DoorHalf.BOTTOM));
        float f = 0.5F;
        float f1 = 1.0F;
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        setCreativeTab(CreativeTabs.tabRedstone);
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

    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return !worldIn.getBlockState(pos).getValue(BlockTrapDoor.OPEN).booleanValue();
    }

    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos)
    {
        setBlockBoundsBasedOnState(worldIn, pos);
        return super.getSelectedBoundingBox(worldIn, pos);
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        setBlockBoundsBasedOnState(worldIn, pos);
        return super.getCollisionBoundingBox(worldIn, pos, state);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        setBounds(worldIn.getBlockState(pos));
    }

    /**
     * Sets the block's bounds for rendering it as an item
     */
    public void setBlockBoundsForItemRender()
    {
        float f = 0.1875F;
        setBlockBounds(0.0F, 0.40625F, 0.0F, 1.0F, 0.59375F, 1.0F);
    }

    public void setBounds(IBlockState state)
    {
        if (state.getBlock() == this)
        {
            boolean flag = state.getValue(BlockTrapDoor.HALF) == BlockTrapDoor.DoorHalf.TOP;
            Boolean obool = state.getValue(BlockTrapDoor.OPEN);
            EnumFacing enumfacing = state.getValue(BlockTrapDoor.FACING);
            float f = 0.1875F;

            if (flag)
            {
                setBlockBounds(0.0F, 0.8125F, 0.0F, 1.0F, 1.0F, 1.0F);
            }
            else
            {
                setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1875F, 1.0F);
            }

            if (obool.booleanValue())
            {
                if (enumfacing == EnumFacing.NORTH)
                {
                    setBlockBounds(0.0F, 0.0F, 0.8125F, 1.0F, 1.0F, 1.0F);
                }

                if (enumfacing == EnumFacing.SOUTH)
                {
                    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.1875F);
                }

                if (enumfacing == EnumFacing.WEST)
                {
                    setBlockBounds(0.8125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                }

                if (enumfacing == EnumFacing.EAST)
                {
                    setBlockBounds(0.0F, 0.0F, 0.0F, 0.1875F, 1.0F, 1.0F);
                }
            }
        }
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (blockMaterial == Material.iron)
        {
            return true;
        }
        else
        {
            state = state.cycleProperty(BlockTrapDoor.OPEN);
            worldIn.setBlockState(pos, state, 2);
            worldIn.playAuxSFXAtEntity(playerIn, state.getValue(BlockTrapDoor.OPEN).booleanValue() ? 1003 : 1006, pos, 0);
            return true;
        }
    }

    /**
     * Called when a neighboring block changes.
     */
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        if (!worldIn.isRemote)
        {
            BlockPos blockpos = pos.offset(state.getValue(BlockTrapDoor.FACING).getOpposite());

            if (!BlockTrapDoor.isValidSupportBlock(worldIn.getBlockState(blockpos).getBlock()))
            {
                worldIn.setBlockToAir(pos);
                dropBlockAsItem(worldIn, pos, state, 0);
            }
            else
            {
                boolean flag = worldIn.isBlockPowered(pos);

                if (flag || neighborBlock.canProvidePower())
                {
                    boolean flag1 = state.getValue(BlockTrapDoor.OPEN).booleanValue();

                    if (flag1 != flag)
                    {
                        worldIn.setBlockState(pos, state.withProperty(BlockTrapDoor.OPEN, Boolean.valueOf(flag)), 2);
                        worldIn.playAuxSFXAtEntity(null, flag ? 1003 : 1006, pos, 0);
                    }
                }
            }
        }
    }

    /**
     * Ray traces through the blocks collision from start vector to end vector returning a ray trace hit.
     */
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end)
    {
        setBlockBoundsBasedOnState(worldIn, pos);
        return super.collisionRayTrace(worldIn, pos, start, end);
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        IBlockState iblockstate = getDefaultState();

        if (facing.getAxis().isHorizontal())
        {
            iblockstate = iblockstate.withProperty(BlockTrapDoor.FACING, facing).withProperty(BlockTrapDoor.OPEN, Boolean.valueOf(false));
            iblockstate = iblockstate.withProperty(BlockTrapDoor.HALF, hitY > 0.5F ? BlockTrapDoor.DoorHalf.TOP : BlockTrapDoor.DoorHalf.BOTTOM);
        }

        return iblockstate;
    }

    /**
     * Check whether this Block can be placed on the given side
     */
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side)
    {
        return !side.getAxis().isVertical() && BlockTrapDoor.isValidSupportBlock(worldIn.getBlockState(pos.offset(side.getOpposite())).getBlock());
    }

    protected static EnumFacing getFacing(int meta)
    {
        switch (meta & 3)
        {
            case 0:
                return EnumFacing.NORTH;

            case 1:
                return EnumFacing.SOUTH;

            case 2:
                return EnumFacing.WEST;

            case 3:
            default:
                return EnumFacing.EAST;
        }
    }

    protected static int getMetaForFacing(EnumFacing facing)
    {
        switch (facing)
        {
            case NORTH:
                return 0;

            case SOUTH:
                return 1;

            case WEST:
                return 2;

            case EAST:
            default:
                return 3;
        }
    }

    private static boolean isValidSupportBlock(Block blockIn)
    {
        return blockIn.blockMaterial.isOpaque() && blockIn.isFullCube() || blockIn == Blocks.glowstone || blockIn instanceof BlockSlab || blockIn instanceof BlockStairs;
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
        return getDefaultState().withProperty(BlockTrapDoor.FACING, BlockTrapDoor.getFacing(meta)).withProperty(BlockTrapDoor.OPEN, Boolean.valueOf((meta & 4) != 0)).withProperty(BlockTrapDoor.HALF, (meta & 8) == 0 ? BlockTrapDoor.DoorHalf.BOTTOM : BlockTrapDoor.DoorHalf.TOP);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | BlockTrapDoor.getMetaForFacing(state.getValue(BlockTrapDoor.FACING));

        if (state.getValue(BlockTrapDoor.OPEN).booleanValue())
        {
            i |= 4;
        }

        if (state.getValue(BlockTrapDoor.HALF) == BlockTrapDoor.DoorHalf.TOP)
        {
            i |= 8;
        }

        return i;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockTrapDoor.FACING, BlockTrapDoor.OPEN, BlockTrapDoor.HALF);
    }

    public static enum DoorHalf implements IStringSerializable
    {
        TOP("top"),
        BOTTOM("bottom");

        private final String name;

        private DoorHalf(String name)
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
