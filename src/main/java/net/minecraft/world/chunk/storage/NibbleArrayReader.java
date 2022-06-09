package net.minecraft.world.chunk.storage;

public class NibbleArrayReader
{
    public final byte[] data;
    private final int depthBits;
    private final int depthBitsPlusFour;

    public NibbleArrayReader(byte[] dataIn, int depthBitsIn)
    {
        data = dataIn;
        depthBits = depthBitsIn;
        depthBitsPlusFour = depthBitsIn + 4;
    }

    public int get(int p_76686_1_, int p_76686_2_, int p_76686_3_)
    {
        int i = p_76686_1_ << depthBitsPlusFour | p_76686_3_ << depthBits | p_76686_2_;
        int j = i >> 1;
        int k = i & 1;
        return k == 0 ? data[j] & 15 : data[j] >> 4 & 15;
    }
}
