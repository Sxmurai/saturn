package net.minecraft.client.shader;

import java.nio.ByteBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class Framebuffer
{
    public int framebufferTextureWidth;
    public int framebufferTextureHeight;
    public int framebufferWidth;
    public int framebufferHeight;
    public boolean useDepth;
    public int framebufferObject;
    public int framebufferTexture;
    public int depthBuffer;
    public float[] framebufferColor;
    public int framebufferFilter;

    public Framebuffer(int p_i45078_1_, int p_i45078_2_, boolean p_i45078_3_)
    {
        useDepth = p_i45078_3_;
        framebufferObject = -1;
        framebufferTexture = -1;
        depthBuffer = -1;
        framebufferColor = new float[4];
        framebufferColor[0] = 1.0F;
        framebufferColor[1] = 1.0F;
        framebufferColor[2] = 1.0F;
        framebufferColor[3] = 0.0F;
        createBindFramebuffer(p_i45078_1_, p_i45078_2_);
    }

    public void createBindFramebuffer(int width, int height)
    {
        if (!OpenGlHelper.isFramebufferEnabled())
        {
            framebufferWidth = width;
            framebufferHeight = height;
        }
        else
        {
            GlStateManager.enableDepth();

            if (framebufferObject >= 0)
            {
                deleteFramebuffer();
            }

            createFramebuffer(width, height);
            checkFramebufferComplete();
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, 0);
        }
    }

    public void deleteFramebuffer()
    {
        if (OpenGlHelper.isFramebufferEnabled())
        {
            unbindFramebufferTexture();
            unbindFramebuffer();

            if (depthBuffer > -1)
            {
                OpenGlHelper.glDeleteRenderbuffers(depthBuffer);
                depthBuffer = -1;
            }

            if (framebufferTexture > -1)
            {
                TextureUtil.deleteTexture(framebufferTexture);
                framebufferTexture = -1;
            }

            if (framebufferObject > -1)
            {
                OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, 0);
                OpenGlHelper.glDeleteFramebuffers(framebufferObject);
                framebufferObject = -1;
            }
        }
    }

    public void createFramebuffer(int width, int height)
    {
        framebufferWidth = width;
        framebufferHeight = height;
        framebufferTextureWidth = width;
        framebufferTextureHeight = height;

        if (!OpenGlHelper.isFramebufferEnabled())
        {
            framebufferClear();
        }
        else
        {
            framebufferObject = OpenGlHelper.glGenFramebuffers();
            framebufferTexture = TextureUtil.glGenTextures();

            if (useDepth)
            {
                depthBuffer = OpenGlHelper.glGenRenderbuffers();
            }

            setFramebufferFilter(9728);
            GlStateManager.bindTexture(framebufferTexture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, framebufferTextureWidth, framebufferTextureHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, framebufferObject);
            OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, 3553, framebufferTexture, 0);

            if (useDepth)
            {
                OpenGlHelper.glBindRenderbuffer(OpenGlHelper.GL_RENDERBUFFER, depthBuffer);
                OpenGlHelper.glRenderbufferStorage(OpenGlHelper.GL_RENDERBUFFER, 33190, framebufferTextureWidth, framebufferTextureHeight);
                OpenGlHelper.glFramebufferRenderbuffer(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_DEPTH_ATTACHMENT, OpenGlHelper.GL_RENDERBUFFER, depthBuffer);
            }

            framebufferClear();
            unbindFramebufferTexture();
        }
    }

    public void setFramebufferFilter(int p_147607_1_)
    {
        if (OpenGlHelper.isFramebufferEnabled())
        {
            framebufferFilter = p_147607_1_;
            GlStateManager.bindTexture(framebufferTexture);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, (float)p_147607_1_);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, (float)p_147607_1_);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10496.0F);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10496.0F);
            GlStateManager.bindTexture(0);
        }
    }

    public void checkFramebufferComplete()
    {
        int i = OpenGlHelper.glCheckFramebufferStatus(OpenGlHelper.GL_FRAMEBUFFER);

        if (i != OpenGlHelper.GL_FRAMEBUFFER_COMPLETE)
        {
            if (i == OpenGlHelper.GL_FB_INCOMPLETE_ATTACHMENT)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            }
            else if (i == OpenGlHelper.GL_FB_INCOMPLETE_MISS_ATTACH)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            }
            else if (i == OpenGlHelper.GL_FB_INCOMPLETE_DRAW_BUFFER)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
            }
            else if (i == OpenGlHelper.GL_FB_INCOMPLETE_READ_BUFFER)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
            }
            else
            {
                throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
            }
        }
    }

    public void bindFramebufferTexture()
    {
        if (OpenGlHelper.isFramebufferEnabled())
        {
            GlStateManager.bindTexture(framebufferTexture);
        }
    }

    public void unbindFramebufferTexture()
    {
        if (OpenGlHelper.isFramebufferEnabled())
        {
            GlStateManager.bindTexture(0);
        }
    }

    public void bindFramebuffer(boolean p_147610_1_)
    {
        if (OpenGlHelper.isFramebufferEnabled())
        {
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, framebufferObject);

            if (p_147610_1_)
            {
                GlStateManager.viewport(0, 0, framebufferWidth, framebufferHeight);
            }
        }
    }

    public void unbindFramebuffer()
    {
        if (OpenGlHelper.isFramebufferEnabled())
        {
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, 0);
        }
    }

    public void setFramebufferColor(float p_147604_1_, float p_147604_2_, float p_147604_3_, float p_147604_4_)
    {
        framebufferColor[0] = p_147604_1_;
        framebufferColor[1] = p_147604_2_;
        framebufferColor[2] = p_147604_3_;
        framebufferColor[3] = p_147604_4_;
    }

    public void framebufferRender(int p_147615_1_, int p_147615_2_)
    {
        framebufferRenderExt(p_147615_1_, p_147615_2_, true);
    }

    public void framebufferRenderExt(int p_178038_1_, int p_178038_2_, boolean p_178038_3_)
    {
        if (OpenGlHelper.isFramebufferEnabled())
        {
            GlStateManager.colorMask(true, true, true, false);
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, p_178038_1_, p_178038_2_, 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            GlStateManager.viewport(0, 0, p_178038_1_, p_178038_2_);
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableAlpha();

            if (p_178038_3_)
            {
                GlStateManager.disableBlend();
                GlStateManager.enableColorMaterial();
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            bindFramebufferTexture();
            float f = (float)p_178038_1_;
            float f1 = (float)p_178038_2_;
            float f2 = (float) framebufferWidth / (float) framebufferTextureWidth;
            float f3 = (float) framebufferHeight / (float) framebufferTextureHeight;
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos(0.0D, f1, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
            worldrenderer.pos(f, f1, 0.0D).tex(f2, 0.0D).color(255, 255, 255, 255).endVertex();
            worldrenderer.pos(f, 0.0D, 0.0D).tex(f2, f3).color(255, 255, 255, 255).endVertex();
            worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, f3).color(255, 255, 255, 255).endVertex();
            tessellator.draw();
            unbindFramebufferTexture();
            GlStateManager.depthMask(true);
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    public void framebufferClear()
    {
        bindFramebuffer(true);
        GlStateManager.clearColor(framebufferColor[0], framebufferColor[1], framebufferColor[2], framebufferColor[3]);
        int i = 16384;

        if (useDepth)
        {
            GlStateManager.clearDepth(1.0D);
            i |= 256;
        }

        GlStateManager.clear(i);
        unbindFramebuffer();
    }
}
