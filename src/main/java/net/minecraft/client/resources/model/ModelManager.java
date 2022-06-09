package net.minecraft.client.resources.model;

import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.IRegistry;

public class ModelManager implements IResourceManagerReloadListener
{
    private IRegistry<ModelResourceLocation, IBakedModel> modelRegistry;
    private final TextureMap texMap;
    private final BlockModelShapes modelProvider;
    private IBakedModel defaultModel;

    public ModelManager(TextureMap textures)
    {
        texMap = textures;
        modelProvider = new BlockModelShapes(this);
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        ModelBakery modelbakery = new ModelBakery(resourceManager, texMap, modelProvider);
        modelRegistry = modelbakery.setupModelRegistry();
        defaultModel = modelRegistry.getObject(ModelBakery.MODEL_MISSING);
        modelProvider.reloadModels();
    }

    public IBakedModel getModel(ModelResourceLocation modelLocation)
    {
        if (modelLocation == null)
        {
            return defaultModel;
        }
        else
        {
            IBakedModel ibakedmodel = modelRegistry.getObject(modelLocation);
            return ibakedmodel == null ? defaultModel : ibakedmodel;
        }
    }

    public IBakedModel getMissingModel()
    {
        return defaultModel;
    }

    public TextureMap getTextureMap()
    {
        return texMap;
    }

    public BlockModelShapes getBlockModelShapes()
    {
        return modelProvider;
    }
}
