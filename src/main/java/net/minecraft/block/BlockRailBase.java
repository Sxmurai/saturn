package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
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

public abstract class BlockRailBase extends Block
{
    protected final boolean isPowered;

    public static boolean isRailBlock(World worldIn, BlockPos pos)
    {
        return BlockRailBase.isRailBlock(worldIn.getBlockState(pos));
    }

    public static boolean isRailBlock(IBlockState state)
    {
        Block block = state.getBlock();
        return block == Blocks.rail || block == Blocks.golden_rail || block == Blocks.detector_rail || block == Blocks.activator_rail;
    }

    protected BlockRailBase(boolean isPowered)
    {
        super(Material.circuits);
        this.isPowered = isPowered;
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
        setCreativeTab(CreativeTabs.tabTransport);
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

    /**
     * Ray traces through the blocks collision from start vector to end vector returning a ray trace hit.
     */
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end)
    {
        setBlockBoundsBasedOnState(worldIn, pos);
        return super.collisionRayTrace(worldIn, pos, start, end);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = iblockstate.getBlock() == this ? iblockstate.getValue(getShapeProperty()) : null;

        if (blockrailbase$enumraildirection != null && blockrailbase$enumraildirection.isAscending())
        {
            setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.625F, 1.0F);
        }
        else
        {
            setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
        }
    }

    public boolean isFullCube()
    {
        return false;
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return World.doesBlockHaveSolidTopSurface(worldIn, pos.down());
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!worldIn.isRemote)
        {
            state = func_176564_a(worldIn, pos, state, true);

            if (isPowered)
            {
                onNeighborBlockChange(worldIn, pos, state, this);
            }
        }
    }

    /**
     * Called when a neighboring block changes.
     */
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        if (!worldIn.isRemote)
        {
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = state.getValue(getShapeProperty());
            boolean flag = !World.doesBlockHaveSolidTopSurface(worldIn, pos.down());

            if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_EAST && !World.doesBlockHaveSolidTopSurface(worldIn, pos.east()))
            {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_WEST && !World.doesBlockHaveSolidTopSurface(worldIn, pos.west()))
            {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_NORTH && !World.doesBlockHaveSolidTopSurface(worldIn, pos.north()))
            {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_SOUTH && !World.doesBlockHaveSolidTopSurface(worldIn, pos.south()))
            {
                flag = true;
            }

            if (flag)
            {
                dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }
            else
            {
                onNeighborChangedInternal(worldIn, pos, state, neighborBlock);
            }
        }
    }

    protected void onNeighborChangedInternal(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
    }

    protected IBlockState func_176564_a(World worldIn, BlockPos p_176564_2_, IBlockState p_176564_3_, boolean p_176564_4_)
    {
        return worldIn.isRemote ? p_176564_3_ : (new BlockRailBase.Rail(worldIn, p_176564_2_, p_176564_3_)).func_180364_a(worldIn.isBlockPowered(p_176564_2_), p_176564_4_).getBlockState();
    }

    public int getMobilityFlag()
    {
        return 0;
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT;
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);

        if (state.getValue(getShapeProperty()).isAscending())
        {
            worldIn.notifyNeighborsOfStateChange(pos.up(), this);
        }

        if (isPowered)
        {
            worldIn.notifyNeighborsOfStateChange(pos, this);
            worldIn.notifyNeighborsOfStateChange(pos.down(), this);
        }
    }

    public abstract IProperty<BlockRailBase.EnumRailDirection> getShapeProperty();

    public static enum EnumRailDirection implements IStringSerializable
    {
        NORTH_SOUTH(0, "north_south"),
        EAST_WEST(1, "east_west"),
        ASCENDING_EAST(2, "ascending_east"),
        ASCENDING_WEST(3, "ascending_west"),
        ASCENDING_NORTH(4, "ascending_north"),
        ASCENDING_SOUTH(5, "ascending_south"),
        SOUTH_EAST(6, "south_east"),
        SOUTH_WEST(7, "south_west"),
        NORTH_WEST(8, "north_west"),
        NORTH_EAST(9, "north_east");

        private static final BlockRailBase.EnumRailDirection[] META_LOOKUP = new BlockRailBase.EnumRailDirection[EnumRailDirection.values().length];
        private final int meta;
        private final String name;

        private EnumRailDirection(int meta, String name)
        {
            this.meta = meta;
            this.name = name;
        }

        public int getMetadata()
        {
            return meta;
        }

        public String toString()
        {
            return name;
        }

        public boolean isAscending()
        {
            return this == EnumRailDirection.ASCENDING_NORTH || this == EnumRailDirection.ASCENDING_EAST || this == EnumRailDirection.ASCENDING_SOUTH || this == EnumRailDirection.ASCENDING_WEST;
        }

        public static BlockRailBase.EnumRailDirection byMetadata(int meta)
        {
            if (meta < 0 || meta >= EnumRailDirection.META_LOOKUP.length)
            {
                meta = 0;
            }

            return EnumRailDirection.META_LOOKUP[meta];
        }

        public String getName()
        {
            return name;
        }

        static {
            for (BlockRailBase.EnumRailDirection blockrailbase$enumraildirection : EnumRailDirection.values())
            {
                EnumRailDirection.META_LOOKUP[blockrailbase$enumraildirection.getMetadata()] = blockrailbase$enumraildirection;
            }
        }
    }

    public class Rail
    {
        private final World world;
        private final BlockPos pos;
        private final BlockRailBase block;
        private IBlockState state;
        private final boolean isPowered;
        private final List<BlockPos> field_150657_g = Lists.newArrayList();

        public Rail(World worldIn, BlockPos pos, IBlockState state)
        {
            world = worldIn;
            this.pos = pos;
            this.state = state;
            block = (BlockRailBase)state.getBlock();
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = state.getValue(getShapeProperty());
            isPowered = block.isPowered;
            func_180360_a(blockrailbase$enumraildirection);
        }

        private void func_180360_a(BlockRailBase.EnumRailDirection p_180360_1_)
        {
            field_150657_g.clear();

            switch (p_180360_1_)
            {
                case NORTH_SOUTH:
                    field_150657_g.add(pos.north());
                    field_150657_g.add(pos.south());
                    break;

                case EAST_WEST:
                    field_150657_g.add(pos.west());
                    field_150657_g.add(pos.east());
                    break;

                case ASCENDING_EAST:
                    field_150657_g.add(pos.west());
                    field_150657_g.add(pos.east().up());
                    break;

                case ASCENDING_WEST:
                    field_150657_g.add(pos.west().up());
                    field_150657_g.add(pos.east());
                    break;

                case ASCENDING_NORTH:
                    field_150657_g.add(pos.north().up());
                    field_150657_g.add(pos.south());
                    break;

                case ASCENDING_SOUTH:
                    field_150657_g.add(pos.north());
                    field_150657_g.add(pos.south().up());
                    break;

                case SOUTH_EAST:
                    field_150657_g.add(pos.east());
                    field_150657_g.add(pos.south());
                    break;

                case SOUTH_WEST:
                    field_150657_g.add(pos.west());
                    field_150657_g.add(pos.south());
                    break;

                case NORTH_WEST:
                    field_150657_g.add(pos.west());
                    field_150657_g.add(pos.north());
                    break;

                case NORTH_EAST:
                    field_150657_g.add(pos.east());
                    field_150657_g.add(pos.north());
            }
        }

        private void func_150651_b()
        {
            for (int i = 0; i < field_150657_g.size(); ++i)
            {
                BlockRailBase.Rail blockrailbase$rail = findRailAt(field_150657_g.get(i));

                if (blockrailbase$rail != null && blockrailbase$rail.func_150653_a(this))
                {
                    field_150657_g.set(i, blockrailbase$rail.pos);
                }
                else
                {
                    field_150657_g.remove(i--);
                }
            }
        }

        private boolean hasRailAt(BlockPos pos)
        {
            return BlockRailBase.isRailBlock(world, pos) || BlockRailBase.isRailBlock(world, pos.up()) || BlockRailBase.isRailBlock(world, pos.down());
        }

        private BlockRailBase.Rail findRailAt(BlockPos pos)
        {
            IBlockState iblockstate = world.getBlockState(pos);

            if (BlockRailBase.isRailBlock(iblockstate))
            {
                return BlockRailBase.this.new Rail(world, pos, iblockstate);
            }
            else
            {
                BlockPos lvt_2_1_ = pos.up();
                iblockstate = world.getBlockState(lvt_2_1_);

                if (BlockRailBase.isRailBlock(iblockstate))
                {
                    return BlockRailBase.this.new Rail(world, lvt_2_1_, iblockstate);
                }
                else
                {
                    lvt_2_1_ = pos.down();
                    iblockstate = world.getBlockState(lvt_2_1_);
                    return BlockRailBase.isRailBlock(iblockstate) ? BlockRailBase.this.new Rail(world, lvt_2_1_, iblockstate) : null;
                }
            }
        }

        private boolean func_150653_a(BlockRailBase.Rail p_150653_1_)
        {
            return func_180363_c(p_150653_1_.pos);
        }

        private boolean func_180363_c(BlockPos p_180363_1_)
        {
            for (int i = 0; i < field_150657_g.size(); ++i)
            {
                BlockPos blockpos = field_150657_g.get(i);

                if (blockpos.getX() == p_180363_1_.getX() && blockpos.getZ() == p_180363_1_.getZ())
                {
                    return true;
                }
            }

            return false;
        }

        protected int countAdjacentRails()
        {
            int i = 0;

            for (Object enumfacing : EnumFacing.Plane.HORIZONTAL)
            {
                if (hasRailAt(pos.offset((EnumFacing) enumfacing)))
                {
                    ++i;
                }
            }

            return i;
        }

        private boolean func_150649_b(BlockRailBase.Rail rail)
        {
            return func_150653_a(rail) || field_150657_g.size() != 2;
        }

        private void func_150645_c(BlockRailBase.Rail p_150645_1_)
        {
            field_150657_g.add(p_150645_1_.pos);
            BlockPos blockpos = pos.north();
            BlockPos blockpos1 = pos.south();
            BlockPos blockpos2 = pos.west();
            BlockPos blockpos3 = pos.east();
            boolean flag = func_180363_c(blockpos);
            boolean flag1 = func_180363_c(blockpos1);
            boolean flag2 = func_180363_c(blockpos2);
            boolean flag3 = func_180363_c(blockpos3);
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = null;

            if (flag || flag1)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            if (flag2 || flag3)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
            }

            if (!isPowered)
            {
                if (flag1 && flag3 && !flag && !flag2)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                }

                if (flag1 && flag2 && !flag && !flag3)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                }

                if (flag && flag2 && !flag1 && !flag3)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
                }

                if (flag && flag3 && !flag1 && !flag2)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
                }
            }

            if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH)
            {
                if (BlockRailBase.isRailBlock(world, blockpos.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
                }

                if (BlockRailBase.isRailBlock(world, blockpos1.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
                }
            }

            if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.EAST_WEST)
            {
                if (BlockRailBase.isRailBlock(world, blockpos3.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_EAST;
                }

                if (BlockRailBase.isRailBlock(world, blockpos2.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_WEST;
                }
            }

            if (blockrailbase$enumraildirection == null)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            state = state.withProperty(block.getShapeProperty(), blockrailbase$enumraildirection);
            world.setBlockState(pos, state, 3);
        }

        private boolean func_180361_d(BlockPos p_180361_1_)
        {
            BlockRailBase.Rail blockrailbase$rail = findRailAt(p_180361_1_);

            if (blockrailbase$rail == null)
            {
                return false;
            }
            else
            {
                blockrailbase$rail.func_150651_b();
                return blockrailbase$rail.func_150649_b(this);
            }
        }

        public BlockRailBase.Rail func_180364_a(boolean p_180364_1_, boolean p_180364_2_)
        {
            BlockPos blockpos = pos.north();
            BlockPos blockpos1 = pos.south();
            BlockPos blockpos2 = pos.west();
            BlockPos blockpos3 = pos.east();
            boolean flag = func_180361_d(blockpos);
            boolean flag1 = func_180361_d(blockpos1);
            boolean flag2 = func_180361_d(blockpos2);
            boolean flag3 = func_180361_d(blockpos3);
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = null;

            if ((flag || flag1) && !flag2 && !flag3)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            if ((flag2 || flag3) && !flag && !flag1)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
            }

            if (!isPowered)
            {
                if (flag1 && flag3 && !flag && !flag2)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                }

                if (flag1 && flag2 && !flag && !flag3)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                }

                if (flag && flag2 && !flag1 && !flag3)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
                }

                if (flag && flag3 && !flag1 && !flag2)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
                }
            }

            if (blockrailbase$enumraildirection == null)
            {
                if (flag || flag1)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
                }

                if (flag2 || flag3)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
                }

                if (!isPowered)
                {
                    if (p_180364_1_)
                    {
                        if (flag1 && flag3)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                        }

                        if (flag2 && flag1)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                        }

                        if (flag3 && flag)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
                        }

                        if (flag && flag2)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
                        }
                    }
                    else
                    {
                        if (flag && flag2)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
                        }

                        if (flag3 && flag)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
                        }

                        if (flag2 && flag1)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                        }

                        if (flag1 && flag3)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                        }
                    }
                }
            }

            if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH)
            {
                if (BlockRailBase.isRailBlock(world, blockpos.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
                }

                if (BlockRailBase.isRailBlock(world, blockpos1.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
                }
            }

            if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.EAST_WEST)
            {
                if (BlockRailBase.isRailBlock(world, blockpos3.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_EAST;
                }

                if (BlockRailBase.isRailBlock(world, blockpos2.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_WEST;
                }
            }

            if (blockrailbase$enumraildirection == null)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            func_180360_a(blockrailbase$enumraildirection);
            state = state.withProperty(block.getShapeProperty(), blockrailbase$enumraildirection);

            if (p_180364_2_ || world.getBlockState(pos) != state)
            {
                world.setBlockState(pos, state, 3);

                for (int i = 0; i < field_150657_g.size(); ++i)
                {
                    BlockRailBase.Rail blockrailbase$rail = findRailAt(field_150657_g.get(i));

                    if (blockrailbase$rail != null)
                    {
                        blockrailbase$rail.func_150651_b();

                        if (blockrailbase$rail.func_150649_b(this))
                        {
                            blockrailbase$rail.func_150645_c(this);
                        }
                    }
                }
            }

            return this;
        }

        public IBlockState getBlockState()
        {
            return state;
        }
    }
}
