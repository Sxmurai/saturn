package net.minecraft.block;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public class BlockRedSandstone extends Block
{
    public static final PropertyEnum<BlockRedSandstone.EnumType> TYPE = PropertyEnum.create("type", BlockRedSandstone.EnumType.class);

    public BlockRedSandstone()
    {
        super(Material.rock, BlockSand.EnumType.RED_SAND.getMapColor());
        setDefaultState(blockState.getBaseState().withProperty(BlockRedSandstone.TYPE, BlockRedSandstone.EnumType.DEFAULT));
        setCreativeTab(CreativeTabs.tabBlock);
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state)
    {
        return state.getValue(BlockRedSandstone.TYPE).getMetadata();
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        for (BlockRedSandstone.EnumType blockredsandstone$enumtype : BlockRedSandstone.EnumType.values())
        {
            list.add(new ItemStack(itemIn, 1, blockredsandstone$enumtype.getMetadata()));
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(BlockRedSandstone.TYPE, BlockRedSandstone.EnumType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(BlockRedSandstone.TYPE).getMetadata();
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockRedSandstone.TYPE);
    }

    public static enum EnumType implements IStringSerializable
    {
        DEFAULT(0, "red_sandstone", "default"),
        CHISELED(1, "chiseled_red_sandstone", "chiseled"),
        SMOOTH(2, "smooth_red_sandstone", "smooth");

        private static final BlockRedSandstone.EnumType[] META_LOOKUP = new BlockRedSandstone.EnumType[EnumType.values().length];
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

        public static BlockRedSandstone.EnumType byMetadata(int meta)
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
            for (BlockRedSandstone.EnumType blockredsandstone$enumtype : EnumType.values())
            {
                EnumType.META_LOOKUP[blockredsandstone$enumtype.getMetadata()] = blockredsandstone$enumtype;
            }
        }
    }
}
