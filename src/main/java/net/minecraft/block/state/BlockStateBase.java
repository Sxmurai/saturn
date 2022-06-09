package net.minecraft.block.state;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.util.ResourceLocation;

public abstract class BlockStateBase implements IBlockState
{
    private static final Joiner COMMA_JOINER = Joiner.on(',');
    private static final Function MAP_ENTRY_TO_STRING = new Function()
    {
        private static final String __OBFID = "CL_00002031";
        public String apply(Map.Entry p_apply_1_)
        {
            if (p_apply_1_ == null)
            {
                return "<NULL>";
            }
            else
            {
                IProperty iproperty = (IProperty)p_apply_1_.getKey();
                return iproperty.getName() + "=" + iproperty.getName((Comparable)p_apply_1_.getValue());
            }
        }
        public Object apply(Object p_apply_1_)
        {
            return apply((Map.Entry)p_apply_1_);
        }
    };
    private static final String __OBFID = "CL_00002032";
    private int blockId = -1;
    private int blockStateId = -1;
    private int metadata = -1;
    private ResourceLocation blockLocation = null;

    public int getBlockId()
    {
        if (blockId < 0)
        {
            blockId = Block.getIdFromBlock(getBlock());
        }

        return blockId;
    }

    public int getBlockStateId()
    {
        if (blockStateId < 0)
        {
            blockStateId = Block.getStateId(this);
        }

        return blockStateId;
    }

    public int getMetadata()
    {
        if (metadata < 0)
        {
            metadata = getBlock().getMetaFromState(this);
        }

        return metadata;
    }

    public ResourceLocation getBlockLocation()
    {
        if (blockLocation == null)
        {
            blockLocation = Block.blockRegistry.getNameForObject(getBlock());
        }

        return blockLocation;
    }

    /**
     * Create a version of this BlockState with the given property cycled to the next value in order. If the property
     * was at the highest possible value, it is set to the lowest one instead.
     */
    public IBlockState cycleProperty(IProperty property)
    {
        return withProperty(property, (Comparable) BlockStateBase.cyclePropertyValue(property.getAllowedValues(), getValue(property)));
    }

    /**
     * Helper method for cycleProperty.
     */
    protected static Object cyclePropertyValue(Collection values, Object currentValue)
    {
        Iterator iterator = values.iterator();

        while (iterator.hasNext())
        {
            if (iterator.next().equals(currentValue))
            {
                if (iterator.hasNext())
                {
                    return iterator.next();
                }

                return values.iterator().next();
            }
        }

        return iterator.next();
    }

    public String toString()
    {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(Block.blockRegistry.getNameForObject(getBlock()));

        if (!getProperties().isEmpty())
        {
            stringbuilder.append("[");
            BlockStateBase.COMMA_JOINER.appendTo(stringbuilder, Iterables.transform(getProperties().entrySet(), BlockStateBase.MAP_ENTRY_TO_STRING));
            stringbuilder.append("]");
        }

        return stringbuilder.toString();
    }

    public ImmutableTable<IProperty, Comparable, IBlockState> getPropertyValueTable()
    {
        return null;
    }
}
