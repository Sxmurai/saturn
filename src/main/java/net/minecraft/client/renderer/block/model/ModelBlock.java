package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModelBlock
{
    private static final Logger LOGGER = LogManager.getLogger();
    static final Gson SERIALIZER = (new GsonBuilder()).registerTypeAdapter(ModelBlock.class, new ModelBlock.Deserializer()).registerTypeAdapter(BlockPart.class, new BlockPart.Deserializer()).registerTypeAdapter(BlockPartFace.class, new BlockPartFace.Deserializer()).registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer()).registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3f.Deserializer()).registerTypeAdapter(ItemCameraTransforms.class, new ItemCameraTransforms.Deserializer()).create();
    private final List<BlockPart> elements;
    private final boolean gui3d;
    private final boolean ambientOcclusion;
    private final ItemCameraTransforms cameraTransforms;
    public String name;
    protected final Map<String, String> textures;
    protected ModelBlock parent;
    protected ResourceLocation parentLocation;

    public static ModelBlock deserialize(Reader p_178307_0_)
    {
        return ModelBlock.SERIALIZER.fromJson(p_178307_0_, ModelBlock.class);
    }

    public static ModelBlock deserialize(String p_178294_0_)
    {
        return ModelBlock.deserialize(new StringReader(p_178294_0_));
    }

    protected ModelBlock(List<BlockPart> p_i46225_1_, Map<String, String> p_i46225_2_, boolean p_i46225_3_, boolean p_i46225_4_, ItemCameraTransforms p_i46225_5_)
    {
        this(null, p_i46225_1_, p_i46225_2_, p_i46225_3_, p_i46225_4_, p_i46225_5_);
    }

    protected ModelBlock(ResourceLocation p_i46226_1_, Map<String, String> p_i46226_2_, boolean p_i46226_3_, boolean p_i46226_4_, ItemCameraTransforms p_i46226_5_)
    {
        this(p_i46226_1_, Collections.emptyList(), p_i46226_2_, p_i46226_3_, p_i46226_4_, p_i46226_5_);
    }

    private ModelBlock(ResourceLocation parentLocationIn, List<BlockPart> elementsIn, Map<String, String> texturesIn, boolean ambientOcclusionIn, boolean gui3dIn, ItemCameraTransforms cameraTransformsIn)
    {
        name = "";
        elements = elementsIn;
        ambientOcclusion = ambientOcclusionIn;
        gui3d = gui3dIn;
        textures = texturesIn;
        parentLocation = parentLocationIn;
        cameraTransforms = cameraTransformsIn;
    }

    public List<BlockPart> getElements()
    {
        return hasParent() ? parent.getElements() : elements;
    }

    private boolean hasParent()
    {
        return parent != null;
    }

    public boolean isAmbientOcclusion()
    {
        return hasParent() ? parent.isAmbientOcclusion() : ambientOcclusion;
    }

    public boolean isGui3d()
    {
        return gui3d;
    }

    public boolean isResolved()
    {
        return parentLocation == null || parent != null && parent.isResolved();
    }

    public void getParentFromMap(Map<ResourceLocation, ModelBlock> p_178299_1_)
    {
        if (parentLocation != null)
        {
            parent = p_178299_1_.get(parentLocation);
        }
    }

    public boolean isTexturePresent(String textureName)
    {
        return !"missingno".equals(resolveTextureName(textureName));
    }

    public String resolveTextureName(String textureName)
    {
        if (!startsWithHash(textureName))
        {
            textureName = '#' + textureName;
        }

        return resolveTextureName(textureName, new ModelBlock.Bookkeep(this));
    }

    private String resolveTextureName(String textureName, ModelBlock.Bookkeep p_178302_2_)
    {
        if (startsWithHash(textureName))
        {
            if (this == p_178302_2_.modelExt)
            {
                ModelBlock.LOGGER.warn("Unable to resolve texture due to upward reference: " + textureName + " in " + name);
                return "missingno";
            }
            else
            {
                String s = textures.get(textureName.substring(1));

                if (s == null && hasParent())
                {
                    s = parent.resolveTextureName(textureName, p_178302_2_);
                }

                p_178302_2_.modelExt = this;

                if (s != null && startsWithHash(s))
                {
                    s = p_178302_2_.model.resolveTextureName(s, p_178302_2_);
                }

                return s != null && !startsWithHash(s) ? s : "missingno";
            }
        }
        else
        {
            return textureName;
        }
    }

    private boolean startsWithHash(String hash)
    {
        return hash.charAt(0) == 35;
    }

    public ResourceLocation getParentLocation()
    {
        return parentLocation;
    }

    public ModelBlock getRootModel()
    {
        return hasParent() ? parent.getRootModel() : this;
    }

    public ItemCameraTransforms func_181682_g()
    {
        ItemTransformVec3f itemtransformvec3f = func_181681_a(ItemCameraTransforms.TransformType.THIRD_PERSON);
        ItemTransformVec3f itemtransformvec3f1 = func_181681_a(ItemCameraTransforms.TransformType.FIRST_PERSON);
        ItemTransformVec3f itemtransformvec3f2 = func_181681_a(ItemCameraTransforms.TransformType.HEAD);
        ItemTransformVec3f itemtransformvec3f3 = func_181681_a(ItemCameraTransforms.TransformType.GUI);
        ItemTransformVec3f itemtransformvec3f4 = func_181681_a(ItemCameraTransforms.TransformType.GROUND);
        ItemTransformVec3f itemtransformvec3f5 = func_181681_a(ItemCameraTransforms.TransformType.FIXED);
        return new ItemCameraTransforms(itemtransformvec3f, itemtransformvec3f1, itemtransformvec3f2, itemtransformvec3f3, itemtransformvec3f4, itemtransformvec3f5);
    }

    private ItemTransformVec3f func_181681_a(ItemCameraTransforms.TransformType p_181681_1_)
    {
        return parent != null && !cameraTransforms.func_181687_c(p_181681_1_) ? parent.func_181681_a(p_181681_1_) : cameraTransforms.getTransform(p_181681_1_);
    }

    public static void checkModelHierarchy(Map<ResourceLocation, ModelBlock> p_178312_0_)
    {
        for (ModelBlock modelblock : p_178312_0_.values())
        {
            try
            {
                ModelBlock modelblock1 = modelblock.parent;

                for (ModelBlock modelblock2 = modelblock1.parent; modelblock1 != modelblock2; modelblock2 = modelblock2.parent.parent)
                {
                    modelblock1 = modelblock1.parent;
                }

                throw new ModelBlock.LoopException();
            }
            catch (NullPointerException var5)
            {
            }
        }
    }

    static final class Bookkeep
    {
        public final ModelBlock model;
        public ModelBlock modelExt;

        private Bookkeep(ModelBlock p_i46223_1_)
        {
            model = p_i46223_1_;
        }
    }

    public static class Deserializer implements JsonDeserializer<ModelBlock>
    {
        public ModelBlock deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException
        {
            JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
            List<BlockPart> list = getModelElements(p_deserialize_3_, jsonobject);
            String s = getParent(jsonobject);
            boolean flag = StringUtils.isEmpty(s);
            boolean flag1 = list.isEmpty();

            if (flag1 && flag)
            {
                throw new JsonParseException("BlockModel requires either elements or parent, found neither");
            }
            else if (!flag && !flag1)
            {
                throw new JsonParseException("BlockModel requires either elements or parent, found both");
            }
            else
            {
                Map<String, String> map = getTextures(jsonobject);
                boolean flag2 = getAmbientOcclusionEnabled(jsonobject);
                ItemCameraTransforms itemcameratransforms = ItemCameraTransforms.DEFAULT;

                if (jsonobject.has("display"))
                {
                    JsonObject jsonobject1 = JsonUtils.getJsonObject(jsonobject, "display");
                    itemcameratransforms = p_deserialize_3_.deserialize(jsonobject1, ItemCameraTransforms.class);
                }

                return flag1 ? new ModelBlock(new ResourceLocation(s), map, flag2, true, itemcameratransforms) : new ModelBlock(list, map, flag2, true, itemcameratransforms);
            }
        }

        private Map<String, String> getTextures(JsonObject p_178329_1_)
        {
            Map<String, String> map = Maps.newHashMap();

            if (p_178329_1_.has("textures"))
            {
                JsonObject jsonobject = p_178329_1_.getAsJsonObject("textures");

                for (Map.Entry<String, JsonElement> entry : jsonobject.entrySet())
                {
                    map.put(entry.getKey(), entry.getValue().getAsString());
                }
            }

            return map;
        }

        private String getParent(JsonObject p_178326_1_)
        {
            return JsonUtils.getString(p_178326_1_, "parent", "");
        }

        protected boolean getAmbientOcclusionEnabled(JsonObject p_178328_1_)
        {
            return JsonUtils.getBoolean(p_178328_1_, "ambientocclusion", true);
        }

        protected List<BlockPart> getModelElements(JsonDeserializationContext p_178325_1_, JsonObject p_178325_2_)
        {
            List<BlockPart> list = Lists.newArrayList();

            if (p_178325_2_.has("elements"))
            {
                for (JsonElement jsonelement : JsonUtils.getJsonArray(p_178325_2_, "elements"))
                {
                    list.add(p_178325_1_.deserialize(jsonelement, BlockPart.class));
                }
            }

            return list;
        }
    }

    public static class LoopException extends RuntimeException
    {
    }
}
