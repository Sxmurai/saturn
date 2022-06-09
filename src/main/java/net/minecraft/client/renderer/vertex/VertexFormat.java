package net.minecraft.client.renderer.vertex;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VertexFormat
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<VertexFormatElement> elements;
    private final List<Integer> offsets;

    /** The next available offset in this vertex format */
    private int nextOffset;
    private int colorElementOffset;
    private final List<Integer> uvOffsetsById;
    private int normalElementOffset;

    public VertexFormat(VertexFormat vertexFormatIn)
    {
        this();

        for (int i = 0; i < vertexFormatIn.getElementCount(); ++i)
        {
            func_181721_a(vertexFormatIn.getElement(i));
        }

        nextOffset = vertexFormatIn.getNextOffset();
    }

    public VertexFormat()
    {
        elements = Lists.newArrayList();
        offsets = Lists.newArrayList();
        nextOffset = 0;
        colorElementOffset = -1;
        uvOffsetsById = Lists.newArrayList();
        normalElementOffset = -1;
    }

    public void clear()
    {
        elements.clear();
        offsets.clear();
        colorElementOffset = -1;
        uvOffsetsById.clear();
        normalElementOffset = -1;
        nextOffset = 0;
    }

    @SuppressWarnings("incomplete-switch")
    public VertexFormat func_181721_a(VertexFormatElement p_181721_1_)
    {
        if (p_181721_1_.isPositionElement() && hasPosition())
        {
            VertexFormat.LOGGER.warn("VertexFormat error: Trying to add a position VertexFormatElement when one already exists, ignoring.");
            return this;
        }
        else
        {
            elements.add(p_181721_1_);
            offsets.add(Integer.valueOf(nextOffset));

            switch (p_181721_1_.getUsage())
            {
                case NORMAL:
                    normalElementOffset = nextOffset;
                    break;

                case COLOR:
                    colorElementOffset = nextOffset;
                    break;

                case UV:
                    uvOffsetsById.add(p_181721_1_.getIndex(), Integer.valueOf(nextOffset));
            }

            nextOffset += p_181721_1_.getSize();
            return this;
        }
    }

    public boolean hasNormal()
    {
        return normalElementOffset >= 0;
    }

    public int getNormalOffset()
    {
        return normalElementOffset;
    }

    public boolean hasColor()
    {
        return colorElementOffset >= 0;
    }

    public int getColorOffset()
    {
        return colorElementOffset;
    }

    public boolean hasUvOffset(int id)
    {
        return uvOffsetsById.size() - 1 >= id;
    }

    public int getUvOffsetById(int id)
    {
        return uvOffsetsById.get(id).intValue();
    }

    public String toString()
    {
        String s = "format: " + elements.size() + " elements: ";

        for (int i = 0; i < elements.size(); ++i)
        {
            s = s + elements.get(i).toString();

            if (i != elements.size() - 1)
            {
                s = s + " ";
            }
        }

        return s;
    }

    private boolean hasPosition()
    {
        int i = 0;

        for (int j = elements.size(); i < j; ++i)
        {
            VertexFormatElement vertexformatelement = elements.get(i);

            if (vertexformatelement.isPositionElement())
            {
                return true;
            }
        }

        return false;
    }

    public int func_181719_f()
    {
        return getNextOffset() / 4;
    }

    public int getNextOffset()
    {
        return nextOffset;
    }

    public List<VertexFormatElement> getElements()
    {
        return elements;
    }

    public int getElementCount()
    {
        return elements.size();
    }

    public VertexFormatElement getElement(int index)
    {
        return elements.get(index);
    }

    public int func_181720_d(int p_181720_1_)
    {
        return offsets.get(p_181720_1_).intValue();
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (p_equals_1_ != null && getClass() == p_equals_1_.getClass())
        {
            VertexFormat vertexformat = (VertexFormat)p_equals_1_;
            return nextOffset == vertexformat.nextOffset && (elements.equals(vertexformat.elements) && offsets.equals(vertexformat.offsets));
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        int i = elements.hashCode();
        i = 31 * i + offsets.hashCode();
        i = 31 * i + nextOffset;
        return i;
    }
}
