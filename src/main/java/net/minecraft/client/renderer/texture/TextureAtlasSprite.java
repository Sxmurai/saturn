package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import optifine.Config;
import optifine.TextureUtils;
import shadersmod.client.Shaders;

public class TextureAtlasSprite
{
    private final String iconName;
    protected List framesTextureData = Lists.newArrayList();
    protected int[][] interpolatedFrameData;
    private AnimationMetadataSection animationMetadata;
    protected boolean rotated;
    protected int originX;
    protected int originY;
    protected int width;
    protected int height;
    private float minU;
    private float maxU;
    private float minV;
    private float maxV;
    protected int frameCounter;
    protected int tickCounter;
    private static String locationNameClock = "builtin/clock";
    private static String locationNameCompass = "builtin/compass";
    private static final String __OBFID = "CL_00001062";
    private int indexInMap = -1;
    public float baseU;
    public float baseV;
    public int sheetWidth;
    public int sheetHeight;
    public int glSpriteTextureId = -1;
    public TextureAtlasSprite spriteSingle = null;
    public boolean isSpriteSingle = false;
    public int mipmapLevels = 0;
    public TextureAtlasSprite spriteNormal = null;
    public TextureAtlasSprite spriteSpecular = null;
    public boolean isShadersSprite = false;

    private TextureAtlasSprite(TextureAtlasSprite p_i12_1_)
    {
        iconName = p_i12_1_.iconName;
        isSpriteSingle = true;
    }

    protected TextureAtlasSprite(String spriteName)
    {
        iconName = spriteName;

        if (Config.isMultiTexture())
        {
            spriteSingle = new TextureAtlasSprite(this);
        }
    }

    protected static TextureAtlasSprite makeAtlasSprite(ResourceLocation spriteResourceLocation)
    {
        String s = spriteResourceLocation.toString();
        return TextureAtlasSprite.locationNameClock.equals(s) ? new TextureClock(s) : (TextureAtlasSprite.locationNameCompass.equals(s) ? new TextureCompass(s) : new TextureAtlasSprite(s));
    }

    public static void setLocationNameClock(String clockName)
    {
        TextureAtlasSprite.locationNameClock = clockName;
    }

    public static void setLocationNameCompass(String compassName)
    {
        TextureAtlasSprite.locationNameCompass = compassName;
    }

    public void initSprite(int inX, int inY, int originInX, int originInY, boolean rotatedIn)
    {
        originX = originInX;
        originY = originInY;
        rotated = rotatedIn;
        float f = (float)(0.009999999776482582D / (double)inX);
        float f1 = (float)(0.009999999776482582D / (double)inY);
        minU = (float)originInX / (float)((double)inX) + f;
        maxU = (float)(originInX + width) / (float)((double)inX) - f;
        minV = (float)originInY / (float)inY + f1;
        maxV = (float)(originInY + height) / (float)inY - f1;
        baseU = Math.min(minU, maxU);
        baseV = Math.min(minV, maxV);

        if (spriteSingle != null)
        {
            spriteSingle.initSprite(width, height, 0, 0, false);
        }

        if (spriteNormal != null)
        {
            spriteNormal.initSprite(inX, inY, originInX, originInY, rotatedIn);
        }

        if (spriteSpecular != null)
        {
            spriteSpecular.initSprite(inX, inY, originInX, originInY, rotatedIn);
        }
    }

    public void copyFrom(TextureAtlasSprite atlasSpirit)
    {
        originX = atlasSpirit.originX;
        originY = atlasSpirit.originY;
        width = atlasSpirit.width;
        height = atlasSpirit.height;
        rotated = atlasSpirit.rotated;
        minU = atlasSpirit.minU;
        maxU = atlasSpirit.maxU;
        minV = atlasSpirit.minV;
        maxV = atlasSpirit.maxV;

        if (spriteSingle != null)
        {
            spriteSingle.initSprite(width, height, 0, 0, false);
        }
    }

    /**
     * Returns the X position of this icon on its texture sheet, in pixels.
     */
    public int getOriginX()
    {
        return originX;
    }

    /**
     * Returns the Y position of this icon on its texture sheet, in pixels.
     */
    public int getOriginY()
    {
        return originY;
    }

    /**
     * Returns the width of the icon, in pixels.
     */
    public int getIconWidth()
    {
        return width;
    }

    /**
     * Returns the height of the icon, in pixels.
     */
    public int getIconHeight()
    {
        return height;
    }

    /**
     * Returns the minimum U coordinate to use when rendering with this icon.
     */
    public float getMinU()
    {
        return minU;
    }

