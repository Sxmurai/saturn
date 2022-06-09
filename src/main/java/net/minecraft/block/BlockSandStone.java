package net.minecraft.block;

import java.util.List;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public class BlockSandStone extends Block
{
    public static final PropertyEnum<BlockSandStone.EnumType> TYPE = PropertyEnum.create("type", BlockSandStone.EnumType.class);

    public BlockSandStone()
    {
        super(Material.rock);
        setDefaultState(blockState.getBaseState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.DEFAULT));
        setCreativeTab(CreativeTabs.tabBlock);
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state)
    {
        return state.getValue(BlockSandStone.TYPE).getMetadata();
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        for (BlockSandStone.EnumType blocksandstone$enumtype : BlockSandStone.EnumType.values())
        {
            list.add(new ItemStack(itemIn, 1, blocksandstone$enumtype.getMetadata()));
        }
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IBlockState state)
    {
        return MapColor.sandColor;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(BlockSandStone.TYPE).getMetadata();
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockSandStone.TYPE);
    }

    public static enum EnumType implements IStringSerializable
    {
        DEFAULT(0, "sandstone", "default"),
        CHISELED(1, "chiseled_sandstone", "chiseled"),
        SMOOTH(2, "smooth_sandstone", "smooth");

        private static final BlockSandStone.EnumType[] META_LOOKUP = new BlockSandStone.EnumType[EnumType.values().length];
        private final int metadata;
        private final String name;
        private final String unlocalizedName;

        private EnumType(int meta, String name, String unlocalizedName)
        {
            metadata = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMetadata()
        {
            return metadata;
        }

        public String toString()
        {
            return name;
        }

        public static BlockSandStone.EnumType byMetadata(int meta)
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
            for (BlockSandStone.EnumType blocksandstone$enumtype : EnumType.values())
            {
                EnumType.META_LOOKUP[blocksandstone$enumtype.getMetadata()] = blocksandstone$enumtype;
            }
        }
    }
}
