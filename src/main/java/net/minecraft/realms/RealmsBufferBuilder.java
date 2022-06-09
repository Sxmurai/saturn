package net.minecraft.realms;

import java.nio.ByteBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;

public class RealmsBufferBuilder
{
    private WorldRenderer b;

    public RealmsBufferBuilder(WorldRenderer p_i46442_1_)
    {
        b = p_i46442_1_;
    }

    public RealmsBufferBuilder from(WorldRenderer p_from_1_)
    {
        b = p_from_1_;
        return this;
    }

    public void sortQuads(float p_sortQuads_1_, float p_sortQuads_2_, float p_sortQuads_3_)
    {
        b.func_181674_a(p_sortQuads_1_, p_sortQuads_2_, p_sortQuads_3_);
    }

    public void fixupQuadColor(int p_fixupQuadColor_1_)
    {
        b.putColor4(p_fixupQuadColor_1_);
    }

    public ByteBuffer getBuffer()
    {
        return b.getByteBuffer();
    }

    public void postNormal(float p_postNormal_1_, float p_postNormal_2_, float p_postNormal_3_)
    {
        b.putNormal(p_postNormal_1_, p_postNormal_2_, p_postNormal_3_);
    }

    public int getDrawMode()
    {
        return b.getDrawMode();
    }

    public void offset(double p_offset_1_, double p_offset_3_, double p_offset_5_)
    {
        b.setTranslation(p_offset_1_, p_offset_3_, p_offset_5_);
    }

    public void restoreState(WorldRenderer.State p_restoreState_1_)
    {
        b.setVertexState(p_restoreState_1_);
    }

    public void endVertex()
    {
        b.endVertex();
    }

    public RealmsBufferBuilder normal(float p_normal_1_, float p_normal_2_, float p_normal_3_)
    {
        return from(b.normal(p_normal_1_, p_normal_2_, p_normal_3_));
    }

    public void end()
    {
        b.finishDrawing();
    }

    public void begin(int p_begin_1_, VertexFormat p_begin_2_)
    {
        b.begin(p_begin_1_, p_begin_2_);
    }

    public RealmsBufferBuilder color(int p_color_1_, int p_color_2_, int p_color_3_, int p_color_4_)
    {
        return from(b.color(p_color_1_, p_color_2_, p_color_3_, p_color_4_));
    }

    public void faceTex2(int p_faceTex2_1_, int p_faceTex2_2_, int p_faceTex2_3_, int p_faceTex2_4_)
    {
        b.putBrightness4(p_faceTex2_1_, p_faceTex2_2_, p_faceTex2_3_, p_faceTex2_4_);
    }

    public void postProcessFacePosition(double p_postProcessFacePosition_1_, double p_postProcessFacePosition_3_, double p_postProcessFacePosition_5_)
    {
        b.putPosition(p_postProcessFacePosition_1_, p_postProcessFacePosition_3_, p_postProcessFacePosition_5_);
    }

    public void fixupVertexColor(float p_fixupVertexColor_1_, float p_fixupVertexColor_2_, float p_fixupVertexColor_3_, int p_fixupVertexColor_4_)
    {
        b.putColorRGB_F(p_fixupVertexColor_1_, p_fixupVertexColor_2_, p_fixupVertexColor_3_, p_fixupVertexColor_4_);
    }

    public RealmsBufferBuilder color(float p_color_1_, float p_color_2_, float p_color_3_, float p_color_4_)
    {
        return from(b.color(p_color_1_, p_color_2_, p_color_3_, p_color_4_));
    }

    public RealmsVertexFormat getVertexFormat()
    {
        return new RealmsVertexFormat(b.getVertexFormat());
    }

    public void faceTint(float p_faceTint_1_, float p_faceTint_2_, float p_faceTint_3_, int p_faceTint_4_)
    {
        b.putColorMultiplier(p_faceTint_1_, p_faceTint_2_, p_faceTint_3_, p_faceTint_4_);
    }

    public RealmsBufferBuilder tex2(int p_tex2_1_, int p_tex2_2_)
    {
        return from(b.lightmap(p_tex2_1_, p_tex2_2_));
    }

    public void putBulkData(int[] p_putBulkData_1_)
    {
        b.addVertexData(p_putBulkData_1_);
    }

    public RealmsBufferBuilder tex(double p_tex_1_, double p_tex_3_)
    {
        return from(b.tex(p_tex_1_, p_tex_3_));
    }

    public int getVertexCount()
    {
        return b.getVertexCount();
    }

    public void clear()
    {
        b.reset();
    }

    public RealmsBufferBuilder vertex(double p_vertex_1_, double p_vertex_3_, double p_vertex_5_)
    {
        return from(b.pos(p_vertex_1_, p_vertex_3_, p_vertex_5_));
    }

    public void fixupQuadColor(float p_fixupQuadColor_1_, float p_fixupQuadColor_2_, float p_fixupQuadColor_3_)
    {
        b.putColorRGB_F4(p_fixupQuadColor_1_, p_fixupQuadColor_2_, p_fixupQuadColor_3_);
    }

    public void noColor()
    {
        b.markDirty();
    }
}
