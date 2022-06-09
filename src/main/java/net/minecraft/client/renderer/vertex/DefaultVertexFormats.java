package net.minecraft.client.renderer.vertex;

import optifine.Config;
import optifine.Reflector;
import shadersmod.client.SVertexFormat;

public class DefaultVertexFormats
{
    public static VertexFormat BLOCK = new VertexFormat();
    public static VertexFormat ITEM = new VertexFormat();
    private static final VertexFormat BLOCK_VANILLA = DefaultVertexFormats.BLOCK;
    private static final VertexFormat ITEM_VANILLA = DefaultVertexFormats.ITEM;
    public static final VertexFormat OLDMODEL_POSITION_TEX_NORMAL = new VertexFormat();
    public static final VertexFormat PARTICLE_POSITION_TEX_COLOR_LMAP = new VertexFormat();
    public static final VertexFormat POSITION = new VertexFormat();
    public static final VertexFormat POSITION_COLOR = new VertexFormat();
    public static final VertexFormat POSITION_TEX = new VertexFormat();
    public static final VertexFormat POSITION_NORMAL = new VertexFormat();
    public static final VertexFormat POSITION_TEX_COLOR = new VertexFormat();
    public static final VertexFormat POSITION_TEX_NORMAL = new VertexFormat();
    public static final VertexFormat POSITION_TEX_LMAP_COLOR = new VertexFormat();
    public static final VertexFormat POSITION_TEX_COLOR_NORMAL = new VertexFormat();
    public static final VertexFormatElement POSITION_3F = new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3);
    public static final VertexFormatElement COLOR_4UB = new VertexFormatElement(0, VertexFormatElement.EnumType.UBYTE, VertexFormatElement.EnumUsage.COLOR, 4);
    public static final VertexFormatElement TEX_2F = new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 2);
    public static final VertexFormatElement TEX_2S = new VertexFormatElement(1, VertexFormatElement.EnumType.SHORT, VertexFormatElement.EnumUsage.UV, 2);
    public static final VertexFormatElement NORMAL_3B = new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.NORMAL, 3);
    public static final VertexFormatElement PADDING_1B = new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.PADDING, 1);
    private static final String __OBFID = "CL_00002403";

    public static void updateVertexFormats()
    {
        if (Config.isShaders())
        {
            DefaultVertexFormats.BLOCK = SVertexFormat.makeDefVertexFormatBlock();
            DefaultVertexFormats.ITEM = SVertexFormat.makeDefVertexFormatItem();
        }
        else
        {
            DefaultVertexFormats.BLOCK = DefaultVertexFormats.BLOCK_VANILLA;
            DefaultVertexFormats.ITEM = DefaultVertexFormats.ITEM_VANILLA;
        }

        if (Reflector.Attributes_DEFAULT_BAKED_FORMAT.exists())
        {
            VertexFormat vertexformat = DefaultVertexFormats.ITEM;
            VertexFormat vertexformat1 = (VertexFormat)Reflector.getFieldValue(Reflector.Attributes_DEFAULT_BAKED_FORMAT);
            vertexformat1.clear();

            for (int i = 0; i < vertexformat.getElementCount(); ++i)
            {
                vertexformat1.func_181721_a(vertexformat.getElement(i));
            }
        }
    }

    static
    {
        DefaultVertexFormats.BLOCK.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.BLOCK.func_181721_a(DefaultVertexFormats.COLOR_4UB);
        DefaultVertexFormats.BLOCK.func_181721_a(DefaultVertexFormats.TEX_2F);
        DefaultVertexFormats.BLOCK.func_181721_a(DefaultVertexFormats.TEX_2S);
        DefaultVertexFormats.ITEM.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.ITEM.func_181721_a(DefaultVertexFormats.COLOR_4UB);
        DefaultVertexFormats.ITEM.func_181721_a(DefaultVertexFormats.TEX_2F);
        DefaultVertexFormats.ITEM.func_181721_a(DefaultVertexFormats.NORMAL_3B);
        DefaultVertexFormats.ITEM.func_181721_a(DefaultVertexFormats.PADDING_1B);
        DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL.func_181721_a(DefaultVertexFormats.TEX_2F);
        DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL.func_181721_a(DefaultVertexFormats.NORMAL_3B);
        DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL.func_181721_a(DefaultVertexFormats.PADDING_1B);
        DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP.func_181721_a(DefaultVertexFormats.TEX_2F);
        DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP.func_181721_a(DefaultVertexFormats.COLOR_4UB);
        DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP.func_181721_a(DefaultVertexFormats.TEX_2S);
        DefaultVertexFormats.POSITION.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.POSITION_COLOR.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.POSITION_COLOR.func_181721_a(DefaultVertexFormats.COLOR_4UB);
        DefaultVertexFormats.POSITION_TEX.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.POSITION_TEX.func_181721_a(DefaultVertexFormats.TEX_2F);
        DefaultVertexFormats.POSITION_NORMAL.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.POSITION_NORMAL.func_181721_a(DefaultVertexFormats.NORMAL_3B);
        DefaultVertexFormats.POSITION_NORMAL.func_181721_a(DefaultVertexFormats.PADDING_1B);
        DefaultVertexFormats.POSITION_TEX_COLOR.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.POSITION_TEX_COLOR.func_181721_a(DefaultVertexFormats.TEX_2F);
        DefaultVertexFormats.POSITION_TEX_COLOR.func_181721_a(DefaultVertexFormats.COLOR_4UB);
        DefaultVertexFormats.POSITION_TEX_NORMAL.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.POSITION_TEX_NORMAL.func_181721_a(DefaultVertexFormats.TEX_2F);
        DefaultVertexFormats.POSITION_TEX_NORMAL.func_181721_a(DefaultVertexFormats.NORMAL_3B);
        DefaultVertexFormats.POSITION_TEX_NORMAL.func_181721_a(DefaultVertexFormats.PADDING_1B);
        DefaultVertexFormats.POSITION_TEX_LMAP_COLOR.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.POSITION_TEX_LMAP_COLOR.func_181721_a(DefaultVertexFormats.TEX_2F);
        DefaultVertexFormats.POSITION_TEX_LMAP_COLOR.func_181721_a(DefaultVertexFormats.TEX_2S);
        DefaultVertexFormats.POSITION_TEX_LMAP_COLOR.func_181721_a(DefaultVertexFormats.COLOR_4UB);
        DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.func_181721_a(DefaultVertexFormats.POSITION_3F);
        DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.func_181721_a(DefaultVertexFormats.TEX_2F);
        DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.func_181721_a(DefaultVertexFormats.COLOR_4UB);
        DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.func_181721_a(DefaultVertexFormats.NORMAL_3B);
        DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.func_181721_a(DefaultVertexFormats.PADDING_1B);
    }
}
