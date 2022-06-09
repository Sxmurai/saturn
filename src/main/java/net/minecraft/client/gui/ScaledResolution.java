package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class ScaledResolution
{
    private final double scaledWidthD;
    private final double scaledHeightD;
    private int scaledWidth;
    private int scaledHeight;
    private int scaleFactor;

    public ScaledResolution(Minecraft p_i46445_1_)
    {
        scaledWidth = p_i46445_1_.displayWidth;
        scaledHeight = p_i46445_1_.displayHeight;
        scaleFactor = 1;
        boolean flag = p_i46445_1_.isUnicode();
        int i = p_i46445_1_.gameSettings.guiScale;

        if (i == 0)
        {
            i = 1000;
        }

        while (scaleFactor < i && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240)
        {
            ++scaleFactor;
        }

        if (flag && scaleFactor % 2 != 0 && scaleFactor != 1)
        {
            --scaleFactor;
        }

        scaledWidthD = (double) scaledWidth / (double) scaleFactor;
        scaledHeightD = (double) scaledHeight / (double) scaleFactor;
        scaledWidth = MathHelper.ceiling_double_int(scaledWidthD);
        scaledHeight = MathHelper.ceiling_double_int(scaledHeightD);
    }

    public int getScaledWidth()
    {
        return scaledWidth;
    }

    public int getScaledHeight()
    {
        return scaledHeight;
    }

    public double getScaledWidth_double()
    {
        return scaledWidthD;
    }

    public double getScaledHeight_double()
    {
        return scaledHeightD;
    }

    public int getScaleFactor()
    {
        return scaleFactor;
    }
}
