package net.minecraft.client.renderer.vertex;

import java.nio.ByteBuffer;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

public class VertexBuffer
{
    private int glBufferId;
    private final VertexFormat vertexFormat;
    private int count;

    public VertexBuffer(VertexFormat vertexFormatIn)
    {
        vertexFormat = vertexFormatIn;
        glBufferId = OpenGlHelper.glGenBuffers();
    }

    public void bindBuffer()
    {
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, glBufferId);
    }

    public void func_181722_a(ByteBuffer p_181722_1_)
    {
        bindBuffer();
        OpenGlHelper.glBufferData(OpenGlHelper.GL_ARRAY_BUFFER, p_181722_1_, 35044);
        unbindBuffer();
        count = p_181722_1_.limit() / vertexFormat.getNextOffset();
    }

    public void drawArrays(int mode)
    {
        GL11.glDrawArrays(mode, 0, count);
    }

    public void unbindBuffer()
    {
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
    }

    public void deleteGlBuffers()
    {
        if (glBufferId >= 0)
        {
            OpenGlHelper.glDeleteBuffers(glBufferId);
            glBufferId = -1;
        }
    }
}
