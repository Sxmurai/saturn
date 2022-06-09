package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockVine extends Block
{
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool[] ALL_FACES = new PropertyBool[] {BlockVine.UP, BlockVine.NORTH, BlockVine.SOUTH, BlockVine.WEST, BlockVine.EAST};

    public BlockVine()
    {
        super(Material.vine);
        setDefaultState(blockState.getBaseState().withProperty(BlockVine.UP, Boolean.valueOf(false)).withProperty(BlockVine.NORTH, Boolean.valueOf(false)).withProperty(BlockVine.EAST, Boolean.valueOf(false)).withProperty(BlockVine.SOUTH, Boolean.valueOf(false)).withProperty(BlockVine.WEST, Boolean.valueOf(false)));
        setTickRandomly(true);
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state.withProperty(BlockVine.UP, Boolean.valueOf(worldIn.getBlockState(pos.up()).getBlock().isBlockNormalCube()));
    }

    /**
     * Sets the block's bounds for rendering it as an item
     */
    public void setBlockBoundsForItemRender()
    {
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
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
     * Whether this Block can be replaced directly by other blocks (true for e.g. tall grass)
     */
    public boolean isReplaceable(World worldIn, BlockPos pos)
    {
        return true;
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        float f = 0.0625F;
        float f1 = 1.0F;
        float f2 = 1.0F;
        float f3 = 1.0F;
        float f4 = 0.0F;
        float f5 = 0.0F;
        float f6 = 0.0F;
        boolean flag = false;

        if (worldIn.getBlockState(pos).getValue(BlockVine.WEST).booleanValue())
        {
            f4 = Math.max(f4, 0.0625F);
            f1 = 0.0F;
            f2 = 0.0F;
            f5 = 1.0F;
            f3 = 0.0F;
            f6 = 1.0F;
            flag = true;
        }

        if (worldIn.getBlockState(pos).getValue(BlockVine.EAST).booleanValue())
        {
            f1 = Math.min(f1, 0.9375F);
            f4 = 1.0F;
            f2 = 0.0F;
            f5 = 1.0F;
            f3 = 0.0F;
            f6 = 1.0F;
            flag = true;
        }

        if (worldIn.getBlockState(pos).getValue(BlockVine.NORTH).booleanValue())
        {
            f6 = Math.max(f6, 0.0625F);
            f3 = 0.0F;
            f1 = 0.0F;
            f4 = 1.0F;
            f2 = 0.0F;
            f5 = 1.0F;
            flag = true;
        }

        if (worldIn.getBlockState(pos).getValue(BlockVine.SOUTH).booleanValue())
        {
            f3 = Math.min(f3, 0.9375F);
            f6 = 1.0F;
            f1 = 0.0F;
            f4 = 1.0F;
            f2 = 0.0F;
            f5 = 1.0F;
            flag = true;
        }

        if (!flag && canPlaceOn(worldIn.getBlockState(pos.up()).getBlock()))
        {
            f2 = Math.min(f2, 0.9375F);
            f5 = 1.0F;
            f1 = 0.0F;
            f4 = 1.0F;
            f3 = 0.0F;
            f6 = 1.0F;
        }

        setBlockBounds(f1, f2, f3, f4, f5, f6);
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        return null;
    }

    /**
     * Check whether this Block can be placed on the given side
     */
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side)
    {
        switch (side)
        {
            case UP:
                return canPlaceOn(worldIn.getBlockState(pos.up()).getBlock());

            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                return canPlaceOn(worldIn.getBlockState(pos.offset(side.getOpposite())).getBlock());

            default:
                return false;
        }
    }

    private boolean canPlaceOn(Block blockIn)
    {
        return blockIn.isFullCube() && blockIn.blockMaterial.blocksMovement();
    }

    private boolean recheckGrownSides(World worldIn, BlockPos pos, IBlockState state)
    {
        IBlockState iblockstate = state;

        for (Object enumfacing : EnumFacing.Plane.HORIZONTAL)
        {
            PropertyBool propertybool = BlockVine.getPropertyFor((EnumFacing) enumfacing);

            if (state.getValue(propertybool).booleanValue() && !canPlaceOn(worldIn.getBlockState(pos.offset((EnumFacing) enumfacing)).getBlock()))
            {
                IBlockState iblockstate1 = worldIn.getBlockState(pos.up());

                if (iblockstate1.getBlock() != this || !iblockstate1.getValue(propertybool).booleanValue())
                {
                    state = state.withProperty(propertybool, Boolean.valueOf(false));
                }
            }
        }

        if (BlockVine.getNumGrownFaces(state) == 0)
        {
            return false;
        }
        else
        {
            if (iblockstate != state)
            {
                worldIn.setBlockState(pos, state, 2);
            }

            return true;
        }
    }

    public int getBlockColor()
    {
        return ColorizerFoliage.getFoliageColorBasic();
    }

    public int getRenderColor(IBlockState state)
    {
        return ColorizerFoliage.getFoliageColorBasic();
    }

    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass)
    {
        return worldIn.getBiomeGenForCoords(pos).getFoliageColorAtPos(pos);
    }

    /**
     * Called when a neighboring block changes.
     */
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        if (!worldIn.isRemote && !recheckGrownSides(worldIn, pos, state))
        {
            dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            if (worldIn.rand.nextInt(4) == 0)
            {
                int i = 4;
                int j = 5;
                boolean flag = false;
                label62:

                for (int k = -i; k <= i; ++k)
                {
                    for (int l = -i; l <= i; ++l)
                    {
                        for (int i1 = -1; i1 <= 1; ++i1)
                        {
                            if (worldIn.getBlockState(pos.add(k, i1, l)).getBlock() == this)
                            {
                                --j;

                                if (j <= 0)
                                {
                                    flag = true;
                                    break label62;
                                }
                            }
                        }
                    }
                }

                EnumFacing enumfacing1 = EnumFacing.random(rand);
                BlockPos blockpos1 = pos.up();

                if (enumfacing1 == EnumFacing.UP && pos.getY() < 255 && worldIn.isAirBlock(blockpos1))
                {
                    if (!flag)
                    {
                        IBlockState iblockstate2 = state;

                        for (Object enumfacing3 : EnumFacing.Plane.HORIZONTAL)
                        {
                            if (rand.nextBoolean() || !canPlaceOn(worldIn.getBlockState(blockpos1.offset((EnumFacing) enumfacing3)).getBlock()))
                            {
                                iblockstate2 = iblockstate2.withProperty(BlockVine.getPropertyFor((EnumFacing) enumfacing3), Boolean.valueOf(false));
                            }
                        }

                        if (iblockstate2.getValue(BlockVine.NORTH).booleanValue() || iblockstate2.getValue(BlockVine.EAST).booleanValue() || iblockstate2.getValue(BlockVine.SOUTH).booleanValue() || iblockstate2.getValue(BlockVine.WEST).booleanValue())
                        {
                            worldIn.setBlockState(blockpos1, iblockstate2, 2);
                        }
                    }
                }
                else if (enumfacing1.getAxis().isHorizontal() && !state.getValue(BlockVine.getPropertyFor(enumfacing1)).booleanValue())
                {
                    if (!flag)
                    {
                        BlockPos blockpos3 = pos.offset(enumfacing1);
                        Block block1 = worldIn.getBlockState(blockpos3).getBlock();

                        if (block1.blockMaterial == Material.air)
                        {
                            EnumFacing enumfacing2 = enumfacing1.rotateY();
                            EnumFacing enumfacing4 = enumfacing1.rotateYCCW();
                            boolean flag1 = state.getValue(BlockVine.getPropertyFor(enumfacing2)).booleanValue();
                            boolean flag2 = state.getValue(BlockVine.getPropertyFor(enumfacing4)).booleanValue();
                            BlockPos blockpos4 = blockpos3.offset(enumfacing2);
                            BlockPos blockpos = blockpos3.offset(enumfacing4);

                            if (flag1 && canPlaceOn(worldIn.getBlockState(blockpos4).getBlock()))
                            {
                                worldIn.setBlockState(blockpos3, getDefaultState().withProperty(BlockVine.getPropertyFor(enumfacing2), Boolean.valueOf(true)), 2);
                            }
                            else if (flag2 && canPlaceOn(worldIn.getBlockState(blockpos).getBlock()))
                            {
                                worldIn.setBlockState(blockpos3, getDefaultState().withProperty(BlockVine.getPropertyFor(enumfacing4), Boolean.valueOf(true)), 2);
                            }
                            else if (flag1 && worldIn.isAirBlock(blockpos4) && canPlaceOn(worldIn.getBlockState(pos.offset(enumfacing2)).getBlock()))
                            {
                                worldIn.setBlockState(blockpos4, getDefaultState().withProperty(BlockVine.getPropertyFor(enumfacing1.getOpposite()), Boolean.valueOf(true)), 2);
                            }
                            else if (flag2 && worldIn.isAirBlock(blockpos) && canPlaceOn(worldIn.getBlockState(pos.offset(enumfacing4)).getBlock()))
                            {
                                worldIn.setBlockState(blockpos, getDefaultState().withProperty(BlockVine.getPropertyFor(enumfacing1.getOpposite()), Boolean.valueOf(true)), 2);
                            }
                            else if (canPlaceOn(worldIn.getBlockState(blockpos3.up()).getBlock()))
                            {
                                worldIn.setBlockState(blockpos3, getDefaultState(), 2);
                            }
                        }
                        else if (block1.blockMaterial.isOpaque() && block1.isFullCube())
                        {
                            worldIn.setBlockState(pos, state.withProperty(BlockVine.getPropertyFor(enumfacing1), Boolean.valueOf(true)), 2);
                        }
                    }
                }
                else
                {
                    if (pos.getY() > 1)
                    {
                        BlockPos blockpos2 = pos.down();
                        IBlockState iblockstate = worldIn.getBlockState(blockpos2);
                        Block block = iblockstate.getBlock();

                        if (block.blockMaterial == Material.air)
                        {
                            IBlockState iblockstate1 = state;

                            for (Object enumfacing : EnumFacing.Plane.HORIZONTAL)
                            {
                                if (rand.nextBoolean())
                                {
                                    iblockstate1 = iblockstate1.withProperty(BlockVine.getPropertyFor((EnumFacing) enumfacing), Boolean.valueOf(false));
                                }
                            }

                            if (iblockstate1.getValue(BlockVine.NORTH).booleanValue() || iblockstate1.getValue(BlockVine.EAST).booleanValue() || iblockstate1.getValue(BlockVine.SOUTH).booleanValue() || iblockstate1.getValue(BlockVine.WEST).booleanValue())
                            {
                                worldIn.setBlockState(blockpos2, iblockstate1, 2);
                            }
                        }
                        else if (block == this)
                        {
                            IBlockState iblockstate3 = iblockstate;

                            for (Object enumfacing5 : EnumFacing.Plane.HORIZONTAL)
                            {
                                PropertyBool propertybool = BlockVine.getPropertyFor((EnumFacing) enumfacing5);

                                if (rand.nextBoolean() && state.getValue(propertybool).booleanValue())
                                {
                                    iblockstate3 = iblockstate3.withProperty(propertybool, Boolean.valueOf(true));
                                }
                            }

                            if (iblockstate3.getValue(BlockVine.NORTH).booleanValue() || iblockstate3.getValue(BlockVine.EAST).booleanValue() || iblockstate3.getValue(BlockVine.SOUTH).booleanValue() || iblockstate3.getValue(BlockVine.WEST).booleanValue())
                            {
                                worldIn.setBlockState(blockpos2, iblockstate3, 2);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        IBlockState iblockstate = getDefaultState().withProperty(BlockVine.UP, Boolean.valueOf(false)).withProperty(BlockVine.NORTH, Boolean.valueOf(false)).withProperty(BlockVine.EAST, Boolean.valueOf(false)).withProperty(BlockVine.SOUTH, Boolean.valueOf(false)).withProperty(BlockVine.WEST, Boolean.valueOf(false));
        return facing.getAxis().isHorizontal() ? iblockstate.withProperty(BlockVine.getPropertyFor(facing.getOpposite()), Boolean.valueOf(true)) : iblockstate;
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return null;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random)
    {
        return 0;
    }

    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te)
    {
        if (!worldIn.isRemote && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.shears)
        {
            player.triggerAchievement(StatList.mineBlockStatArray[Block.getIdFromBlock(this)]);
            Block.spawnAsEntity(worldIn, pos, new ItemStack(Blocks.vine, 1, 0));
        }
        else
        {
            super.harvestBlock(worldIn, player, pos, state, te);
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
        return getDefaultState().withProperty(BlockVine.SOUTH, Boolean.valueOf((meta & 1) > 0)).withProperty(BlockVine.WEST, Boolean.valueOf((meta & 2) > 0)).withProperty(BlockVine.NORTH, Boolean.valueOf((meta & 4) > 0)).withProperty(BlockVine.EAST, Boolean.valueOf((meta & 8) > 0));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;

        if (state.getValue(BlockVine.SOUTH).booleanValue())
        {
            i |= 1;
        }

        if (state.getValue(BlockVine.WEST).booleanValue())
        {
            i |= 2;
        }

        if (state.getValue(BlockVine.NORTH).booleanValue())
        {
            i |= 4;
        }

        if (state.getValue(BlockVine.EAST).booleanValue())
        {
            i |= 8;
        }

        return i;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockVine.UP, BlockVine.NORTH, BlockVine.EAST, BlockVine.SOUTH, BlockVine.WEST);
    }

    public static PropertyBool getPropertyFor(EnumFacing side)
    {
        switch (side)
        {
            case UP:
                return BlockVine.UP;

            case NORTH:
                return BlockVine.NORTH;

            case SOUTH:
                return BlockVine.SOUTH;

            case EAST:
                return BlockVine.EAST;

            case WEST:
                return BlockVine.WEST;

            default:
                throw new IllegalArgumentException(side + " is an invalid choice");
        }
    }

    public static int getNumGrownFaces(IBlockState state)
    {
        int i = 0;

        for (PropertyBool propertybool : BlockVine.ALL_FACES)
        {
            if (state.getValue(propertybool).booleanValue())
            {
                ++i;
            }
        }

        return i;
    }
}
