package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import optifine.Config;

public class GlStateManager
{
    private static final GlStateManager.AlphaState alphaState = new GlStateManager.AlphaState(null);
    private static final GlStateManager.BooleanState lightingState = new GlStateManager.BooleanState(2896);
    private static final GlStateManager.BooleanState[] lightState = new GlStateManager.BooleanState[8];
    private static final GlStateManager.ColorMaterialState colorMaterialState = new GlStateManager.ColorMaterialState(null);
    private static final GlStateManager.BlendState blendState = new GlStateManager.BlendState(null);
    private static final GlStateManager.DepthState depthState = new GlStateManager.DepthState(null);
    private static final GlStateManager.FogState fogState = new GlStateManager.FogState(null);
    private static final GlStateManager.CullState cullState = new GlStateManager.CullState(null);
    private static final GlStateManager.PolygonOffsetState polygonOffsetState = new GlStateManager.PolygonOffsetState(null);
    private static final GlStateManager.ColorLogicState colorLogicState = new GlStateManager.ColorLogicState(null);
    private static final GlStateManager.TexGenState texGenState = new GlStateManager.TexGenState(null);
    private static final GlStateManager.ClearState clearState = new GlStateManager.ClearState(null);
    private static final GlStateManager.StencilState stencilState = new GlStateManager.StencilState(null);
    private static final GlStateManager.BooleanState normalizeState = new GlStateManager.BooleanState(2977);
    private static int activeTextureUnit = 0;
    private static final GlStateManager.TextureState[] textureState = new GlStateManager.TextureState[32];
    private static int activeShadeModel = 7425;
    private static final GlStateManager.BooleanState rescaleNormalState = new GlStateManager.BooleanState(32826);
    private static final GlStateManager.ColorMask colorMaskState = new GlStateManager.ColorMask(null);
    private static final GlStateManager.Color colorState = new GlStateManager.Color();
    private static final String __OBFID = "CL_00002558";
    public static boolean clearEnabled = true;

    public static void pushAttrib()
    {
        GL11.glPushAttrib(8256);
    }

    public static void popAttrib()
    {
        GL11.glPopAttrib();
    }

    public static void disableAlpha()
    {
        GlStateManager.alphaState.field_179208_a.setDisabled();
    }

    public static void enableAlpha()
    {
        GlStateManager.alphaState.field_179208_a.setEnabled();
    }

    public static void alphaFunc(int func, float ref)
    {
        if (func != GlStateManager.alphaState.func || ref != GlStateManager.alphaState.ref)
        {
            GlStateManager.alphaState.func = func;
            GlStateManager.alphaState.ref = ref;
            GL11.glAlphaFunc(func, ref);
        }
    }

    public static void enableLighting()
    {
        GlStateManager.lightingState.setEnabled();
    }

    public static void disableLighting()
    {
        GlStateManager.lightingState.setDisabled();
    }

    public static void enableLight(int light)
    {
        GlStateManager.lightState[light].setEnabled();
    }

    public static void disableLight(int light)
    {
        GlStateManager.lightState[light].setDisabled();
    }

    public static void enableColorMaterial()
    {
        GlStateManager.colorMaterialState.field_179191_a.setEnabled();
    }

    public static void disableColorMaterial()
    {
        GlStateManager.colorMaterialState.field_179191_a.setDisabled();
    }

    public static void colorMaterial(int face, int mode)
    {
        if (face != GlStateManager.colorMaterialState.field_179189_b || mode != GlStateManager.colorMaterialState.field_179190_c)
        {
            GlStateManager.colorMaterialState.field_179189_b = face;
            GlStateManager.colorMaterialState.field_179190_c = mode;
            GL11.glColorMaterial(face, mode);
        }
    }

    public static void disableDepth()
    {
        GlStateManager.depthState.depthTest.setDisabled();
    }

    public static void enableDepth()
    {
        GlStateManager.depthState.depthTest.setEnabled();
    }

