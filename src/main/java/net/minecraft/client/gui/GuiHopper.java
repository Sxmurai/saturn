package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class GuiHopper extends GuiContainer
{
    /** The ResourceLocation containing the gui texture for the hopper */
    private static final ResourceLocation HOPPER_GUI_TEXTURE = new ResourceLocation("textures/gui/container/hopper.png");

    /** The player inventory currently bound to this GUI instance */
    private final IInventory playerInventory;

    /** The hopper inventory bound to this GUI instance */
    private final IInventory hopperInventory;

    public GuiHopper(InventoryPlayer playerInv, IInventory hopperInv)
    {
        super(new ContainerHopper(playerInv, hopperInv, Minecraft.getMinecraft().thePlayer));
        playerInventory = playerInv;
        hopperInventory = hopperInv;
        allowUserInput = false;
        ySize = 133;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items). Args : mouseX, mouseY
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        fontRendererObj.drawString(hopperInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
        fontRendererObj.drawString(playerInventory.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
    }

    /**
     * Args : renderPartialTicks, mouseX, mouseY
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(GuiHopper.HOPPER_GUI_TEXTURE);
        int i = (width - xSize) / 2;
        int j = (height - ySize) / 2;
        drawTexturedModalRect(i, j, 0, 0, xSize, ySize);
    }
}
