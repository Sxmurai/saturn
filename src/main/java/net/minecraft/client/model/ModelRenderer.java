package net.minecraft.client.model;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import optifine.ModelSprite;

import org.lwjgl.opengl.GL11;

public class ModelRenderer
{
    /** The size of the texture file's width in pixels. */
    public float textureWidth;

    /** The size of the texture file's height in pixels. */
    public float textureHeight;

    /** The X offset into the texture used for displaying this model */
    private int textureOffsetX;

    /** The Y offset into the texture used for displaying this model */
    private int textureOffsetY;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    private boolean compiled;

    /** The GL display list rendered by the Tessellator for this model */
    private int displayList;
    public boolean mirror;
    public boolean showModel;

    /** Hides the model. */
    public boolean isHidden;
    public List cubeList;
    public List childModels;
    public final String boxName;
    private final ModelBase baseModel;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    private static final String __OBFID = "CL_00000874";
    public List spriteList;
    public boolean mirrorV;
    float savedScale;

    public ModelRenderer(ModelBase model, String boxNameIn)
    {
        spriteList = new ArrayList();
        mirrorV = false;
        textureWidth = 64.0F;
        textureHeight = 32.0F;
        showModel = true;
        cubeList = Lists.newArrayList();
        baseModel = model;
        model.boxList.add(this);
        boxName = boxNameIn;
        setTextureSize(model.textureWidth, model.textureHeight);
    }

    public ModelRenderer(ModelBase model)
    {
        this(model, null);
    }

    public ModelRenderer(ModelBase model, int texOffX, int texOffY)
    {
        this(model);
        setTextureOffset(texOffX, texOffY);
    }

    /**
     * Sets the current box's rotation points and rotation angles to another box.
     */
    public void addChild(ModelRenderer renderer)
    {
        if (childModels == null)
        {
            childModels = Lists.newArrayList();
        }

        childModels.add(renderer);
    }

    public ModelRenderer setTextureOffset(int x, int y)
    {
        textureOffsetX = x;
        textureOffsetY = y;
        return this;
    }

