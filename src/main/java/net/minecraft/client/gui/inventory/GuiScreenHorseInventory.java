package net.minecraft.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class GuiScreenHorseInventory extends GuiContainer
{
    private static final ResourceLocation horseGuiTextures = new ResourceLocation("textures/gui/container/horse.png");

    /** The player inventory bound to this GUI. */
    private final IInventory playerInventory;

    /** The horse inventory bound to this GUI. */
    private final IInventory horseInventory;

    /** The EntityHorse whose inventory is currently being accessed. */
    private final EntityHorse horseEntity;

    /** The mouse x-position recorded during the last rendered frame. */
    private float mousePosx;

    /** The mouse y-position recorded during the last renderered frame. */
    private float mousePosY;

    public GuiScreenHorseInventory(IInventory playerInv, IInventory horseInv, EntityHorse horse)
    {
        super(new ContainerHorseInventory(playerInv, horseInv, horse, Minecraft.getMinecraft().thePlayer));
        playerInventory = playerInv;
        horseInventory = horseInv;
        horseEntity = horse;
        allowUserInput = false;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items). Args : mouseX, mouseY
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        fontRendererObj.drawString(horseInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
        fontRendererObj.drawString(playerInventory.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
    }

    /**
     * Args : renderPartialTicks, mouseX, mouseY
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(GuiScreenHorseInventory.horseGuiTextures);
        int i = (width - xSize) / 2;
        int j = (height - ySize) / 2;
        drawTexturedModalRect(i, j, 0, 0, xSize, ySize);

        if (horseEntity.isChested())
        {
            drawTexturedModalRect(i + 79, j + 17, 0, ySize, 90, 54);
        }

        if (horseEntity.canWearArmor())
        {
            drawTexturedModalRect(i + 7, j + 35, 0, ySize + 54, 18, 18);
        }

        GuiInventory.drawEntityOnScreen(i + 51, j + 60, 17, (float)(i + 51) - mousePosx, (float)(j + 75 - 50) - mousePosY, horseEntity);
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        mousePosx = (float)mouseX;
        mousePosY = (float)mouseY;
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
