package net.minecraft.realms;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class RealmsDefaultVertexFormat
{
    public static final RealmsVertexFormat BLOCK = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormat BLOCK_NORMALS = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormat ENTITY = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormat PARTICLE = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormat POSITION = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormat POSITION_COLOR = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormat POSITION_TEX = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormat POSITION_NORMAL = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormat POSITION_TEX_COLOR = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormat POSITION_TEX_NORMAL = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormat POSITION_TEX2_COLOR = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormat POSITION_TEX_COLOR_NORMAL = new RealmsVertexFormat(new VertexFormat());
    public static final RealmsVertexFormatElement ELEMENT_POSITION = new RealmsVertexFormatElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
    public static final RealmsVertexFormatElement ELEMENT_COLOR = new RealmsVertexFormatElement(new VertexFormatElement(0, VertexFormatElement.EnumType.UBYTE, VertexFormatElement.EnumUsage.COLOR, 4));
    public static final RealmsVertexFormatElement ELEMENT_UV0 = new RealmsVertexFormatElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 2));
    public static final RealmsVertexFormatElement ELEMENT_UV1 = new RealmsVertexFormatElement(new VertexFormatElement(1, VertexFormatElement.EnumType.SHORT, VertexFormatElement.EnumUsage.UV, 2));
    public static final RealmsVertexFormatElement ELEMENT_NORMAL = new RealmsVertexFormatElement(new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.NORMAL, 3));
    public static final RealmsVertexFormatElement ELEMENT_PADDING = new RealmsVertexFormatElement(new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.PADDING, 1));

    static
    {
        RealmsDefaultVertexFormat.BLOCK.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.BLOCK.addElement(RealmsDefaultVertexFormat.ELEMENT_COLOR);
        RealmsDefaultVertexFormat.BLOCK.addElement(RealmsDefaultVertexFormat.ELEMENT_UV0);
        RealmsDefaultVertexFormat.BLOCK.addElement(RealmsDefaultVertexFormat.ELEMENT_UV1);
        RealmsDefaultVertexFormat.BLOCK_NORMALS.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.BLOCK_NORMALS.addElement(RealmsDefaultVertexFormat.ELEMENT_COLOR);
        RealmsDefaultVertexFormat.BLOCK_NORMALS.addElement(RealmsDefaultVertexFormat.ELEMENT_UV0);
        RealmsDefaultVertexFormat.BLOCK_NORMALS.addElement(RealmsDefaultVertexFormat.ELEMENT_NORMAL);
        RealmsDefaultVertexFormat.BLOCK_NORMALS.addElement(RealmsDefaultVertexFormat.ELEMENT_PADDING);
        RealmsDefaultVertexFormat.ENTITY.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.ENTITY.addElement(RealmsDefaultVertexFormat.ELEMENT_UV0);
        RealmsDefaultVertexFormat.ENTITY.addElement(RealmsDefaultVertexFormat.ELEMENT_NORMAL);
        RealmsDefaultVertexFormat.ENTITY.addElement(RealmsDefaultVertexFormat.ELEMENT_PADDING);
        RealmsDefaultVertexFormat.PARTICLE.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.PARTICLE.addElement(RealmsDefaultVertexFormat.ELEMENT_UV0);
        RealmsDefaultVertexFormat.PARTICLE.addElement(RealmsDefaultVertexFormat.ELEMENT_COLOR);
        RealmsDefaultVertexFormat.PARTICLE.addElement(RealmsDefaultVertexFormat.ELEMENT_UV1);
        RealmsDefaultVertexFormat.POSITION.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.POSITION_COLOR.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.POSITION_COLOR.addElement(RealmsDefaultVertexFormat.ELEMENT_COLOR);
        RealmsDefaultVertexFormat.POSITION_TEX.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.POSITION_TEX.addElement(RealmsDefaultVertexFormat.ELEMENT_UV0);
        RealmsDefaultVertexFormat.POSITION_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.POSITION_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_NORMAL);
        RealmsDefaultVertexFormat.POSITION_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_PADDING);
        RealmsDefaultVertexFormat.POSITION_TEX_COLOR.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.POSITION_TEX_COLOR.addElement(RealmsDefaultVertexFormat.ELEMENT_UV0);
        RealmsDefaultVertexFormat.POSITION_TEX_COLOR.addElement(RealmsDefaultVertexFormat.ELEMENT_COLOR);
        RealmsDefaultVertexFormat.POSITION_TEX_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.POSITION_TEX_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_UV0);
        RealmsDefaultVertexFormat.POSITION_TEX_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_NORMAL);
        RealmsDefaultVertexFormat.POSITION_TEX_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_PADDING);
        RealmsDefaultVertexFormat.POSITION_TEX2_COLOR.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.POSITION_TEX2_COLOR.addElement(RealmsDefaultVertexFormat.ELEMENT_UV0);
        RealmsDefaultVertexFormat.POSITION_TEX2_COLOR.addElement(RealmsDefaultVertexFormat.ELEMENT_UV1);
        RealmsDefaultVertexFormat.POSITION_TEX2_COLOR.addElement(RealmsDefaultVertexFormat.ELEMENT_COLOR);
        RealmsDefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_POSITION);
        RealmsDefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_UV0);
        RealmsDefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_COLOR);
        RealmsDefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_NORMAL);
        RealmsDefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.addElement(RealmsDefaultVertexFormat.ELEMENT_PADDING);
    }
}
