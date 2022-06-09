package net.minecraft.client.renderer;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.EnumWorldBlockLayer;
import optifine.Config;

import org.lwjgl.opengl.GL11;
import shadersmod.client.ShadersRender;

public class VboRenderList extends ChunkRenderContainer
{
    private static final String __OBFID = "CL_00002533";

    public void renderChunkLayer(EnumWorldBlockLayer layer)
    {
        if (initialized)
        {
            for (RenderChunk renderchunk : renderChunks)
            {
                VertexBuffer vertexbuffer = renderchunk.getVertexBufferByLayer(layer.ordinal());
                GlStateManager.pushMatrix();
                preRenderChunk(renderchunk);
                renderchunk.multModelviewMatrix();
                vertexbuffer.bindBuffer();
                setupArrayPointers();
                vertexbuffer.drawArrays(7);
                GlStateManager.popMatrix();
            }

            OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
            GlStateManager.resetColor();
            renderChunks.clear();
        }
    }

    private void setupArrayPointers()
    {
        if (Config.isShaders())
        {
            ShadersRender.setupArrayPointersVbo();
        }
        else
        {
            GL11.glVertexPointer(3, GL11.GL_FLOAT, 28, 0L);
            GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12L);
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16L);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24L);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        }
    }
}