    /**
     * Returns the maximum U coordinate to use when rendering with this icon.
     */
    public float getMaxU()
    {
        return maxU;
    }

    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 16 returns uMax. Other arguments return in-between values.
     */
    public float getInterpolatedU(double u)
    {
        float f = maxU - minU;
        return minU + f * (float)u / 16.0F;
    }

    /**
     * Returns the minimum V coordinate to use when rendering with this icon.
     */
    public float getMinV()
    {
        return minV;
    }

    /**
     * Returns the maximum V coordinate to use when rendering with this icon.
     */
    public float getMaxV()
    {
        return maxV;
    }

    /**
     * Gets a V coordinate on the icon. 0 returns vMin and 16 returns vMax. Other arguments return in-between values.
     */
    public float getInterpolatedV(double v)
    {
        float f = maxV - minV;
        return minV + f * ((float)v / 16.0F);
    }

    public String getIconName()
    {
        return iconName;
    }

    public void updateAnimation()
    {
        if (animationMetadata != null)
        {
            ++tickCounter;

            if (tickCounter >= animationMetadata.getFrameTimeSingle(frameCounter))
            {
                int i = animationMetadata.getFrameIndex(frameCounter);
                int j = animationMetadata.getFrameCount() == 0 ? framesTextureData.size() : animationMetadata.getFrameCount();
                frameCounter = (frameCounter + 1) % j;
                tickCounter = 0;
                int k = animationMetadata.getFrameIndex(frameCounter);
                boolean flag = false;
                boolean flag1 = isSpriteSingle;

                if (i != k && k >= 0 && k < framesTextureData.size())
                {
                    TextureUtil.uploadTextureMipmap((int[][]) framesTextureData.get(k), width, height, originX, originY, flag, flag1);
                }
            }
            else if (animationMetadata.isInterpolate())
            {
                updateAnimationInterpolated();
            }
        }
    }

    private void updateAnimationInterpolated()
    {
        double d0 = 1.0D - (double) tickCounter / (double) animationMetadata.getFrameTimeSingle(frameCounter);
        int i = animationMetadata.getFrameIndex(frameCounter);
        int j = animationMetadata.getFrameCount() == 0 ? framesTextureData.size() : animationMetadata.getFrameCount();
        int k = animationMetadata.getFrameIndex((frameCounter + 1) % j);

        if (i != k && k >= 0 && k < framesTextureData.size())
        {
            int[][] aint = (int[][]) framesTextureData.get(i);
            int[][] aint1 = (int[][]) framesTextureData.get(k);

            if (interpolatedFrameData == null || interpolatedFrameData.length != aint.length)
            {
                interpolatedFrameData = new int[aint.length][];
            }

            for (int l = 0; l < aint.length; ++l)
            {
                if (interpolatedFrameData[l] == null)
                {
                    interpolatedFrameData[l] = new int[aint[l].length];
                }

                if (l < aint1.length && aint1[l].length == aint[l].length)
                {
                    for (int i1 = 0; i1 < aint[l].length; ++i1)
                    {
                        int j1 = aint[l][i1];
                        int k1 = aint1[l][i1];
                        int l1 = (int)((double)((j1 & 16711680) >> 16) * d0 + (double)((k1 & 16711680) >> 16) * (1.0D - d0));
                        int i2 = (int)((double)((j1 & 65280) >> 8) * d0 + (double)((k1 & 65280) >> 8) * (1.0D - d0));
                        int j2 = (int)((double)(j1 & 255) * d0 + (double)(k1 & 255) * (1.0D - d0));
                        interpolatedFrameData[l][i1] = j1 & -16777216 | l1 << 16 | i2 << 8 | j2;
                    }
                }
            }

            TextureUtil.uploadTextureMipmap(interpolatedFrameData, width, height, originX, originY, false, false);
        }
    }

    public int[][] getFrameTextureData(int index)
    {
        return (int[][]) framesTextureData.get(index);
    }

    public int getFrameCount()
    {
        return framesTextureData.size();
    }

    public void setIconWidth(int newWidth)
    {
        width = newWidth;

        if (spriteSingle != null)
        {
            spriteSingle.setIconWidth(width);
        }
    }

    public void setIconHeight(int newHeight)
    {
        height = newHeight;

        if (spriteSingle != null)
        {
            spriteSingle.setIconHeight(height);
        }
    }

