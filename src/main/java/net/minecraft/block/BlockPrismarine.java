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
import net.minecraft.util.StatCollector;

public class BlockPrismarine extends Block
{
    public static final PropertyEnum<BlockPrismarine.EnumType> VARIANT = PropertyEnum.create("variant", BlockPrismarine.EnumType.class);
    public static final int ROUGH_META = BlockPrismarine.EnumType.ROUGH.getMetadata();
    public static final int BRICKS_META = BlockPrismarine.EnumType.BRICKS.getMetadata();
    public static final int DARK_META = BlockPrismarine.EnumType.DARK.getMetadata();

    public BlockPrismarine()
    {
        super(Material.rock);
        setDefaultState(blockState.getBaseState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.ROUGH));
        setCreativeTab(CreativeTabs.tabBlock);
    }

    /**
     * Gets the localized name of this block. Used for the statistics page.
     */
    public String getLocalizedName()
    {
        return StatCollector.translateToLocal(getUnlocalizedName() + "." + BlockPrismarine.EnumType.ROUGH.getUnlocalizedName() + ".name");
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IBlockState state)
    {
        return state.getValue(BlockPrismarine.VARIANT) == BlockPrismarine.EnumType.ROUGH ? MapColor.cyanColor : MapColor.diamondColor;
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state)
    {
        return state.getValue(BlockPrismarine.VARIANT).getMetadata();
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(BlockPrismarine.VARIANT).getMetadata();
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockPrismarine.VARIANT);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.byMetadata(meta));
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        list.add(new ItemStack(itemIn, 1, BlockPrismarine.ROUGH_META));
        list.add(new ItemStack(itemIn, 1, BlockPrismarine.BRICKS_META));
        list.add(new ItemStack(itemIn, 1, BlockPrismarine.DARK_META));
    }

    public static enum EnumType implements IStringSerializable
    {
        ROUGH(0, "prismarine", "rough"),
        BRICKS(1, "prismarine_bricks", "bricks"),
        DARK(2, "dark_prismarine", "dark");

        private static final BlockPrismarine.EnumType[] META_LOOKUP = new BlockPrismarine.EnumType[EnumType.values().length];
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

        public static BlockPrismarine.EnumType byMetadata(int meta)
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
            for (BlockPrismarine.EnumType blockprismarine$enumtype : EnumType.values())
            {
                EnumType.META_LOOKUP[blockprismarine$enumtype.getMetadata()] = blockprismarine$enumtype;
            }
        }
    }
}
