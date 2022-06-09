package net.minecraft.block.state;

import com.google.common.base.Predicate;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BlockWorldState
{
    private final World world;
    private final BlockPos pos;
    private final boolean field_181628_c;
    private IBlockState state;
    private TileEntity tileEntity;
    private boolean tileEntityInitialized;

    public BlockWorldState(World p_i46451_1_, BlockPos p_i46451_2_, boolean p_i46451_3_)
    {
        world = p_i46451_1_;
        pos = p_i46451_2_;
        field_181628_c = p_i46451_3_;
    }

    public IBlockState getBlockState()
    {
        if (state == null && (field_181628_c || world.isBlockLoaded(pos)))
        {
            state = world.getBlockState(pos);
        }

        return state;
    }

    public TileEntity getTileEntity()
    {
        if (tileEntity == null && !tileEntityInitialized)
        {
            tileEntity = world.getTileEntity(pos);
            tileEntityInitialized = true;
        }

        return tileEntity;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public static Predicate<BlockWorldState> hasState(final Predicate<IBlockState> p_177510_0_)
    {
        return new Predicate<BlockWorldState>()
        {
            public boolean apply(BlockWorldState p_apply_1_)
            {
                return p_apply_1_ != null && p_177510_0_.apply(p_apply_1_.getBlockState());
            }
        };
    }
}
