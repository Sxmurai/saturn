package net.minecraft.block;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWall extends Block
{
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyEnum<BlockWall.EnumType> VARIANT = PropertyEnum.create("variant", BlockWall.EnumType.class);

    public BlockWall(Block modelBlock)
    {
        super(modelBlock.blockMaterial);
        setDefaultState(blockState.getBaseState().withProperty(BlockWall.UP, Boolean.valueOf(false)).withProperty(BlockWall.NORTH, Boolean.valueOf(false)).withProperty(BlockWall.EAST, Boolean.valueOf(false)).withProperty(BlockWall.SOUTH, Boolean.valueOf(false)).withProperty(BlockWall.WEST, Boolean.valueOf(false)).withProperty(BlockWall.VARIANT, BlockWall.EnumType.NORMAL));
        setHardness(modelBlock.blockHardness);
        setResistance(modelBlock.blockResistance / 3.0F);
        setStepSound(modelBlock.stepSound);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    /**
     * Gets the localized name of this block. Used for the statistics page.
     */
    public String getLocalizedName()
    {
        return StatCollector.translateToLocal(getUnlocalizedName() + "." + BlockWall.EnumType.NORMAL.getUnlocalizedName() + ".name");
    }

    public boolean isFullCube()
    {
        return false;
    }

    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return false;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube()
    {
        return false;
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        boolean flag = canConnectTo(worldIn, pos.north());
        boolean flag1 = canConnectTo(worldIn, pos.south());
        boolean flag2 = canConnectTo(worldIn, pos.west());
        boolean flag3 = canConnectTo(worldIn, pos.east());
        float f = 0.25F;
        float f1 = 0.75F;
        float f2 = 0.25F;
        float f3 = 0.75F;
        float f4 = 1.0F;

        if (flag)
        {
            f2 = 0.0F;
        }

        if (flag1)
        {
            f3 = 1.0F;
        }

        if (flag2)
        {
            f = 0.0F;
        }

        if (flag3)
        {
            f1 = 1.0F;
        }

        if (flag && flag1 && !flag2 && !flag3)
        {
            f4 = 0.8125F;
            f = 0.3125F;
            f1 = 0.6875F;
        }
        else if (!flag && !flag1 && flag2 && flag3)
        {
            f4 = 0.8125F;
            f2 = 0.3125F;
            f3 = 0.6875F;
        }

        setBlockBounds(f, 0.0F, f2, f1, f4, f3);
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        setBlockBoundsBasedOnState(worldIn, pos);
        maxY = 1.5D;
        return super.getCollisionBoundingBox(worldIn, pos, state);
    }

    public boolean canConnectTo(IBlockAccess worldIn, BlockPos pos)
    {
        Block block = worldIn.getBlockState(pos).getBlock();
        return block != Blocks.barrier && (block == this || block instanceof BlockFenceGate || (block.blockMaterial.isOpaque() && block.isFullCube() && block.blockMaterial != Material.gourd));
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        for (BlockWall.EnumType blockwall$enumtype : BlockWall.EnumType.values())
        {
            list.add(new ItemStack(itemIn, 1, blockwall$enumtype.getMetadata()));
        }
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state)
    {
        return state.getValue(BlockWall.VARIANT).getMetadata();
    }

    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return side != EnumFacing.DOWN || super.shouldSideBeRendered(worldIn, pos, side);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(BlockWall.VARIANT, BlockWall.EnumType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(BlockWall.VARIANT).getMetadata();
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state.withProperty(BlockWall.UP, Boolean.valueOf(!worldIn.isAirBlock(pos.up()))).withProperty(BlockWall.NORTH, Boolean.valueOf(canConnectTo(worldIn, pos.north()))).withProperty(BlockWall.EAST, Boolean.valueOf(canConnectTo(worldIn, pos.east()))).withProperty(BlockWall.SOUTH, Boolean.valueOf(canConnectTo(worldIn, pos.south()))).withProperty(BlockWall.WEST, Boolean.valueOf(canConnectTo(worldIn, pos.west())));
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockWall.UP, BlockWall.NORTH, BlockWall.EAST, BlockWall.WEST, BlockWall.SOUTH, BlockWall.VARIANT);
    }

    public static enum EnumType implements IStringSerializable
    {
        NORMAL(0, "cobblestone", "normal"),
        MOSSY(1, "mossy_cobblestone", "mossy");

        private static final BlockWall.EnumType[] META_LOOKUP = new BlockWall.EnumType[EnumType.values().length];
        private final int meta;
        private final String name;
        private final String unlocalizedName;

        private EnumType(int meta, String name, String unlocalizedName)
        {
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMetadata()
        {
            return meta;
        }

        public String toString()
        {
            return name;
        }

        public static BlockWall.EnumType byMetadata(int meta)
        {
            if (meta < 0 || meta >= EnumType.META_LOOKUP.length)
            {
                meta = 0;
            }

            return EnumType.META_LOOKUP[meta];
        }

        public String getName()
        {
            return name;
        }

        public String getUnlocalizedName()
        {
            return unlocalizedName;
        }

        static {
            for (BlockWall.EnumType blockwall$enumtype : EnumType.values())
            {
                EnumType.META_LOOKUP[blockwall$enumtype.getMetadata()] = blockwall$enumtype;
            }
        }
    }
}