    public void loadSprite(BufferedImage[] images, AnimationMetadataSection meta) throws IOException
    {
        resetSprite();
        int i = images[0].getWidth();
        int j = images[0].getHeight();
        width = i;
        height = j;
        int[][] aint = new int[images.length][];

        for (int k = 0; k < images.length; ++k)
        {
            BufferedImage bufferedimage = images[k];

            if (bufferedimage != null)
            {
                if (k > 0 && (bufferedimage.getWidth() != i >> k || bufferedimage.getHeight() != j >> k))
                {
                    throw new RuntimeException(String.format("Unable to load miplevel: %d, image is size: %dx%d, expected %dx%d", Integer.valueOf(k), Integer.valueOf(bufferedimage.getWidth()), Integer.valueOf(bufferedimage.getHeight()), Integer.valueOf(i >> k), Integer.valueOf(j >> k)));
                }

                aint[k] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
                bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), aint[k], 0, bufferedimage.getWidth());
            }
        }

        if (meta == null)
        {
            if (j != i)
            {
                throw new RuntimeException("broken aspect ratio and not an animation");
            }

            framesTextureData.add(aint);
        }
        else
        {
            int j1 = j / i;
            int k1 = i;
            int l = i;
            height = width;

            if (meta.getFrameCount() > 0)
            {
                Iterator iterator = meta.getFrameIndexSet().iterator();

                while (iterator.hasNext())
                {
                    int i1 = ((Integer)iterator.next()).intValue();

                    if (i1 >= j1)
                    {
                        throw new RuntimeException("invalid frameindex " + i1);
                    }

                    allocateFrameTextureData(i1);
                    framesTextureData.set(i1, TextureAtlasSprite.getFrameTextureData(aint, k1, l, i1));
                }

                animationMetadata = meta;
            }
            else
            {
                ArrayList arraylist = Lists.newArrayList();

                for (int i2 = 0; i2 < j1; ++i2)
                {
                    framesTextureData.add(TextureAtlasSprite.getFrameTextureData(aint, k1, l, i2));
                    arraylist.add(new AnimationFrame(i2, -1));
                }

                animationMetadata = new AnimationMetadataSection(arraylist, width, height, meta.getFrameTime(), meta.isInterpolate());
            }
        }

        if (!isShadersSprite)
        {
            if (Config.isShaders())
            {
                loadShadersSprites();
            }

            for (int l1 = 0; l1 < framesTextureData.size(); ++l1)
            {
                int[][] aint1 = (int[][]) framesTextureData.get(l1);

                if (aint1 != null && !iconName.startsWith("minecraft:blocks/leaves_"))
                {
                    for (int j2 = 0; j2 < aint1.length; ++j2)
                    {
                        int[] aint2 = aint1[j2];
                        fixTransparentColor(aint2);
                    }
                }
            }

            if (spriteSingle != null)
            {
                spriteSingle.loadSprite(images, meta);
            }
        }
    }

    public void generateMipmaps(int level)
    {
        ArrayList arraylist = Lists.newArrayList();

        for (int i = 0; i < framesTextureData.size(); ++i)
        {
            final int[][] aint = (int[][]) framesTextureData.get(i);

            if (aint != null)
            {
                try
                {
                    arraylist.add(TextureUtil.generateMipmapData(level, width, aint));
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Generating mipmaps for frame");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Frame being iterated");
                    crashreportcategory.addCrashSection("Frame index", Integer.valueOf(i));
                    crashreportcategory.addCrashSectionCallable("Frame sizes", new Callable()
                    {
                        private static final String __OBFID = "CL_00001063";
                        public String call() throws Exception
                        {
                            StringBuilder stringbuilder = new StringBuilder();

                            for (int[] aint1 : aint)
                            {
                                if (stringbuilder.length() > 0)
                                {
                                    stringbuilder.append(", ");
                                }

                                stringbuilder.append(aint1 == null ? "null" : Integer.valueOf(aint1.length));
                            }

                            return stringbuilder.toString();
                        }
                    });
                    throw new ReportedException(crashreport);
                }
            }
        }

        setFramesTextureData(arraylist);

        if (spriteSingle != null)
        {
            spriteSingle.generateMipmaps(level);
        }
    }

    private void allocateFrameTextureData(int index)
    {
        if (framesTextureData.size() <= index)
        {
            for (int i = framesTextureData.size(); i <= index; ++i)
            {
                framesTextureData.add(null);
            }
        }

        if (spriteSingle != null)
        {
            spriteSingle.allocateFrameTextureData(index);
        }
    }

    private static int[][] getFrameTextureData(int[][] data, int rows, int columns, int p_147962_3_)
    {
        int[][] aint = new int[data.length][];

        for (int i = 0; i < data.length; ++i)
        {
            int[] aint1 = data[i];

            if (aint1 != null)
            {
                aint[i] = new int[(rows >> i) * (columns >> i)];
                System.arraycopy(aint1, p_147962_3_ * aint[i].length, aint[i], 0, aint[i].length);
            }
        }

        return aint;
    }

    public void clearFramesTextureData()
    {
        framesTextureData.clear();

        if (spriteSingle != null)
        {
            spriteSingle.clearFramesTextureData();
        }
    }

    public boolean hasAnimationMetadata()
    {
        return animationMetadata != null;
    }

    public void setFramesTextureData(List newFramesTextureData)
    {
        framesTextureData = newFramesTextureData;

        if (spriteSingle != null)
        {
            spriteSingle.setFramesTextureData(newFramesTextureData);
        }
    }

    private void resetSprite()
    {
        animationMetadata = null;
        setFramesTextureData(Lists.newArrayList());
        frameCounter = 0;
        tickCounter = 0;

        if (spriteSingle != null)
        {
            spriteSingle.resetSprite();
        }
    }

    public String toString()
    {
        return "TextureAtlasSprite{name='" + iconName + '\'' + ", frameCount=" + framesTextureData.size() + ", rotated=" + rotated + ", x=" + originX + ", y=" + originY + ", height=" + height + ", width=" + width + ", u0=" + minU + ", u1=" + maxU + ", v0=" + minV + ", v1=" + maxV + '}';
    }

    public boolean hasCustomLoader(IResourceManager p_hasCustomLoader_1_, ResourceLocation p_hasCustomLoader_2_)
    {
        return false;
    }

    public boolean load(IResourceManager p_load_1_, ResourceLocation p_load_2_)
    {
        return true;
    }

    public int getIndexInMap()
    {
        return indexInMap;
    }

    public void setIndexInMap(int p_setIndexInMap_1_)
    {
        indexInMap = p_setIndexInMap_1_;
    }

    private void fixTransparentColor(int[] p_fixTransparentColor_1_)
    {
        if (p_fixTransparentColor_1_ != null)
        {
            long i = 0L;
            long j = 0L;
            long k = 0L;
            long l = 0L;

            for (int i1 = 0; i1 < p_fixTransparentColor_1_.length; ++i1)
            {
                int j1 = p_fixTransparentColor_1_[i1];
                int k1 = j1 >> 24 & 255;

                if (k1 >= 16)
                {
                    int l1 = j1 >> 16 & 255;
                    int i2 = j1 >> 8 & 255;
                    int j2 = j1 & 255;
                    i += l1;
                    j += i2;
                    k += j2;
                    ++l;
                }
            }

            if (l > 0L)
            {
                int l2 = (int)(i / l);
                int i3 = (int)(j / l);
                int j3 = (int)(k / l);
                int k3 = l2 << 16 | i3 << 8 | j3;

                for (int l3 = 0; l3 < p_fixTransparentColor_1_.length; ++l3)
                {
                    int i4 = p_fixTransparentColor_1_[l3];
                    int k2 = i4 >> 24 & 255;

                    if (k2 <= 16)
                    {
                        p_fixTransparentColor_1_[l3] = k3;
                    }
                }
            }
        }
    }

    public double getSpriteU16(float p_getSpriteU16_1_)
    {
        float f = maxU - minU;
        return (p_getSpriteU16_1_ - minU) / f * 16.0F;
    }

    public double getSpriteV16(float p_getSpriteV16_1_)
    {
        float f = maxV - minV;
        return (p_getSpriteV16_1_ - minV) / f * 16.0F;
    }

    public void bindSpriteTexture()
    {
        if (glSpriteTextureId < 0)
        {
            glSpriteTextureId = TextureUtil.glGenTextures();
            TextureUtil.allocateTextureImpl(glSpriteTextureId, mipmapLevels, width, height);
            TextureUtils.applyAnisotropicLevel();
        }

        TextureUtils.bindTexture(glSpriteTextureId);
    }

    public void deleteSpriteTexture()
    {
        if (glSpriteTextureId >= 0)
        {
            TextureUtil.deleteTexture(glSpriteTextureId);
            glSpriteTextureId = -1;
        }
    }

    public float toSingleU(float p_toSingleU_1_)
    {
        p_toSingleU_1_ = p_toSingleU_1_ - baseU;
        float f = (float) sheetWidth / (float) width;
        p_toSingleU_1_ = p_toSingleU_1_ * f;
        return p_toSingleU_1_;
    }

    public float toSingleV(float p_toSingleV_1_)
    {
        p_toSingleV_1_ = p_toSingleV_1_ - baseV;
        float f = (float) sheetHeight / (float) height;
        p_toSingleV_1_ = p_toSingleV_1_ * f;
        return p_toSingleV_1_;
    }

    public List<int[][]> getFramesTextureData()
    {
        List<int[][]> list = new ArrayList();
        list.addAll(framesTextureData);
        return list;
    }

    public AnimationMetadataSection getAnimationMetadata()
    {
        return animationMetadata;
    }

    public void setAnimationMetadata(AnimationMetadataSection p_setAnimationMetadata_1_)
    {
        animationMetadata = p_setAnimationMetadata_1_;
    }

    private void loadShadersSprites()
    {
        mipmapLevels = Config.getTextureMap().getMipmapLevels();

        if (Shaders.configNormalMap)
        {
            String s = iconName + "_n";
            ResourceLocation resourcelocation = new ResourceLocation(s);
            resourcelocation = Config.getTextureMap().completeResourceLocation(resourcelocation, 0);

            if (Config.hasResource(resourcelocation))
            {
                try
                {
                    TextureAtlasSprite textureatlassprite = new TextureAtlasSprite(s);
                    textureatlassprite.isShadersSprite = true;
                    textureatlassprite.copyFrom(this);
                    textureatlassprite.loadShaderSpriteFrames(resourcelocation, mipmapLevels + 1);
                    textureatlassprite.generateMipmaps(mipmapLevels);
                    spriteNormal = textureatlassprite;
                }
                catch (IOException ioexception1)
                {
                    Config.warn("Error loading normal texture: " + s);
                    Config.warn(ioexception1.getClass().getName() + ": " + ioexception1.getMessage());
                }
            }
        }

        if (Shaders.configSpecularMap)
        {
            String s1 = iconName + "_s";
            ResourceLocation resourcelocation1 = new ResourceLocation(s1);
            resourcelocation1 = Config.getTextureMap().completeResourceLocation(resourcelocation1, 0);

            if (Config.hasResource(resourcelocation1))
            {
                try
                {
                    TextureAtlasSprite textureatlassprite1 = new TextureAtlasSprite(s1);
                    textureatlassprite1.isShadersSprite = true;
                    textureatlassprite1.copyFrom(this);
                    textureatlassprite1.loadShaderSpriteFrames(resourcelocation1, mipmapLevels + 1);
                    textureatlassprite1.generateMipmaps(mipmapLevels);
                    spriteSpecular = textureatlassprite1;
                }
                catch (IOException ioexception)
                {
                    Config.warn("Error loading specular texture: " + s1);
                    Config.warn(ioexception.getClass().getName() + ": " + ioexception.getMessage());
                }
            }
        }
    }

    public void loadShaderSpriteFrames(ResourceLocation p_loadShaderSpriteFrames_1_, int p_loadShaderSpriteFrames_2_) throws IOException
    {
        IResource iresource = Config.getResource(p_loadShaderSpriteFrames_1_);
        BufferedImage bufferedimage = TextureUtil.readBufferedImage(iresource.getInputStream());

        if (width != bufferedimage.getWidth())
        {
            bufferedimage = TextureUtils.scaleImage(bufferedimage, width);
        }

        AnimationMetadataSection animationmetadatasection = iresource.getMetadata("animation");
        int[][] aint = new int[p_loadShaderSpriteFrames_2_][];
        aint[0] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
        bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), aint[0], 0, bufferedimage.getWidth());

        if (animationmetadatasection == null)
        {
            framesTextureData.add(aint);
        }
        else
        {
            int i = bufferedimage.getHeight() / width;

            if (animationmetadatasection.getFrameCount() > 0)
            {
                Iterator iterator = animationmetadatasection.getFrameIndexSet().iterator();

                while (iterator.hasNext())
                {
                    int j = ((Integer)iterator.next()).intValue();

                    if (j >= i)
                    {
                        throw new RuntimeException("invalid frameindex " + j);
                    }

                    allocateFrameTextureData(j);
                    framesTextureData.set(j, TextureAtlasSprite.getFrameTextureData(aint, width, width, j));
                }

                animationMetadata = animationmetadatasection;
            }
            else
            {
                List<AnimationFrame> list = Lists.newArrayList();

                for (int k = 0; k < i; ++k)
                {
                    framesTextureData.add(TextureAtlasSprite.getFrameTextureData(aint, width, width, k));
                    list.add(new AnimationFrame(k, -1));
                }

                animationMetadata = new AnimationMetadataSection(list, width, height, animationmetadatasection.getFrameTime(), animationmetadatasection.isInterpolate());
            }
        }
    }
}
