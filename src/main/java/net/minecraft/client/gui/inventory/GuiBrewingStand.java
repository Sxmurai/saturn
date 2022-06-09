package net.minecraft.client.gui.inventory;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class GuiBrewingStand extends GuiContainer
{
    private static final ResourceLocation brewingStandGuiTextures = new ResourceLocation("textures/gui/container/brewing_stand.png");

    /** The player inventory bound to this GUI. */
    private final InventoryPlayer playerInventory;
    private final IInventory tileBrewingStand;

    public GuiBrewingStand(InventoryPlayer playerInv, IInventory p_i45506_2_)
    {
        super(new ContainerBrewingStand(playerInv, p_i45506_2_));
        playerInventory = playerInv;
        tileBrewingStand = p_i45506_2_;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items). Args : mouseX, mouseY
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String s = tileBrewingStand.getDisplayName().getUnformattedText();
        fontRendererObj.drawString(s, xSize / 2 - fontRendererObj.getStringWidth(s) / 2, 6, 4210752);
        fontRendererObj.drawString(playerInventory.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
    }

    /**
     * Args : renderPartialTicks, mouseX, mouseY
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(GuiBrewingStand.brewingStandGuiTextures);
        int i = (width - xSize) / 2;
        int j = (height - ySize) / 2;
        drawTexturedModalRect(i, j, 0, 0, xSize, ySize);
        int k = tileBrewingStand.getField(0);

        if (k > 0)
        {
            int l = (int)(28.0F * (1.0F - (float)k / 400.0F));

            if (l > 0)
            {
                drawTexturedModalRect(i + 97, j + 16, 176, 0, 9, l);
            }

            int i1 = k / 2 % 7;

            switch (i1)
            {
                case 0:
                    l = 29;
                    break;

                case 1:
                    l = 24;
                    break;

                case 2:
                    l = 20;
                    break;

                case 3:
                    l = 16;
                    break;

                case 4:
                    l = 11;
                    break;

                case 5:
                    l = 6;
                    break;

                case 6:
                    l = 0;
            }

            if (l > 0)
            {
                drawTexturedModalRect(i + 65, j + 14 + 29 - l, 185, 29 - l, 12, l);
            }
        }
    }
}
