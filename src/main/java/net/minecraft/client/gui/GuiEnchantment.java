package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnchantmentNameParts;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import org.lwjgl.util.glu.Project;

public class GuiEnchantment extends GuiContainer
{
    /** The ResourceLocation containing the Enchantment GUI texture location */
    private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation("textures/gui/container/enchanting_table.png");

    /**
     * The ResourceLocation containing the texture for the Book rendered above the enchantment table
     */
    private static final ResourceLocation ENCHANTMENT_TABLE_BOOK_TEXTURE = new ResourceLocation("textures/entity/enchanting_table_book.png");

    /**
     * The ModelBook instance used for rendering the book on the Enchantment table
     */
    private static final ModelBook MODEL_BOOK = new ModelBook();

    /** The player inventory currently bound to this GuiEnchantment instance. */
    private final InventoryPlayer playerInventory;

    /** A Random instance for use with the enchantment gui */
    private final Random random = new Random();
    private final ContainerEnchantment container;
    public int field_147073_u;
    public float field_147071_v;
    public float field_147069_w;
    public float field_147082_x;
    public float field_147081_y;
    public float field_147080_z;
    public float field_147076_A;
    ItemStack field_147077_B;
    private final IWorldNameable field_175380_I;

    public GuiEnchantment(InventoryPlayer inventory, World worldIn, IWorldNameable p_i45502_3_)
    {
        super(new ContainerEnchantment(inventory, worldIn));
        playerInventory = inventory;
        container = (ContainerEnchantment) inventorySlots;
        field_175380_I = p_i45502_3_;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items). Args : mouseX, mouseY
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        fontRendererObj.drawString(field_175380_I.getDisplayName().getUnformattedText(), 12, 5, 4210752);
        fontRendererObj.drawString(playerInventory.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
        func_147068_g();
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int i = (width - xSize) / 2;
        int j = (height - ySize) / 2;

        for (int k = 0; k < 3; ++k)
        {
            int l = mouseX - (i + 60);
            int i1 = mouseY - (j + 14 + 19 * k);

            if (l >= 0 && i1 >= 0 && l < 108 && i1 < 19 && container.enchantItem(mc.thePlayer, k))
            {
                mc.playerController.sendEnchantPacket(container.windowId, k);
            }
        }
    }

