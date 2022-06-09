package net.minecraft.client.renderer;

import net.minecraft.util.EnumWorldBlockLayer;

public class RegionRenderCacheBuilder
{
    private final WorldRenderer[] worldRenderers = new WorldRenderer[EnumWorldBlockLayer.values().length];

    public RegionRenderCacheBuilder()
    {
        worldRenderers[EnumWorldBlockLayer.SOLID.ordinal()] = new WorldRenderer(2097152);
        worldRenderers[EnumWorldBlockLayer.CUTOUT.ordinal()] = new WorldRenderer(131072);
        worldRenderers[EnumWorldBlockLayer.CUTOUT_MIPPED.ordinal()] = new WorldRenderer(131072);
        worldRenderers[EnumWorldBlockLayer.TRANSLUCENT.ordinal()] = new WorldRenderer(262144);
    }

    public WorldRenderer getWorldRendererByLayer(EnumWorldBlockLayer layer)
    {
        return worldRenderers[layer.ordinal()];
    }

    public WorldRenderer getWorldRendererByLayerId(int id)
    {
        return worldRenderers[id];
    }
}