    public ModelRenderer addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth)
    {
        partName = boxName + "." + partName;
        TextureOffset textureoffset = baseModel.getTextureOffset(partName);
        setTextureOffset(textureoffset.textureOffsetX, textureoffset.textureOffsetY);
        cubeList.add((new ModelBox(this, textureOffsetX, textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F)).setBoxName(partName));
        return this;
    }

    public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth)
    {
        cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F));
        return this;
    }

    public ModelRenderer addBox(float p_178769_1_, float p_178769_2_, float p_178769_3_, int p_178769_4_, int p_178769_5_, int p_178769_6_, boolean p_178769_7_)
    {
        cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, p_178769_1_, p_178769_2_, p_178769_3_, p_178769_4_, p_178769_5_, p_178769_6_, 0.0F, p_178769_7_));
        return this;
    }

    /**
     * Creates a textured box. Args: originX, originY, originZ, width, height, depth, scaleFactor.
     */
    public void addBox(float p_78790_1_, float p_78790_2_, float p_78790_3_, int width, int height, int depth, float scaleFactor)
    {
        cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, p_78790_1_, p_78790_2_, p_78790_3_, width, height, depth, scaleFactor));
    }

    public void setRotationPoint(float rotationPointXIn, float rotationPointYIn, float rotationPointZIn)
    {
        rotationPointX = rotationPointXIn;
        rotationPointY = rotationPointYIn;
        rotationPointZ = rotationPointZIn;
    }

    public void render(float p_78785_1_)
    {
        if (!isHidden && showModel)
        {
            if (!compiled)
            {
                compileDisplayList(p_78785_1_);
            }

            GlStateManager.translate(offsetX, offsetY, offsetZ);

            if (rotateAngleX == 0.0F && rotateAngleY == 0.0F && rotateAngleZ == 0.0F)
            {
                if (rotationPointX == 0.0F && rotationPointY == 0.0F && rotationPointZ == 0.0F)
                {
                    GlStateManager.callList(displayList);

                    if (childModels != null)
                    {
                        for (int k = 0; k < childModels.size(); ++k)
                        {
                            ((ModelRenderer) childModels.get(k)).render(p_78785_1_);
                        }
                    }
                }
                else
                {
                    GlStateManager.translate(rotationPointX * p_78785_1_, rotationPointY * p_78785_1_, rotationPointZ * p_78785_1_);
                    GlStateManager.callList(displayList);

                    if (childModels != null)
                    {
                        for (int j = 0; j < childModels.size(); ++j)
                        {
                            ((ModelRenderer) childModels.get(j)).render(p_78785_1_);
                        }
                    }

                    GlStateManager.translate(-rotationPointX * p_78785_1_, -rotationPointY * p_78785_1_, -rotationPointZ * p_78785_1_);
                }
            }
            else
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(rotationPointX * p_78785_1_, rotationPointY * p_78785_1_, rotationPointZ * p_78785_1_);

                if (rotateAngleZ != 0.0F)
                {
                    GlStateManager.rotate(rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (rotateAngleY != 0.0F)
                {
                    GlStateManager.rotate(rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (rotateAngleX != 0.0F)
                {
                    GlStateManager.rotate(rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                }

                GlStateManager.callList(displayList);

                if (childModels != null)
                {
                    for (int i = 0; i < childModels.size(); ++i)
                    {
                        ((ModelRenderer) childModels.get(i)).render(p_78785_1_);
                    }
                }

                GlStateManager.popMatrix();
            }

            GlStateManager.translate(-offsetX, -offsetY, -offsetZ);
        }
    }

    public void renderWithRotation(float p_78791_1_)
    {
        if (!isHidden && showModel)
        {
            if (!compiled)
            {
                compileDisplayList(p_78791_1_);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(rotationPointX * p_78791_1_, rotationPointY * p_78791_1_, rotationPointZ * p_78791_1_);

            if (rotateAngleY != 0.0F)
            {
                GlStateManager.rotate(rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
            }

            if (rotateAngleX != 0.0F)
            {
                GlStateManager.rotate(rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
            }

            if (rotateAngleZ != 0.0F)
            {
                GlStateManager.rotate(rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
            }

            GlStateManager.callList(displayList);
            GlStateManager.popMatrix();
        }
    }

    /**
     * Allows the changing of Angles after a box has been rendered
     */
    public void postRender(float scale)
    {
        if (!isHidden && showModel)
        {
            if (!compiled)
            {
                compileDisplayList(scale);
            }

            if (rotateAngleX == 0.0F && rotateAngleY == 0.0F && rotateAngleZ == 0.0F)
            {
                if (rotationPointX != 0.0F || rotationPointY != 0.0F || rotationPointZ != 0.0F)
                {
                    GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
                }
            }
            else
            {
                GlStateManager.translate(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

                if (rotateAngleZ != 0.0F)
                {
                    GlStateManager.rotate(rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (rotateAngleY != 0.0F)
                {
                    GlStateManager.rotate(rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (rotateAngleX != 0.0F)
                {
                    GlStateManager.rotate(rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                }
            }
        }
    }

    /**
     * Compiles a GL display list for this model
     */
    private void compileDisplayList(float scale)
    {
        if (displayList == 0)
        {
            savedScale = scale;
            displayList = GLAllocation.generateDisplayLists(1);
        }

        GL11.glNewList(displayList, GL11.GL_COMPILE);
        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();

        for (int i = 0; i < cubeList.size(); ++i)
        {
            ((ModelBox) cubeList.get(i)).render(worldrenderer, scale);
        }

        for (int j = 0; j < spriteList.size(); ++j)
        {
            ModelSprite modelsprite = (ModelSprite) spriteList.get(j);
            modelsprite.render(Tessellator.getInstance(), scale);
        }

        GL11.glEndList();
        compiled = true;
    }

    /**
     * Returns the model renderer with the new texture parameters.
     */
    public ModelRenderer setTextureSize(int textureWidthIn, int textureHeightIn)
    {
        textureWidth = (float)textureWidthIn;
        textureHeight = (float)textureHeightIn;
        return this;
    }

    public void addSprite(float p_addSprite_1_, float p_addSprite_2_, float p_addSprite_3_, int p_addSprite_4_, int p_addSprite_5_, int p_addSprite_6_, float p_addSprite_7_)
    {
        spriteList.add(new ModelSprite(this, textureOffsetX, textureOffsetY, p_addSprite_1_, p_addSprite_2_, p_addSprite_3_, p_addSprite_4_, p_addSprite_5_, p_addSprite_6_, p_addSprite_7_));
    }

    public boolean getCompiled()
    {
        return compiled;
    }

    public int getDisplayList()
    {
        return displayList;
    }

    public void resetDisplayList()
    {
        if (compiled)
        {
            compiled = false;
            compileDisplayList(savedScale);
        }
    }
}