    /**
     * Args : renderPartialTicks, mouseX, mouseY
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(GuiEnchantment.ENCHANTMENT_TABLE_GUI_TEXTURE);
        int i = (width - xSize) / 2;
        int j = (height - ySize) / 2;
        drawTexturedModalRect(i, j, 0, 0, xSize, ySize);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        ScaledResolution scaledresolution = new ScaledResolution(mc);
        GlStateManager.viewport((scaledresolution.getScaledWidth() - 320) / 2 * scaledresolution.getScaleFactor(), (scaledresolution.getScaledHeight() - 240) / 2 * scaledresolution.getScaleFactor(), 320 * scaledresolution.getScaleFactor(), 240 * scaledresolution.getScaleFactor());
        GlStateManager.translate(-0.34F, 0.23F, 0.0F);
        Project.gluPerspective(90.0F, 1.3333334F, 9.0F, 80.0F);
        float f = 1.0F;
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.translate(0.0F, 3.3F, -16.0F);
        GlStateManager.scale(f, f, f);
        float f1 = 5.0F;
        GlStateManager.scale(f1, f1, f1);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        mc.getTextureManager().bindTexture(GuiEnchantment.ENCHANTMENT_TABLE_BOOK_TEXTURE);
        GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
        float f2 = field_147076_A + (field_147080_z - field_147076_A) * partialTicks;
        GlStateManager.translate((1.0F - f2) * 0.2F, (1.0F - f2) * 0.1F, (1.0F - f2) * 0.25F);
        GlStateManager.rotate(-(1.0F - f2) * 90.0F - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        float f3 = field_147069_w + (field_147071_v - field_147069_w) * partialTicks + 0.25F;
        float f4 = field_147069_w + (field_147071_v - field_147069_w) * partialTicks + 0.75F;
        f3 = (f3 - (float)MathHelper.truncateDoubleToInt(f3)) * 1.6F - 0.3F;
        f4 = (f4 - (float)MathHelper.truncateDoubleToInt(f4)) * 1.6F - 0.3F;

        if (f3 < 0.0F)
        {
            f3 = 0.0F;
        }

        if (f4 < 0.0F)
        {
            f4 = 0.0F;
        }

        if (f3 > 1.0F)
        {
            f3 = 1.0F;
        }

        if (f4 > 1.0F)
        {
            f4 = 1.0F;
        }

        GlStateManager.enableRescaleNormal();
        GuiEnchantment.MODEL_BOOK.render(null, 0.0F, f3, f4, f2, 0.0F, 0.0625F);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.matrixMode(5889);
        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        EnchantmentNameParts.getInstance().reseedRandomGenerator(container.xpSeed);
        int k = container.getLapisAmount();

        for (int l = 0; l < 3; ++l)
        {
            int i1 = i + 60;
            int j1 = i1 + 20;
            int k1 = 86;
            String s = EnchantmentNameParts.getInstance().generateNewRandomName();
            zLevel = 0.0F;
            mc.getTextureManager().bindTexture(GuiEnchantment.ENCHANTMENT_TABLE_GUI_TEXTURE);
            int l1 = container.enchantLevels[l];
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (l1 == 0)
            {
                drawTexturedModalRect(i1, j + 14 + 19 * l, 0, 185, 108, 19);
            }
            else
            {
                String s1 = "" + l1;
                FontRenderer fontrenderer = mc.standardGalacticFontRenderer;
                int i2 = 6839882;

                if ((k < l + 1 || mc.thePlayer.experienceLevel < l1) && !mc.thePlayer.capabilities.isCreativeMode)
                {
                    drawTexturedModalRect(i1, j + 14 + 19 * l, 0, 185, 108, 19);
                    drawTexturedModalRect(i1 + 1, j + 15 + 19 * l, 16 * l, 239, 16, 16);
                    fontrenderer.drawSplitString(s, j1, j + 16 + 19 * l, k1, (i2 & 16711422) >> 1);
                    i2 = 4226832;
                }
                else
                {
                    int j2 = mouseX - (i + 60);
                    int k2 = mouseY - (j + 14 + 19 * l);

                    if (j2 >= 0 && k2 >= 0 && j2 < 108 && k2 < 19)
                    {
                        drawTexturedModalRect(i1, j + 14 + 19 * l, 0, 204, 108, 19);
                        i2 = 16777088;
                    }
                    else
                    {
                        drawTexturedModalRect(i1, j + 14 + 19 * l, 0, 166, 108, 19);
                    }

                    drawTexturedModalRect(i1 + 1, j + 15 + 19 * l, 16 * l, 223, 16, 16);
                    fontrenderer.drawSplitString(s, j1, j + 16 + 19 * l, k1, i2);
                    i2 = 8453920;
                }

                fontrenderer = mc.fontRendererObj;
                fontrenderer.drawStringWithShadow(s1, (float)(j1 + 86 - fontrenderer.getStringWidth(s1)), (float)(j + 16 + 19 * l + 7), i2);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        boolean flag = mc.thePlayer.capabilities.isCreativeMode;
        int i = container.getLapisAmount();

        for (int j = 0; j < 3; ++j)
        {
            int k = container.enchantLevels[j];
            int l = container.field_178151_h[j];
            int i1 = j + 1;

            if (isPointInRegion(60, 14 + 19 * j, 108, 17, mouseX, mouseY) && k > 0 && l >= 0)
            {
                List<String> list = Lists.newArrayList();

                if (l >= 0 && Enchantment.getEnchantmentById(l & 255) != null)
                {
                    String s = Enchantment.getEnchantmentById(l & 255).getTranslatedName((l & 65280) >> 8);
                    list.add(EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC + I18n.format("container.enchant.clue", s));
                }

                if (!flag)
                {
                    if (l >= 0)
                    {
                        list.add("");
                    }

                    if (mc.thePlayer.experienceLevel < k)
                    {
                        list.add(EnumChatFormatting.RED + "Level Requirement: " + container.enchantLevels[j]);
                    }
                    else
                    {
                        String s1 = "";

                        if (i1 == 1)
                        {
                            s1 = I18n.format("container.enchant.lapis.one");
                        }
                        else
                        {
                            s1 = I18n.format("container.enchant.lapis.many", Integer.valueOf(i1));
                        }

                        if (i >= i1)
                        {
                            list.add(EnumChatFormatting.GRAY + "" + s1);
                        }
                        else
                        {
                            list.add(EnumChatFormatting.RED + "" + s1);
                        }

                        if (i1 == 1)
                        {
                            s1 = I18n.format("container.enchant.level.one");
                        }
                        else
                        {
                            s1 = I18n.format("container.enchant.level.many", Integer.valueOf(i1));
                        }

                        list.add(EnumChatFormatting.GRAY + "" + s1);
                    }
                }

                drawHoveringText(list, mouseX, mouseY);
                break;
            }
        }
    }

    public void func_147068_g()
    {
        ItemStack itemstack = inventorySlots.getSlot(0).getStack();

        if (!ItemStack.areItemStacksEqual(itemstack, field_147077_B))
        {
            field_147077_B = itemstack;

            while (true)
            {
                field_147082_x += (float)(random.nextInt(4) - random.nextInt(4));

                if (field_147071_v > field_147082_x + 1.0F || field_147071_v < field_147082_x - 1.0F)
                {
                    break;
                }
            }
        }

        ++field_147073_u;
        field_147069_w = field_147071_v;
        field_147076_A = field_147080_z;
        boolean flag = false;

        for (int i = 0; i < 3; ++i)
        {
            if (container.enchantLevels[i] != 0)
            {
                flag = true;
            }
        }

        if (flag)
        {
            field_147080_z += 0.2F;
        }
        else
        {
            field_147080_z -= 0.2F;
        }

        field_147080_z = MathHelper.clamp_float(field_147080_z, 0.0F, 1.0F);
        float f1 = (field_147082_x - field_147071_v) * 0.4F;
        float f = 0.2F;
        f1 = MathHelper.clamp_float(f1, -f, f);
        field_147081_y += (f1 - field_147081_y) * 0.9F;
        field_147071_v += field_147081_y;
    }
}
