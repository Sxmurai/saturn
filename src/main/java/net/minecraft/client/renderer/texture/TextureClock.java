package net.minecraft.client.renderer.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import optifine.Config;
import shadersmod.client.ShadersTex;

public class TextureClock extends TextureAtlasSprite
{
    private double field_94239_h;
    private double field_94240_i;
    private static final String __OBFID = "CL_00001070";

    public TextureClock(String iconName)
    {
        super(iconName);
    }

    public void updateAnimation()
    {
        if (!framesTextureData.isEmpty())
        {
            Minecraft minecraft = Minecraft.getMinecraft();
            double d0 = 0.0D;

            if (minecraft.theWorld != null && minecraft.thePlayer != null)
            {
                d0 = minecraft.theWorld.getCelestialAngle(1.0F);

                if (!minecraft.theWorld.provider.isSurfaceWorld())
                {
                    d0 = Math.random();
                }
            }

            double d1;

            for (d1 = d0 - field_94239_h; d1 < -0.5D; ++d1)
            {
            }

            while (d1 >= 0.5D)
            {
                --d1;
            }

            d1 = MathHelper.clamp_double(d1, -1.0D, 1.0D);
            field_94240_i += d1 * 0.1D;
            field_94240_i *= 0.8D;
            field_94239_h += field_94240_i;
            int i;

            for (i = (int)((field_94239_h + 1.0D) * (double) framesTextureData.size()) % framesTextureData.size(); i < 0; i = (i + framesTextureData.size()) % framesTextureData.size())
            {
            }

            if (i != frameCounter)
            {
                frameCounter = i;

                if (Config.isShaders())
                {
                    ShadersTex.uploadTexSub((int[][]) framesTextureData.get(frameCounter), width, height, originX, originY, false, false);
                }
                else
                {
                    TextureUtil.uploadTextureMipmap((int[][]) framesTextureData.get(frameCounter), width, height, originX, originY, false, false);
                }
            }
        }
    }
}
