package net.minecraft.client.renderer;

public class Tessellator
{
    private final WorldRenderer worldRenderer;
    private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();

    /** The static instance of the Tessellator. */
    private static final Tessellator instance = new Tessellator(2097152);

    public static Tessellator getInstance()
    {
        return Tessellator.instance;
    }

    public Tessellator(int bufferSize)
    {
        worldRenderer = new WorldRenderer(bufferSize);
    }

    /**
     * Draws the data set up in this tessellator and resets the state to prepare for new drawing.
     */
    public void draw()
    {
        worldRenderer.finishDrawing();
        vboUploader.func_181679_a(worldRenderer);
    }

    public WorldRenderer getWorldRenderer()
    {
        return worldRenderer;
    }
}
