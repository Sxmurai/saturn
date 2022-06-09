package net.minecraft.util;

public class Matrix4f extends org.lwjgl.util.vector.Matrix4f
{
    public Matrix4f(float[] p_i46413_1_)
    {
        m00 = p_i46413_1_[0];
        m01 = p_i46413_1_[1];
        m02 = p_i46413_1_[2];
        m03 = p_i46413_1_[3];
        m10 = p_i46413_1_[4];
        m11 = p_i46413_1_[5];
        m12 = p_i46413_1_[6];
        m13 = p_i46413_1_[7];
        m20 = p_i46413_1_[8];
        m21 = p_i46413_1_[9];
        m22 = p_i46413_1_[10];
        m23 = p_i46413_1_[11];
        m30 = p_i46413_1_[12];
        m31 = p_i46413_1_[13];
        m32 = p_i46413_1_[14];
        m33 = p_i46413_1_[15];
    }

    public Matrix4f()
    {
        m00 = m01 = m02 = m03 = m10 = m11 = m12 = m13 = m20 = m21 = m22 = m23 = m30 = m31 = m32 = m33 = 0.0F;
    }
}
