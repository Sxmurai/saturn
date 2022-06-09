package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityComparator;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRedstoneComparator extends BlockRedstoneDiode implements ITileEntityProvider
{
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyEnum<BlockRedstoneComparator.Mode> MODE = PropertyEnum.create("mode", BlockRedstoneComparator.Mode.class);

    public BlockRedstoneComparator(boolean powered)
    {
        super(powered);
        setDefaultState(blockState.getBaseState().withProperty(BlockDirectional.FACING, EnumFacing.NORTH).withProperty(BlockRedstoneComparator.POWERED, Boolean.valueOf(false)).withProperty(BlockRedstoneComparator.MODE, BlockRedstoneComparator.Mode.COMPARE));
        isBlockContainer = true;
    }

    /**
     * Gets the localized name of this block. Used for the statistics page.
     */
    public String getLocalizedName()
    {
        return StatCollector.translateToLocal("item.comparator.name");
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Items.comparator;
    }

    public Item getItem(World worldIn, BlockPos pos)
    {
        return Items.comparator;
    }

    protected int getDelay(IBlockState state)
    {
        return 2;
    }

    protected IBlockState getPoweredState(IBlockState unpoweredState)
    {
        Boolean obool = unpoweredState.getValue(BlockRedstoneComparator.POWERED);
        BlockRedstoneComparator.Mode blockredstonecomparator$mode = unpoweredState.getValue(BlockRedstoneComparator.MODE);
        EnumFacing enumfacing = unpoweredState.getValue(BlockDirectional.FACING);
        return Blocks.powered_comparator.getDefaultState().withProperty(BlockDirectional.FACING, enumfacing).withProperty(BlockRedstoneComparator.POWERED, obool).withProperty(BlockRedstoneComparator.MODE, blockredstonecomparator$mode);
    }

    protected IBlockState getUnpoweredState(IBlockState poweredState)
    {
        Boolean obool = poweredState.getValue(BlockRedstoneComparator.POWERED);
        BlockRedstoneComparator.Mode blockredstonecomparator$mode = poweredState.getValue(BlockRedstoneComparator.MODE);
        EnumFacing enumfacing = poweredState.getValue(BlockDirectional.FACING);
        return Blocks.unpowered_comparator.getDefaultState().withProperty(BlockDirectional.FACING, enumfacing).withProperty(BlockRedstoneComparator.POWERED, obool).withProperty(BlockRedstoneComparator.MODE, blockredstonecomparator$mode);
    }

    protected boolean isPowered(IBlockState state)
    {
        return isRepeaterPowered || state.getValue(BlockRedstoneComparator.POWERED).booleanValue();
    }

    protected int getActiveSignal(IBlockAccess worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity instanceof TileEntityComparator ? ((TileEntityComparator)tileentity).getOutputSignal() : 0;
    }

    private int calculateOutput(World worldIn, BlockPos pos, IBlockState state)
    {
        return state.getValue(BlockRedstoneComparator.MODE) == BlockRedstoneComparator.Mode.SUBTRACT ? Math.max(calculateInputStrength(worldIn, pos, state) - getPowerOnSides(worldIn, pos, state), 0) : calculateInputStrength(worldIn, pos, state);
    }

    protected boolean shouldBePowered(World worldIn, BlockPos pos, IBlockState state)
    {
        int i = calculateInputStrength(worldIn, pos, state);

        if (i >= 15)
        {
            return true;
        }
        else if (i == 0)
        {
            return false;
        }
        else
        {
            int j = getPowerOnSides(worldIn, pos, state);
            return j == 0 || i >= j;
        }
    }

    protected int calculateInputStrength(World worldIn, BlockPos pos, IBlockState state)
    {
        int i = super.calculateInputStrength(worldIn, pos, state);
        EnumFacing enumfacing = state.getValue(BlockDirectional.FACING);
        BlockPos blockpos = pos.offset(enumfacing);
        Block block = worldIn.getBlockState(blockpos).getBlock();

        if (block.hasComparatorInputOverride())
        {
            i = block.getComparatorInputOverride(worldIn, blockpos);
        }
        else if (i < 15 && block.isNormalCube())
        {
            blockpos = blockpos.offset(enumfacing);
            block = worldIn.getBlockState(blockpos).getBlock();

            if (block.hasComparatorInputOverride())
            {
                i = block.getComparatorInputOverride(worldIn, blockpos);
            }
            else if (block.getMaterial() == Material.air)
            {
                EntityItemFrame entityitemframe = findItemFrame(worldIn, enumfacing, blockpos);

                if (entityitemframe != null)
                {
                    i = entityitemframe.func_174866_q();
                }
            }
        }

        return i;
    }

    private EntityItemFrame findItemFrame(World worldIn, final EnumFacing facing, BlockPos pos)
    {
        List<EntityItemFrame> list = worldIn.getEntitiesWithinAABB(EntityItemFrame.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1), new Predicate<Entity>()
        {
            public boolean apply(Entity p_apply_1_)
            {
                return p_apply_1_ != null && p_apply_1_.getHorizontalFacing() == facing;
            }
        });
        return list.size() == 1 ? list.get(0) : null;
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!playerIn.capabilities.allowEdit)
        {
            return false;
        }
        else
        {
            state = state.cycleProperty(BlockRedstoneComparator.MODE);
            worldIn.playSoundEffect((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, "random.click", 0.3F, state.getValue(BlockRedstoneComparator.MODE) == BlockRedstoneComparator.Mode.SUBTRACT ? 0.55F : 0.5F);
            worldIn.setBlockState(pos, state, 2);
            onStateChange(worldIn, pos, state);
            return true;
        }
    }

    protected void updateState(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!worldIn.isBlockTickPending(pos, this))
        {
            int i = calculateOutput(worldIn, pos, state);
            TileEntity tileentity = worldIn.getTileEntity(pos);
            int j = tileentity instanceof TileEntityComparator ? ((TileEntityComparator)tileentity).getOutputSignal() : 0;

            if (i != j || isPowered(state) != shouldBePowered(worldIn, pos, state))
            {
                if (isFacingTowardsRepeater(worldIn, pos, state))
                {
                    worldIn.updateBlockTick(pos, this, 2, -1);
                }
                else
                {
                    worldIn.updateBlockTick(pos, this, 2, 0);
                }
            }
        }
    }

    private void onStateChange(World worldIn, BlockPos pos, IBlockState state)
    {
        int i = calculateOutput(worldIn, pos, state);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        int j = 0;

        if (tileentity instanceof TileEntityComparator)
        {
            TileEntityComparator tileentitycomparator = (TileEntityComparator)tileentity;
            j = tileentitycomparator.getOutputSignal();
            tileentitycomparator.setOutputSignal(i);
        }

        if (j != i || state.getValue(BlockRedstoneComparator.MODE) == BlockRedstoneComparator.Mode.COMPARE)
        {
            boolean flag1 = shouldBePowered(worldIn, pos, state);
            boolean flag = isPowered(state);

            if (flag && !flag1)
            {
                worldIn.setBlockState(pos, state.withProperty(BlockRedstoneComparator.POWERED, Boolean.valueOf(false)), 2);
            }
            else if (!flag && flag1)
            {
                worldIn.setBlockState(pos, state.withProperty(BlockRedstoneComparator.POWERED, Boolean.valueOf(true)), 2);
            }

            notifyNeighbors(worldIn, pos, state);
        }
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (isRepeaterPowered)
        {
            worldIn.setBlockState(pos, getUnpoweredState(state).withProperty(BlockRedstoneComparator.POWERED, Boolean.valueOf(true)), 4);
        }

        onStateChange(worldIn, pos, state);
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);
        worldIn.setTileEntity(pos, createNewTileEntity(worldIn, 0));
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);
        worldIn.removeTileEntity(pos);
        notifyNeighbors(worldIn, pos, state);
    }

    /**
     * Called on both Client and Server when World#addBlockEvent is called
     */
    public boolean onBlockEventReceived(World worldIn, BlockPos pos, IBlockState state, int eventID, int eventParam)
    {
        super.onBlockEventReceived(worldIn, pos, state, eventID, eventParam);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }

    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityComparator();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(BlockDirectional.FACING, EnumFacing.getHorizontal(meta)).withProperty(BlockRedstoneComparator.POWERED, Boolean.valueOf((meta & 8) > 0)).withProperty(BlockRedstoneComparator.MODE, (meta & 4) > 0 ? BlockRedstoneComparator.Mode.SUBTRACT : BlockRedstoneComparator.Mode.COMPARE);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | state.getValue(BlockDirectional.FACING).getHorizontalIndex();

        if (state.getValue(BlockRedstoneComparator.POWERED).booleanValue())
        {
            i |= 8;
        }

        if (state.getValue(BlockRedstoneComparator.MODE) == BlockRedstoneComparator.Mode.SUBTRACT)
        {
            i |= 4;
        }

        return i;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, BlockDirectional.FACING, BlockRedstoneComparator.MODE, BlockRedstoneComparator.POWERED);
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return getDefaultState().withProperty(BlockDirectional.FACING, placer.getHorizontalFacing().getOpposite()).withProperty(BlockRedstoneComparator.POWERED, Boolean.valueOf(false)).withProperty(BlockRedstoneComparator.MODE, BlockRedstoneComparator.Mode.COMPARE);
    }

    public static enum Mode implements IStringSerializable
    {
        COMPARE("compare"),
        SUBTRACT("subtract");

        private final String name;

        private Mode(String name)
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