    public static void depthFunc(int depthFunc)
    {
        if (depthFunc != GlStateManager.depthState.depthFunc)
        {
            GlStateManager.depthState.depthFunc = depthFunc;
            GL11.glDepthFunc(depthFunc);
        }
    }

    public static void depthMask(boolean flagIn)
    {
        if (flagIn != GlStateManager.depthState.maskEnabled)
        {
            GlStateManager.depthState.maskEnabled = flagIn;
            GL11.glDepthMask(flagIn);
        }
    }

    public static void disableBlend()
    {
        GlStateManager.blendState.field_179213_a.setDisabled();
    }

    public static void enableBlend()
    {
        GlStateManager.blendState.field_179213_a.setEnabled();
    }

    public static void blendFunc(int srcFactor, int dstFactor)
    {
        if (srcFactor != GlStateManager.blendState.srcFactor || dstFactor != GlStateManager.blendState.dstFactor)
        {
            GlStateManager.blendState.srcFactor = srcFactor;
            GlStateManager.blendState.dstFactor = dstFactor;
            GL11.glBlendFunc(srcFactor, dstFactor);
        }
    }

    public static void tryBlendFuncSeparate(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha)
    {
        if (srcFactor != GlStateManager.blendState.srcFactor || dstFactor != GlStateManager.blendState.dstFactor || srcFactorAlpha != GlStateManager.blendState.srcFactorAlpha || dstFactorAlpha != GlStateManager.blendState.dstFactorAlpha)
        {
            GlStateManager.blendState.srcFactor = srcFactor;
            GlStateManager.blendState.dstFactor = dstFactor;
            GlStateManager.blendState.srcFactorAlpha = srcFactorAlpha;
            GlStateManager.blendState.dstFactorAlpha = dstFactorAlpha;
            OpenGlHelper.glBlendFunc(srcFactor, dstFactor, srcFactorAlpha, dstFactorAlpha);
        }
    }

    public static void enableFog()
    {
        GlStateManager.fogState.field_179049_a.setEnabled();
    }

    public static void disableFog()
    {
        GlStateManager.fogState.field_179049_a.setDisabled();
    }

    public static void setFog(int param)
    {
        if (param != GlStateManager.fogState.field_179047_b)
        {
            GlStateManager.fogState.field_179047_b = param;
            GL11.glFogi(GL11.GL_FOG_MODE, param);
        }
    }

    public static void setFogDensity(float param)
    {
        if (param != GlStateManager.fogState.field_179048_c)
        {
            GlStateManager.fogState.field_179048_c = param;
            GL11.glFogf(GL11.GL_FOG_DENSITY, param);
        }
    }

    public static void setFogStart(float param)
    {
        if (param != GlStateManager.fogState.field_179045_d)
        {
            GlStateManager.fogState.field_179045_d = param;
            GL11.glFogf(GL11.GL_FOG_START, param);
        }
    }

    public static void setFogEnd(float param)
    {
        if (param != GlStateManager.fogState.field_179046_e)
        {
            GlStateManager.fogState.field_179046_e = param;
            GL11.glFogf(GL11.GL_FOG_END, param);
        }
    }

    public static void enableCull()
    {
        GlStateManager.cullState.field_179054_a.setEnabled();
    }

    public static void disableCull()
    {
        GlStateManager.cullState.field_179054_a.setDisabled();
    }

    public static void cullFace(int mode)
    {
        if (mode != GlStateManager.cullState.field_179053_b)
        {
            GlStateManager.cullState.field_179053_b = mode;
            GL11.glCullFace(mode);
        }
    }

    public static void enablePolygonOffset()
    {
        GlStateManager.polygonOffsetState.field_179044_a.setEnabled();
    }

    public static void disablePolygonOffset()
    {
        GlStateManager.polygonOffsetState.field_179044_a.setDisabled();
    }

    public static void doPolygonOffset(float factor, float units)
    {
        if (factor != GlStateManager.polygonOffsetState.field_179043_c || units != GlStateManager.polygonOffsetState.field_179041_d)
        {
            GlStateManager.polygonOffsetState.field_179043_c = factor;
            GlStateManager.polygonOffsetState.field_179041_d = units;
            GL11.glPolygonOffset(factor, units);
        }
    }

