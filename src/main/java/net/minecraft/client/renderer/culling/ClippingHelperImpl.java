package net.minecraft.client.renderer.culling;

import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

public class ClippingHelperImpl extends ClippingHelper
{
    private static final ClippingHelperImpl instance = new ClippingHelperImpl();
    private final FloatBuffer projectionMatrixBuffer = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer modelviewMatrixBuffer = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer field_78564_h = GLAllocation.createDirectFloatBuffer(16);

    /**
     * Initialises the ClippingHelper object then returns an instance of it.
     */
    public static ClippingHelper getInstance()
    {
        ClippingHelperImpl.instance.init();
        return ClippingHelperImpl.instance;
    }

    private void normalize(float[] p_180547_1_)
    {
        float f = MathHelper.sqrt_float(p_180547_1_[0] * p_180547_1_[0] + p_180547_1_[1] * p_180547_1_[1] + p_180547_1_[2] * p_180547_1_[2]);
        p_180547_1_[0] /= f;
        p_180547_1_[1] /= f;
        p_180547_1_[2] /= f;
        p_180547_1_[3] /= f;
    }

    public void init()
    {
        projectionMatrixBuffer.clear();
        modelviewMatrixBuffer.clear();
        field_78564_h.clear();
        GlStateManager.getFloat(2983, projectionMatrixBuffer);
        GlStateManager.getFloat(2982, modelviewMatrixBuffer);
        float[] afloat = projectionMatrix;
        float[] afloat1 = modelviewMatrix;
        projectionMatrixBuffer.flip().limit(16);
        projectionMatrixBuffer.get(afloat);
        modelviewMatrixBuffer.flip().limit(16);
        modelviewMatrixBuffer.get(afloat1);
        clippingMatrix[0] = afloat1[0] * afloat[0] + afloat1[1] * afloat[4] + afloat1[2] * afloat[8] + afloat1[3] * afloat[12];
        clippingMatrix[1] = afloat1[0] * afloat[1] + afloat1[1] * afloat[5] + afloat1[2] * afloat[9] + afloat1[3] * afloat[13];
        clippingMatrix[2] = afloat1[0] * afloat[2] + afloat1[1] * afloat[6] + afloat1[2] * afloat[10] + afloat1[3] * afloat[14];
        clippingMatrix[3] = afloat1[0] * afloat[3] + afloat1[1] * afloat[7] + afloat1[2] * afloat[11] + afloat1[3] * afloat[15];
        clippingMatrix[4] = afloat1[4] * afloat[0] + afloat1[5] * afloat[4] + afloat1[6] * afloat[8] + afloat1[7] * afloat[12];
        clippingMatrix[5] = afloat1[4] * afloat[1] + afloat1[5] * afloat[5] + afloat1[6] * afloat[9] + afloat1[7] * afloat[13];
        clippingMatrix[6] = afloat1[4] * afloat[2] + afloat1[5] * afloat[6] + afloat1[6] * afloat[10] + afloat1[7] * afloat[14];
        clippingMatrix[7] = afloat1[4] * afloat[3] + afloat1[5] * afloat[7] + afloat1[6] * afloat[11] + afloat1[7] * afloat[15];
        clippingMatrix[8] = afloat1[8] * afloat[0] + afloat1[9] * afloat[4] + afloat1[10] * afloat[8] + afloat1[11] * afloat[12];
        clippingMatrix[9] = afloat1[8] * afloat[1] + afloat1[9] * afloat[5] + afloat1[10] * afloat[9] + afloat1[11] * afloat[13];
        clippingMatrix[10] = afloat1[8] * afloat[2] + afloat1[9] * afloat[6] + afloat1[10] * afloat[10] + afloat1[11] * afloat[14];
        clippingMatrix[11] = afloat1[8] * afloat[3] + afloat1[9] * afloat[7] + afloat1[10] * afloat[11] + afloat1[11] * afloat[15];
        clippingMatrix[12] = afloat1[12] * afloat[0] + afloat1[13] * afloat[4] + afloat1[14] * afloat[8] + afloat1[15] * afloat[12];
        clippingMatrix[13] = afloat1[12] * afloat[1] + afloat1[13] * afloat[5] + afloat1[14] * afloat[9] + afloat1[15] * afloat[13];
        clippingMatrix[14] = afloat1[12] * afloat[2] + afloat1[13] * afloat[6] + afloat1[14] * afloat[10] + afloat1[15] * afloat[14];
        clippingMatrix[15] = afloat1[12] * afloat[3] + afloat1[13] * afloat[7] + afloat1[14] * afloat[11] + afloat1[15] * afloat[15];
        float[] afloat2 = frustum[0];
        afloat2[0] = clippingMatrix[3] - clippingMatrix[0];
        afloat2[1] = clippingMatrix[7] - clippingMatrix[4];
        afloat2[2] = clippingMatrix[11] - clippingMatrix[8];
        afloat2[3] = clippingMatrix[15] - clippingMatrix[12];
        normalize(afloat2);
        float[] afloat3 = frustum[1];
        afloat3[0] = clippingMatrix[3] + clippingMatrix[0];
        afloat3[1] = clippingMatrix[7] + clippingMatrix[4];
        afloat3[2] = clippingMatrix[11] + clippingMatrix[8];
        afloat3[3] = clippingMatrix[15] + clippingMatrix[12];
        normalize(afloat3);
        float[] afloat4 = frustum[2];
        afloat4[0] = clippingMatrix[3] + clippingMatrix[1];
        afloat4[1] = clippingMatrix[7] + clippingMatrix[5];
        afloat4[2] = clippingMatrix[11] + clippingMatrix[9];
        afloat4[3] = clippingMatrix[15] + clippingMatrix[13];
        normalize(afloat4);
        float[] afloat5 = frustum[3];
        afloat5[0] = clippingMatrix[3] - clippingMatrix[1];
        afloat5[1] = clippingMatrix[7] - clippingMatrix[5];
        afloat5[2] = clippingMatrix[11] - clippingMatrix[9];
        afloat5[3] = clippingMatrix[15] - clippingMatrix[13];
        normalize(afloat5);
        float[] afloat6 = frustum[4];
        afloat6[0] = clippingMatrix[3] - clippingMatrix[2];
        afloat6[1] = clippingMatrix[7] - clippingMatrix[6];
        afloat6[2] = clippingMatrix[11] - clippingMatrix[10];
        afloat6[3] = clippingMatrix[15] - clippingMatrix[14];
        normalize(afloat6);
        float[] afloat7 = frustum[5];
        afloat7[0] = clippingMatrix[3] + clippingMatrix[2];
        afloat7[1] = clippingMatrix[7] + clippingMatrix[6];
        afloat7[2] = clippingMatrix[11] + clippingMatrix[10];
        afloat7[3] = clippingMatrix[15] + clippingMatrix[14];
        normalize(afloat7);
    }
}
