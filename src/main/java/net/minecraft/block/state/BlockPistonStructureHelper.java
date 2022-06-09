package net.minecraft.block.state;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockPistonStructureHelper
{
    private final World world;
    private final BlockPos pistonPos;
    private final BlockPos blockToMove;
    private final EnumFacing moveDirection;
    private final List<BlockPos> toMove = Lists.newArrayList();
    private final List<BlockPos> toDestroy = Lists.newArrayList();

    public BlockPistonStructureHelper(World worldIn, BlockPos posIn, EnumFacing pistonFacing, boolean extending)
    {
        world = worldIn;
        pistonPos = posIn;

        if (extending)
        {
            moveDirection = pistonFacing;
            blockToMove = posIn.offset(pistonFacing);
        }
        else
        {
            moveDirection = pistonFacing.getOpposite();
            blockToMove = posIn.offset(pistonFacing, 2);
        }
    }

    public boolean canMove()
    {
        toMove.clear();
        toDestroy.clear();
        Block block = world.getBlockState(blockToMove).getBlock();

        if (!BlockPistonBase.canPush(block, world, blockToMove, moveDirection, false))
        {
            if (block.getMobilityFlag() != 1)
            {
                return false;
            }
            else
            {
                toDestroy.add(blockToMove);
                return true;
            }
        }
        else if (!func_177251_a(blockToMove))
        {
            return false;
        }
        else
        {
            for (int i = 0; i < toMove.size(); ++i)
            {
                BlockPos blockpos = toMove.get(i);

                if (world.getBlockState(blockpos).getBlock() == Blocks.slime_block && !func_177250_b(blockpos))
                {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean func_177251_a(BlockPos origin)
    {
        Block block = world.getBlockState(origin).getBlock();

        if (block.getMaterial() == Material.air)
        {
            return true;
        }
        else if (!BlockPistonBase.canPush(block, world, origin, moveDirection, false))
        {
            return true;
        }
        else if (origin.equals(pistonPos))
        {
            return true;
        }
        else if (toMove.contains(origin))
        {
            return true;
        }
        else
        {
            int i = 1;

            if (i + toMove.size() > 12)
            {
                return false;
            }
            else
            {
                while (block == Blocks.slime_block)
                {
                    BlockPos blockpos = origin.offset(moveDirection.getOpposite(), i);
                    block = world.getBlockState(blockpos).getBlock();

                    if (block.getMaterial() == Material.air || !BlockPistonBase.canPush(block, world, blockpos, moveDirection, false) || blockpos.equals(pistonPos))
                    {
                        break;
                    }

                    ++i;

                    if (i + toMove.size() > 12)
                    {
                        return false;
                    }
                }

                int i1 = 0;

                for (int j = i - 1; j >= 0; --j)
                {
                    toMove.add(origin.offset(moveDirection.getOpposite(), j));
                    ++i1;
                }

                int j1 = 1;

                while (true)
                {
                    BlockPos blockpos1 = origin.offset(moveDirection, j1);
                    int k = toMove.indexOf(blockpos1);

                    if (k > -1)
                    {
                        func_177255_a(i1, k);

                        for (int l = 0; l <= k + i1; ++l)
                        {
                            BlockPos blockpos2 = toMove.get(l);

                            if (world.getBlockState(blockpos2).getBlock() == Blocks.slime_block && !func_177250_b(blockpos2))
                            {
                                return false;
                            }
                        }

                        return true;
                    }

                    block = world.getBlockState(blockpos1).getBlock();

                    if (block.getMaterial() == Material.air)
                    {
                        return true;
                    }

                    if (!BlockPistonBase.canPush(block, world, blockpos1, moveDirection, true) || blockpos1.equals(pistonPos))
                    {
                        return false;
                    }

                    if (block.getMobilityFlag() == 1)
                    {
                        toDestroy.add(blockpos1);
                        return true;
                    }

                    if (toMove.size() >= 12)
                    {
                        return false;
                    }

                    toMove.add(blockpos1);
                    ++i1;
                    ++j1;
                }
            }
        }
    }

    private void func_177255_a(int p_177255_1_, int p_177255_2_)
    {
        List<BlockPos> list = Lists.newArrayList();
        List<BlockPos> list1 = Lists.newArrayList();
        List<BlockPos> list2 = Lists.newArrayList();
        list.addAll(toMove.subList(0, p_177255_2_));
        list1.addAll(toMove.subList(toMove.size() - p_177255_1_, toMove.size()));
        list2.addAll(toMove.subList(p_177255_2_, toMove.size() - p_177255_1_));
        toMove.clear();
        toMove.addAll(list);
        toMove.addAll(list1);
        toMove.addAll(list2);
    }

    private boolean func_177250_b(BlockPos p_177250_1_)
    {
        for (EnumFacing enumfacing : EnumFacing.values())
        {
            if (enumfacing.getAxis() != moveDirection.getAxis() && !func_177251_a(p_177250_1_.offset(enumfacing)))
            {
                return false;
            }
        }

        return true;
    }

    public List<BlockPos> getBlocksToMove()
    {
        return toMove;
    }

    public List<BlockPos> getBlocksToDestroy()
    {
        return toDestroy;
    }
}