    public static void enableColorLogic()
    {
        GlStateManager.colorLogicState.field_179197_a.setEnabled();
    }

    public static void disableColorLogic()
    {
        GlStateManager.colorLogicState.field_179197_a.setDisabled();
    }

    public static void colorLogicOp(int opcode)
    {
        if (opcode != GlStateManager.colorLogicState.field_179196_b)
        {
            GlStateManager.colorLogicState.field_179196_b = opcode;
            GL11.glLogicOp(opcode);
        }
    }

    public static void enableTexGenCoord(GlStateManager.TexGen p_179087_0_)
    {
        GlStateManager.texGenCoord(p_179087_0_).field_179067_a.setEnabled();
    }

    public static void disableTexGenCoord(GlStateManager.TexGen p_179100_0_)
    {
        GlStateManager.texGenCoord(p_179100_0_).field_179067_a.setDisabled();
    }

    public static void texGen(GlStateManager.TexGen p_179149_0_, int p_179149_1_)
    {
        GlStateManager.TexGenCoord glstatemanager$texgencoord = GlStateManager.texGenCoord(p_179149_0_);

        if (p_179149_1_ != glstatemanager$texgencoord.field_179066_c)
        {
            glstatemanager$texgencoord.field_179066_c = p_179149_1_;
            GL11.glTexGeni(glstatemanager$texgencoord.field_179065_b, GL11.GL_TEXTURE_GEN_MODE, p_179149_1_);
        }
    }

    public static void func_179105_a(GlStateManager.TexGen p_179105_0_, int pname, FloatBuffer params)
    {
        GL11.glTexGen(GlStateManager.texGenCoord(p_179105_0_).field_179065_b, pname, params);
    }

    private static GlStateManager.TexGenCoord texGenCoord(GlStateManager.TexGen p_179125_0_)
    {
        switch (GlStateManager.GlStateManager$1.field_179175_a[p_179125_0_.ordinal()])
        {
            case 1:
                return GlStateManager.texGenState.field_179064_a;

            case 2:
                return GlStateManager.texGenState.field_179062_b;

            case 3:
                return GlStateManager.texGenState.field_179063_c;

            case 4:
                return GlStateManager.texGenState.field_179061_d;

            default:
                return GlStateManager.texGenState.field_179064_a;
        }
    }

    public static void setActiveTexture(int texture)
    {
        if (GlStateManager.activeTextureUnit != texture - OpenGlHelper.defaultTexUnit)
        {
            GlStateManager.activeTextureUnit = texture - OpenGlHelper.defaultTexUnit;
            OpenGlHelper.setActiveTexture(texture);
        }
    }

    public static void enableTexture2D()
    {
        GlStateManager.textureState[GlStateManager.activeTextureUnit].texture2DState.setEnabled();
    }

    public static void disableTexture2D()
    {
        GlStateManager.textureState[GlStateManager.activeTextureUnit].texture2DState.setDisabled();
    }

    public static int generateTexture()
    {
        return GL11.glGenTextures();
    }

    public static void deleteTexture(int texture)
    {
        if (texture != 0)
        {
            GL11.glDeleteTextures(texture);

            for (GlStateManager.TextureState glstatemanager$texturestate : GlStateManager.textureState)
            {
                if (glstatemanager$texturestate.textureName == texture)
                {
                    glstatemanager$texturestate.textureName = 0;
                }
            }
        }
    }

    public static void bindTexture(int texture)
    {
        if (texture != GlStateManager.textureState[GlStateManager.activeTextureUnit].textureName)
        {
            GlStateManager.textureState[GlStateManager.activeTextureUnit].textureName = texture;
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        }
    }

