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

public class BlockPlanks extends Block
{
    public static final PropertyEnum<BlockPlanks.EnumType> VARIANT = PropertyEnum.create("variant", BlockPlanks.EnumType.class);

    public BlockPlanks()
    {
        super(Material.wood);
        setDefaultState(blockState.getBaseState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.OAK));
        setCreativeTab(CreativeTabs.tabBlock);
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state)
    {
        return state.getValue(BlockPlanks.VARIANT).getMetadata();
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        for (BlockPlanks.EnumType blockplanks$enumtype : BlockPlanks.EnumType.values())
        {
            list.add(new ItemStack(itemIn, 1, blockplanks$enumtype.getMetadata()));
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.byMetadata(meta));
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IBlockState state)
    {
        return state.getValue(BlockPlanks.VARIANT).func_181070_c();
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(BlockPlanks.VARIANT).getMetadata();
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockPlanks.VARIANT);
    }

    public static enum EnumType implements IStringSerializable
    {
        OAK(0, "oak", MapColor.woodColor),
        SPRUCE(1, "spruce", MapColor.obsidianColor),
        BIRCH(2, "birch", MapColor.sandColor),
        JUNGLE(3, "jungle", MapColor.dirtColor),
        ACACIA(4, "acacia", MapColor.adobeColor),
        DARK_OAK(5, "dark_oak", "big_oak", MapColor.brownColor);

        private static final BlockPlanks.EnumType[] META_LOOKUP = new BlockPlanks.EnumType[EnumType.values().length];
        private final int meta;
        private final String name;
        private final String unlocalizedName;
        private final MapColor field_181071_k;

        private EnumType(int p_i46388_3_, String p_i46388_4_, MapColor p_i46388_5_)
        {
            this(p_i46388_3_, p_i46388_4_, p_i46388_4_, p_i46388_5_);
        }

        private EnumType(int p_i46389_3_, String p_i46389_4_, String p_i46389_5_, MapColor p_i46389_6_)
        {
            meta = p_i46389_3_;
            name = p_i46389_4_;
            unlocalizedName = p_i46389_5_;
            field_181071_k = p_i46389_6_;
        }

        public int getMetadata()
        {
            return meta;
        }

        public MapColor func_181070_c()
        {
            return field_181071_k;
        }

        public String toString()
        {
            return name;
        }

        public static BlockPlanks.EnumType byMetadata(int meta)
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
            for (BlockPlanks.EnumType blockplanks$enumtype : EnumType.values())
            {
                EnumType.META_LOOKUP[blockplanks$enumtype.getMetadata()] = blockplanks$enumtype;
            }
        }
    }
}
