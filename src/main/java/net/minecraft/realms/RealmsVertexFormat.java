package net.minecraft.realms;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class RealmsVertexFormat
{
    private VertexFormat v;

    public RealmsVertexFormat(VertexFormat p_i46456_1_)
    {
        v = p_i46456_1_;
    }

    public RealmsVertexFormat from(VertexFormat p_from_1_)
    {
        v = p_from_1_;
        return this;
    }

    public VertexFormat getVertexFormat()
    {
        return v;
    }

    public void clear()
    {
        v.clear();
    }

    public int getUvOffset(int p_getUvOffset_1_)
    {
        return v.getUvOffsetById(p_getUvOffset_1_);
    }

    public int getElementCount()
    {
        return v.getElementCount();
    }

    public boolean hasColor()
    {
        return v.hasColor();
    }

    public boolean hasUv(int p_hasUv_1_)
    {
        return v.hasUvOffset(p_hasUv_1_);
    }

    public RealmsVertexFormatElement getElement(int p_getElement_1_)
    {
        return new RealmsVertexFormatElement(v.getElement(p_getElement_1_));
    }

    public RealmsVertexFormat addElement(RealmsVertexFormatElement p_addElement_1_)
    {
        return from(v.func_181721_a(p_addElement_1_.getVertexFormatElement()));
    }

    public int getColorOffset()
    {
        return v.getColorOffset();
    }

    public List<RealmsVertexFormatElement> getElements()
    {
        List<RealmsVertexFormatElement> list = new ArrayList();

        for (VertexFormatElement vertexformatelement : v.getElements())
        {
            list.add(new RealmsVertexFormatElement(vertexformatelement));
        }

        return list;
    }

    public boolean hasNormal()
    {
        return v.hasNormal();
    }

    public int getVertexSize()
    {
        return v.getNextOffset();
    }

    public int getOffset(int p_getOffset_1_)
    {
        return v.func_181720_d(p_getOffset_1_);
    }

    public int getNormalOffset()
    {
        return v.getNormalOffset();
    }

    public int getIntegerSize()
    {
        return v.func_181719_f();
    }

    public boolean equals(Object p_equals_1_)
    {
        return v.equals(p_equals_1_);
    }

    public int hashCode()
    {
        return v.hashCode();
    }

    public String toString()
    {
        return v.toString();
    }
}