    public static void bindCurrentTexture()
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, GlStateManager.textureState[GlStateManager.activeTextureUnit].textureName);
    }

    public static void enableNormalize()
    {
        GlStateManager.normalizeState.setEnabled();
    }

    public static void disableNormalize()
    {
        GlStateManager.normalizeState.setDisabled();
    }

    public static void shadeModel(int mode)
    {
        if (mode != GlStateManager.activeShadeModel)
        {
            GlStateManager.activeShadeModel = mode;
            GL11.glShadeModel(mode);
        }
    }

    public static void enableRescaleNormal()
    {
        GlStateManager.rescaleNormalState.setEnabled();
    }

    public static void disableRescaleNormal()
    {
        GlStateManager.rescaleNormalState.setDisabled();
    }

    public static void viewport(int x, int y, int width, int height)
    {
        GL11.glViewport(x, y, width, height);
    }

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha)
    {
        if (red != GlStateManager.colorMaskState.red || green != GlStateManager.colorMaskState.green || blue != GlStateManager.colorMaskState.blue || alpha != GlStateManager.colorMaskState.alpha)
        {
            GlStateManager.colorMaskState.red = red;
            GlStateManager.colorMaskState.green = green;
            GlStateManager.colorMaskState.blue = blue;
            GlStateManager.colorMaskState.alpha = alpha;
            GL11.glColorMask(red, green, blue, alpha);
        }
    }

    public static void clearDepth(double depth)
    {
        if (depth != GlStateManager.clearState.field_179205_a)
        {
            GlStateManager.clearState.field_179205_a = depth;
            GL11.glClearDepth(depth);
        }
    }

    public static void clearColor(float red, float green, float blue, float alpha)
    {
        if (red != GlStateManager.clearState.field_179203_b.red || green != GlStateManager.clearState.field_179203_b.green || blue != GlStateManager.clearState.field_179203_b.blue || alpha != GlStateManager.clearState.field_179203_b.alpha)
        {
            GlStateManager.clearState.field_179203_b.red = red;
            GlStateManager.clearState.field_179203_b.green = green;
            GlStateManager.clearState.field_179203_b.blue = blue;
            GlStateManager.clearState.field_179203_b.alpha = alpha;
            GL11.glClearColor(red, green, blue, alpha);
        }
    }

    public static void clear(int mask)
    {
        if (GlStateManager.clearEnabled)
        {
            GL11.glClear(mask);
        }
    }

    public static void matrixMode(int mode)
    {
        GL11.glMatrixMode(mode);
    }

    public static void loadIdentity()
    {
        GL11.glLoadIdentity();
    }

    public static void pushMatrix()
    {
        GL11.glPushMatrix();
    }

    public static void popMatrix()
    {
        GL11.glPopMatrix();
    }

    public static void getFloat(int pname, FloatBuffer params)
    {
        GL11.glGetFloat(pname, params);
    }

    public static void ortho(double left, double right, double bottom, double top, double zNear, double zFar)
    {
        GL11.glOrtho(left, right, bottom, top, zNear, zFar);
    }

    public static void rotate(float angle, float x, float y, float z)
    {
        GL11.glRotatef(angle, x, y, z);
    }

    public static void scale(float x, float y, float z)
    {
        GL11.glScalef(x, y, z);
    }

    public static void scale(double x, double y, double z)
    {
        GL11.glScaled(x, y, z);
    }

    public static void translate(float x, float y, float z)
    {
        GL11.glTranslatef(x, y, z);
    }

    public static void translate(double x, double y, double z)
    {
        GL11.glTranslated(x, y, z);
    }

    public static void multMatrix(FloatBuffer matrix)
    {
        GL11.glMultMatrix(matrix);
    }

    public static void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha)
    {
        if (colorRed != GlStateManager.colorState.red || colorGreen != GlStateManager.colorState.green || colorBlue != GlStateManager.colorState.blue || colorAlpha != GlStateManager.colorState.alpha)
        {
            GlStateManager.colorState.red = colorRed;
            GlStateManager.colorState.green = colorGreen;
            GlStateManager.colorState.blue = colorBlue;
            GlStateManager.colorState.alpha = colorAlpha;
            GL11.glColor4f(colorRed, colorGreen, colorBlue, colorAlpha);
        }
    }

    public static void color(float colorRed, float colorGreen, float colorBlue)
    {
        GlStateManager.color(colorRed, colorGreen, colorBlue, 1.0F);
    }

    public static void resetColor()
    {
        GlStateManager.colorState.red = GlStateManager.colorState.green = GlStateManager.colorState.blue = GlStateManager.colorState.alpha = -1.0F;
    }

    public static void callList(int list)
    {
        GL11.glCallList(list);
    }

    public static int getActiveTextureUnit()
    {
        return OpenGlHelper.defaultTexUnit + GlStateManager.activeTextureUnit;
    }

    public static int getBoundTexture()
    {
        return GlStateManager.textureState[GlStateManager.activeTextureUnit].textureName;
    }

    public static void checkBoundTexture()
    {
        if (Config.isMinecraftThread())
        {
            int i = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
            int j = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            int k = GlStateManager.getActiveTextureUnit();
            int l = GlStateManager.getBoundTexture();

            if (l > 0)
            {
                if (i != k || j != l)
                {
                    Config.dbg("checkTexture: act: " + k + ", glAct: " + i + ", tex: " + l + ", glTex: " + j);
                }
            }
        }
    }

    public static void deleteTextures(IntBuffer p_deleteTextures_0_)
    {
        p_deleteTextures_0_.rewind();

        while (p_deleteTextures_0_.position() < p_deleteTextures_0_.limit())
        {
            int i = p_deleteTextures_0_.get();
            GlStateManager.deleteTexture(i);
        }

        p_deleteTextures_0_.rewind();
    }

    static
    {
        for (int i = 0; i < 8; ++i)
        {
            GlStateManager.lightState[i] = new GlStateManager.BooleanState(16384 + i);
        }

        for (int j = 0; j < GlStateManager.textureState.length; ++j)
        {
            GlStateManager.textureState[j] = new GlStateManager.TextureState(null);
        }
    }

    static final class GlStateManager$1
    {
        static final int[] field_179175_a = new int[GlStateManager.TexGen.values().length];
        private static final String __OBFID = "CL_00002557";

        static
        {
            try
            {
                GlStateManager$1.field_179175_a[GlStateManager.TexGen.S.ordinal()] = 1;
            }
            catch (NoSuchFieldError var4)
            {
            }

            try
            {
                GlStateManager$1.field_179175_a[GlStateManager.TexGen.T.ordinal()] = 2;
            }
            catch (NoSuchFieldError var3)
            {
            }

            try
            {
                GlStateManager$1.field_179175_a[GlStateManager.TexGen.R.ordinal()] = 3;
            }
            catch (NoSuchFieldError var2)
            {
            }

            try
            {
                GlStateManager$1.field_179175_a[GlStateManager.TexGen.Q.ordinal()] = 4;
            }
            catch (NoSuchFieldError var1)
            {
            }
        }
    }

    static class AlphaState
    {
        public GlStateManager.BooleanState field_179208_a;
        public int func;
        public float ref;
        private static final String __OBFID = "CL_00002556";

        private AlphaState()
        {
            field_179208_a = new GlStateManager.BooleanState(3008);
            func = 519;
            ref = -1.0F;
        }

        AlphaState(GlStateManager.GlStateManager$1 p_i46489_1_)
        {
            this();
        }
    }

    static class BlendState
    {
        public GlStateManager.BooleanState field_179213_a;
        public int srcFactor;
        public int dstFactor;
        public int srcFactorAlpha;
        public int dstFactorAlpha;
        private static final String __OBFID = "CL_00002555";

        private BlendState()
        {
            field_179213_a = new GlStateManager.BooleanState(3042);
            srcFactor = 1;
            dstFactor = 0;
            srcFactorAlpha = 1;
            dstFactorAlpha = 0;
        }

        BlendState(GlStateManager.GlStateManager$1 p_i46488_1_)
        {
            this();
        }
    }

    static class BooleanState
    {
        private final int capability;
        private boolean currentState = false;
        private static final String __OBFID = "CL_00002554";

        public BooleanState(int capabilityIn)
        {
            capability = capabilityIn;
        }

        public void setDisabled()
        {
            setState(false);
        }

        public void setEnabled()
        {
            setState(true);
        }

        public void setState(boolean state)
        {
            if (state != currentState)
            {
                currentState = state;

                if (state)
                {
                    GL11.glEnable(capability);
                }
                else
                {
                    GL11.glDisable(capability);
                }
            }
        }
    }

    static class ClearState
    {
        public double field_179205_a;
        public GlStateManager.Color field_179203_b;
        public int field_179204_c;
        private static final String __OBFID = "CL_00002553";

        private ClearState()
        {
            field_179205_a = 1.0D;
            field_179203_b = new GlStateManager.Color(0.0F, 0.0F, 0.0F, 0.0F);
            field_179204_c = 0;
        }

        ClearState(GlStateManager.GlStateManager$1 p_i46487_1_)
        {
            this();
        }
    }

    static class Color
    {
        public float red = 1.0F;
        public float green = 1.0F;
        public float blue = 1.0F;
        public float alpha = 1.0F;
        private static final String __OBFID = "CL_00002552";

        public Color()
        {
        }

        public Color(float redIn, float greenIn, float blueIn, float alphaIn)
        {
            red = redIn;
            green = greenIn;
            blue = blueIn;
            alpha = alphaIn;
        }
    }

    static class ColorLogicState
    {
        public GlStateManager.BooleanState field_179197_a;
        public int field_179196_b;
        private static final String __OBFID = "CL_00002551";

        private ColorLogicState()
        {
            field_179197_a = new GlStateManager.BooleanState(3058);
            field_179196_b = 5379;
        }

        ColorLogicState(GlStateManager.GlStateManager$1 p_i46486_1_)
        {
            this();
        }
    }

    static class ColorMask
    {
        public boolean red;
        public boolean green;
        public boolean blue;
        public boolean alpha;
        private static final String __OBFID = "CL_00002550";

        private ColorMask()
        {
            red = true;
            green = true;
            blue = true;
            alpha = true;
        }

        ColorMask(GlStateManager.GlStateManager$1 p_i46485_1_)
        {
            this();
        }
    }

    static class ColorMaterialState
    {
        public GlStateManager.BooleanState field_179191_a;
        public int field_179189_b;
        public int field_179190_c;
        private static final String __OBFID = "CL_00002549";

        private ColorMaterialState()
        {
            field_179191_a = new GlStateManager.BooleanState(2903);
            field_179189_b = 1032;
            field_179190_c = 5634;
        }

        ColorMaterialState(GlStateManager.GlStateManager$1 p_i46484_1_)
        {
            this();
        }
    }

    static class CullState
    {
        public GlStateManager.BooleanState field_179054_a;
        public int field_179053_b;
        private static final String __OBFID = "CL_00002548";

        private CullState()
        {
            field_179054_a = new GlStateManager.BooleanState(2884);
            field_179053_b = 1029;
        }

        CullState(GlStateManager.GlStateManager$1 p_i46483_1_)
        {
            this();
        }
    }

    static class DepthState
    {
        public GlStateManager.BooleanState depthTest;
        public boolean maskEnabled;
        public int depthFunc;
        private static final String __OBFID = "CL_00002547";

        private DepthState()
        {
            depthTest = new GlStateManager.BooleanState(2929);
            maskEnabled = true;
            depthFunc = 513;
        }

        DepthState(GlStateManager.GlStateManager$1 p_i46482_1_)
        {
            this();
        }
    }

    static class FogState
    {
        public GlStateManager.BooleanState field_179049_a;
        public int field_179047_b;
        public float field_179048_c;
        public float field_179045_d;
        public float field_179046_e;
        private static final String __OBFID = "CL_00002546";

        private FogState()
        {
            field_179049_a = new GlStateManager.BooleanState(2912);
            field_179047_b = 2048;
            field_179048_c = 1.0F;
            field_179045_d = 0.0F;
            field_179046_e = 1.0F;
        }

        FogState(GlStateManager.GlStateManager$1 p_i46481_1_)
        {
            this();
        }
    }

    static class PolygonOffsetState
    {
        public GlStateManager.BooleanState field_179044_a;
        public GlStateManager.BooleanState field_179042_b;
        public float field_179043_c;
        public float field_179041_d;
        private static final String __OBFID = "CL_00002545";

        private PolygonOffsetState()
        {
            field_179044_a = new GlStateManager.BooleanState(32823);
            field_179042_b = new GlStateManager.BooleanState(10754);
            field_179043_c = 0.0F;
            field_179041_d = 0.0F;
        }

        PolygonOffsetState(GlStateManager.GlStateManager$1 p_i46480_1_)
        {
            this();
        }
    }

    static class StencilFunc
    {
        public int field_179081_a;
        public int field_179079_b;
        public int field_179080_c;
        private static final String __OBFID = "CL_00002544";

        private StencilFunc()
        {
            field_179081_a = 519;
            field_179079_b = 0;
            field_179080_c = -1;
        }

        StencilFunc(GlStateManager.GlStateManager$1 p_i46479_1_)
        {
            this();
        }
    }

    static class StencilState
    {
        public GlStateManager.StencilFunc field_179078_a;
        public int field_179076_b;
        public int field_179077_c;
        public int field_179074_d;
        public int field_179075_e;
        private static final String __OBFID = "CL_00002543";

        private StencilState()
        {
            field_179078_a = new GlStateManager.StencilFunc(null);
            field_179076_b = -1;
            field_179077_c = 7680;
            field_179074_d = 7680;
            field_179075_e = 7680;
        }

        StencilState(GlStateManager.GlStateManager$1 p_i46478_1_)
        {
            this();
        }
    }

    public static enum TexGen
    {
        S("S", 0),
        T("T", 1),
        R("R", 2),
        Q("Q", 3);

        private static final GlStateManager.TexGen[] $VALUES = new GlStateManager.TexGen[]{TexGen.S, TexGen.T, TexGen.R, TexGen.Q};
        private static final String __OBFID = "CL_00002542";

        private TexGen(String p_i3_3_, int p_i3_4_)
        {
        }
    }

    static class TexGenCoord
    {
        public GlStateManager.BooleanState field_179067_a;
        public int field_179065_b;
        public int field_179066_c = -1;
        private static final String __OBFID = "CL_00002541";

        public TexGenCoord(int p_i46254_1_, int p_i46254_2_)
        {
            field_179065_b = p_i46254_1_;
            field_179067_a = new GlStateManager.BooleanState(p_i46254_2_);
        }
    }

    static class TexGenState
    {
        public GlStateManager.TexGenCoord field_179064_a;
        public GlStateManager.TexGenCoord field_179062_b;
        public GlStateManager.TexGenCoord field_179063_c;
        public GlStateManager.TexGenCoord field_179061_d;
        private static final String __OBFID = "CL_00002540";

        private TexGenState()
        {
            field_179064_a = new GlStateManager.TexGenCoord(8192, 3168);
            field_179062_b = new GlStateManager.TexGenCoord(8193, 3169);
            field_179063_c = new GlStateManager.TexGenCoord(8194, 3170);
            field_179061_d = new GlStateManager.TexGenCoord(8195, 3171);
        }

        TexGenState(GlStateManager.GlStateManager$1 p_i46477_1_)
        {
            this();
        }
    }

    static class TextureState
    {
        public GlStateManager.BooleanState texture2DState;
        public int textureName;
        private static final String __OBFID = "CL_00002539";

        private TextureState()
        {
            texture2DState = new GlStateManager.BooleanState(3553);
            textureName = 0;
        }

        TextureState(GlStateManager.GlStateManager$1 p_i46476_1_)
        {
            this();
        }
    }
}
