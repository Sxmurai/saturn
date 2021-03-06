package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStem extends BlockBush implements IGrowable
{
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 7);
    public static final PropertyDirection FACING = PropertyDirection.create("facing", new Predicate<EnumFacing>()
    {
        public boolean apply(EnumFacing p_apply_1_)
        {
            return p_apply_1_ != EnumFacing.DOWN;
        }
    });
    private final Block crop;

    protected BlockStem(Block crop)
    {
        setDefaultState(blockState.getBaseState().withProperty(BlockStem.AGE, Integer.valueOf(0)).withProperty(BlockStem.FACING, EnumFacing.UP));
        this.crop = crop;
        setTickRandomly(true);
        float f = 0.125F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.25F, 0.5F + f);
        setCreativeTab(null);
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        state = state.withProperty(BlockStem.FACING, EnumFacing.UP);

        for (Object enumfacing : EnumFacing.Plane.HORIZONTAL)
        {
            if (worldIn.getBlockState(pos.offset((EnumFacing) enumfacing)).getBlock() == crop)
            {
                state = state.withProperty(BlockStem.FACING, (EnumFacing) enumfacing);
                break;
            }
        }

        return state;
    }

    /**
     * is the block grass, dirt or farmland
     */
    protected boolean canPlaceBlockOn(Block ground)
    {
        return ground == Blocks.farmland;
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        super.updateTick(worldIn, pos, state, rand);

        if (worldIn.getLightFromNeighbors(pos.up()) >= 9)
        {
            float f = BlockCrops.getGrowthChance(this, worldIn, pos);

            if (rand.nextInt((int)(25.0F / f) + 1) == 0)
            {
                int i = state.getValue(BlockStem.AGE).intValue();

                if (i < 7)
                {
                    state = state.withProperty(BlockStem.AGE, Integer.valueOf(i + 1));
                    worldIn.setBlockState(pos, state, 2);
                }
                else
                {
                    for (Object enumfacing : EnumFacing.Plane.HORIZONTAL)
                    {
                        if (worldIn.getBlockState(pos.offset((EnumFacing) enumfacing)).getBlock() == crop)
                        {
                            return;
                        }
                    }

                    pos = pos.offset(EnumFacing.Plane.HORIZONTAL.random(rand));
                    Block block = worldIn.getBlockState(pos.down()).getBlock();

                    if (worldIn.getBlockState(pos).getBlock().blockMaterial == Material.air && (block == Blocks.farmland || block == Blocks.dirt || block == Blocks.grass))
                    {
                        worldIn.setBlockState(pos, crop.getDefaultState());
                    }
                }
            }
        }
    }

    public void growStem(World worldIn, BlockPos pos, IBlockState state)
    {
        int i = state.getValue(BlockStem.AGE).intValue() + MathHelper.getRandomIntegerInRange(worldIn.rand, 2, 5);
        worldIn.setBlockState(pos, state.withProperty(BlockStem.AGE, Integer.valueOf(Math.min(7, i))), 2);
    }

    public int getRenderColor(IBlockState state)
    {
        if (state.getBlock() != this)
        {
            return super.getRenderColor(state);
        }
        else
        {
            int i = state.getValue(BlockStem.AGE).intValue();
            int j = i * 32;
            int k = 255 - i * 8;
            int l = i * 4;
            return j << 16 | k << 8 | l;
        }
    }

    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass)
    {
        return getRenderColor(worldIn.getBlockState(pos));
    }

    /**
     * Sets the block's bounds for rendering it as an item
     */
    public void setBlockBoundsForItemRender()
    {
        float f = 0.125F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.25F, 0.5F + f);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        maxY = (float)(worldIn.getBlockState(pos).getValue(BlockStem.AGE).intValue() * 2 + 2) / 16.0F;
        float f = 0.125F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, (float) maxY, 0.5F + f);
    }

    /**
     * Spawns this Block's drops into the World as EntityItems.
     */
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);

        if (!worldIn.isRemote)
        {
            Item item = getSeedItem();

            if (item != null)
            {
                int i = state.getValue(BlockStem.AGE).intValue();

                for (int j = 0; j < 3; ++j)
                {
                    if (worldIn.rand.nextInt(15) <= i)
                    {
                        Block.spawnAsEntity(worldIn, pos, new ItemStack(item));
                    }
                }
            }
        }
    }

    protected Item getSeedItem()
    {
        return crop == Blocks.pumpkin ? Items.pumpkin_seeds : (crop == Blocks.melon_block ? Items.melon_seeds : null);
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return null;
    }

    public Item getItem(World worldIn, BlockPos pos)
    {
        Item item = getSeedItem();
        return item;
    }

    /**
     * Whether this IGrowable can grow
     */
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient)
    {
        return state.getValue(BlockStem.AGE).intValue() != 7;
    }

    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state)
    {
        return true;
    }

    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state)
    {
        growStem(worldIn, pos, state);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(BlockStem.AGE, Integer.valueOf(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(BlockStem.AGE).intValue();
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockStem.AGE, BlockStem.FACING);
    }
}
