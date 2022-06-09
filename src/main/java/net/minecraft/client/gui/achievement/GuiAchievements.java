package net.minecraft.client.gui.achievement;

import java.io.IOException;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

public class GuiAchievements extends GuiScreen implements IProgressMeter
{
    private static final int field_146572_y = AchievementList.minDisplayColumn * 24 - 112;
    private static final int field_146571_z = AchievementList.minDisplayRow * 24 - 112;
    private static final int field_146559_A = AchievementList.maxDisplayColumn * 24 - 77;
    private static final int field_146560_B = AchievementList.maxDisplayRow * 24 - 77;
    private static final ResourceLocation ACHIEVEMENT_BACKGROUND = new ResourceLocation("textures/gui/achievement/achievement_background.png");
    protected GuiScreen parentScreen;
    protected int field_146555_f = 256;
    protected int field_146557_g = 202;
    protected int field_146563_h;
    protected int field_146564_i;
    protected float field_146570_r = 1.0F;
    protected double field_146569_s;
    protected double field_146568_t;
    protected double field_146567_u;
    protected double field_146566_v;
    protected double field_146565_w;
    protected double field_146573_x;
    private int field_146554_D;
    private final StatFileWriter statFileWriter;
    private boolean loadingAchievements = true;

    public GuiAchievements(GuiScreen parentScreenIn, StatFileWriter statFileWriterIn)
    {
        parentScreen = parentScreenIn;
        statFileWriter = statFileWriterIn;
        int i = 141;
        int j = 141;
        field_146569_s = field_146567_u = field_146565_w = AchievementList.openInventory.displayColumn * 24 - i / 2 - 12;
        field_146568_t = field_146566_v = field_146573_x = AchievementList.openInventory.displayRow * 24 - j / 2;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS));
        buttonList.clear();
        buttonList.add(new GuiOptionButton(1, width / 2 + 24, height / 2 + 74, 80, 20, I18n.format("gui.done")));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (!loadingAchievements)
        {
            if (button.id == 1)
            {
                mc.displayGuiScreen(parentScreen);
            }
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == mc.gameSettings.keyBindInventory.getKeyCode())
        {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
        else
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (loadingAchievements)
        {
            drawDefaultBackground();
            drawCenteredString(fontRendererObj, I18n.format("multiplayer.downloadingStats"), width / 2, height / 2, 16777215);
            drawCenteredString(fontRendererObj, IProgressMeter.lanSearchStates[(int)(Minecraft.getSystemTime() / 150L % (long) IProgressMeter.lanSearchStates.length)], width / 2, height / 2 + fontRendererObj.FONT_HEIGHT * 2, 16777215);
        }
        else
        {
            if (Mouse.isButtonDown(0))
            {
                int i = (width - field_146555_f) / 2;
                int j = (height - field_146557_g) / 2;
                int k = i + 8;
                int l = j + 17;

                if ((field_146554_D == 0 || field_146554_D == 1) && mouseX >= k && mouseX < k + 224 && mouseY >= l && mouseY < l + 155)
                {
                    if (field_146554_D == 0)
                    {
                        field_146554_D = 1;
                    }
                    else
                    {
                        field_146567_u -= (float)(mouseX - field_146563_h) * field_146570_r;
                        field_146566_v -= (float)(mouseY - field_146564_i) * field_146570_r;
                        field_146565_w = field_146569_s = field_146567_u;
                        field_146573_x = field_146568_t = field_146566_v;
                    }

                    field_146563_h = mouseX;
                    field_146564_i = mouseY;
                }
            }
            else
            {
                field_146554_D = 0;
            }

            int i1 = Mouse.getDWheel();
            float f3 = field_146570_r;

            if (i1 < 0)
            {
                field_146570_r += 0.25F;
            }
            else if (i1 > 0)
            {
                field_146570_r -= 0.25F;
            }

            field_146570_r = MathHelper.clamp_float(field_146570_r, 1.0F, 2.0F);

            if (field_146570_r != f3)
            {
                float f5 = f3 - field_146570_r;
                float f4 = f3 * (float) field_146555_f;
                float f = f3 * (float) field_146557_g;
                float f1 = field_146570_r * (float) field_146555_f;
                float f2 = field_146570_r * (float) field_146557_g;
                field_146567_u -= (f1 - f4) * 0.5F;
                field_146566_v -= (f2 - f) * 0.5F;
                field_146565_w = field_146569_s = field_146567_u;
                field_146573_x = field_146568_t = field_146566_v;
            }

            if (field_146565_w < (double) GuiAchievements.field_146572_y)
            {
                field_146565_w = GuiAchievements.field_146572_y;
            }

            if (field_146573_x < (double) GuiAchievements.field_146571_z)
            {
                field_146573_x = GuiAchievements.field_146571_z;
            }

            if (field_146565_w >= (double) GuiAchievements.field_146559_A)
            {
                field_146565_w = GuiAchievements.field_146559_A - 1;
            }

            if (field_146573_x >= (double) GuiAchievements.field_146560_B)
            {
                field_146573_x = GuiAchievements.field_146560_B - 1;
            }

            drawDefaultBackground();
            drawAchievementScreen(mouseX, mouseY, partialTicks);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            drawTitle();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    public void doneLoading()
    {
        if (loadingAchievements)
        {
            loadingAchievements = false;
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        if (!loadingAchievements)
        {
            field_146569_s = field_146567_u;
            field_146568_t = field_146566_v;
            double d0 = field_146565_w - field_146567_u;
            double d1 = field_146573_x - field_146566_v;

            if (d0 * d0 + d1 * d1 < 4.0D)
            {
                field_146567_u += d0;
                field_146566_v += d1;
            }
            else
            {
                field_146567_u += d0 * 0.85D;
                field_146566_v += d1 * 0.85D;
            }
        }
    }

    protected void drawTitle()
    {
        int i = (width - field_146555_f) / 2;
        int j = (height - field_146557_g) / 2;
        fontRendererObj.drawString(I18n.format("gui.achievements"), i + 15, j + 5, 4210752);
    }

    protected void drawAchievementScreen(int p_146552_1_, int p_146552_2_, float p_146552_3_)
    {
        int i = MathHelper.floor_double(field_146569_s + (field_146567_u - field_146569_s) * (double)p_146552_3_);
        int j = MathHelper.floor_double(field_146568_t + (field_146566_v - field_146568_t) * (double)p_146552_3_);

        if (i < GuiAchievements.field_146572_y)
        {
            i = GuiAchievements.field_146572_y;
        }

        if (j < GuiAchievements.field_146571_z)
        {
            j = GuiAchievements.field_146571_z;
        }

        if (i >= GuiAchievements.field_146559_A)
        {
            i = GuiAchievements.field_146559_A - 1;
        }

        if (j >= GuiAchievements.field_146560_B)
        {
            j = GuiAchievements.field_146560_B - 1;
        }

        int k = (width - field_146555_f) / 2;
        int l = (height - field_146557_g) / 2;
        int i1 = k + 16;
        int j1 = l + 17;
        zLevel = 0.0F;
        GlStateManager.depthFunc(518);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)i1, (float)j1, -200.0F);
        GlStateManager.scale(1.0F / field_146570_r, 1.0F / field_146570_r, 0.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        int k1 = i + 288 >> 4;
        int l1 = j + 288 >> 4;
        int i2 = (i + 288) % 16;
        int j2 = (j + 288) % 16;
        int k2 = 4;
        int l2 = 8;
        int i3 = 10;
        int j3 = 22;
        int k3 = 37;
        Random random = new Random();
        float f = 16.0F / field_146570_r;
        float f1 = 16.0F / field_146570_r;

        for (int l3 = 0; (float)l3 * f - (float)j2 < 155.0F; ++l3)
        {
            float f2 = 0.6F - (float)(l1 + l3) / 25.0F * 0.3F;
            GlStateManager.color(f2, f2, f2, 1.0F);

            for (int i4 = 0; (float)i4 * f1 - (float)i2 < 224.0F; ++i4)
            {
                random.setSeed(mc.getSession().getPlayerID().hashCode() + k1 + i4 + (l1 + l3) * 16);
                int j4 = random.nextInt(1 + l1 + l3) + (l1 + l3) / 2;
                TextureAtlasSprite textureatlassprite = func_175371_a(Blocks.sand);

                if (j4 <= 37 && l1 + l3 != 35)
                {
                    if (j4 == 22)
                    {
                        if (random.nextInt(2) == 0)
                        {
                            textureatlassprite = func_175371_a(Blocks.diamond_ore);
                        }
                        else
                        {
                            textureatlassprite = func_175371_a(Blocks.redstone_ore);
                        }
                    }
                    else if (j4 == 10)
                    {
                        textureatlassprite = func_175371_a(Blocks.iron_ore);
                    }
                    else if (j4 == 8)
                    {
                        textureatlassprite = func_175371_a(Blocks.coal_ore);
                    }
                    else if (j4 > 4)
                    {
                        textureatlassprite = func_175371_a(Blocks.stone);
                    }
                    else if (j4 > 0)
                    {
                        textureatlassprite = func_175371_a(Blocks.dirt);
                    }
                }
                else
                {
                    Block block = Blocks.bedrock;
                    textureatlassprite = func_175371_a(block);
                }

                mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                drawTexturedModalRect(i4 * 16 - i2, l3 * 16 - j2, textureatlassprite, 16, 16);
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.depthFunc(515);
        mc.getTextureManager().bindTexture(GuiAchievements.ACHIEVEMENT_BACKGROUND);

        for (int j5 = 0; j5 < AchievementList.achievementList.size(); ++j5)
        {
            Achievement achievement1 = AchievementList.achievementList.get(j5);

            if (achievement1.parentAchievement != null)
            {
                int k5 = achievement1.displayColumn * 24 - i + 11;
                int l5 = achievement1.displayRow * 24 - j + 11;
                int j6 = achievement1.parentAchievement.displayColumn * 24 - i + 11;
                int k6 = achievement1.parentAchievement.displayRow * 24 - j + 11;
                boolean flag = statFileWriter.hasAchievementUnlocked(achievement1);
                boolean flag1 = statFileWriter.canUnlockAchievement(achievement1);
                int k4 = statFileWriter.func_150874_c(achievement1);

                if (k4 <= 4)
                {
                    int l4 = -16777216;

                    if (flag)
                    {
                        l4 = -6250336;
                    }
                    else if (flag1)
                    {
                        l4 = -16711936;
                    }

                    drawHorizontalLine(k5, j6, l5, l4);
                    drawVerticalLine(j6, l5, k6, l4);

                    if (k5 > j6)
                    {
                        drawTexturedModalRect(k5 - 11 - 7, l5 - 5, 114, 234, 7, 11);
                    }
                    else if (k5 < j6)
                    {
                        drawTexturedModalRect(k5 + 11, l5 - 5, 107, 234, 7, 11);
                    }
                    else if (l5 > k6)
                    {
                        drawTexturedModalRect(k5 - 5, l5 - 11 - 7, 96, 234, 11, 7);
                    }
                    else if (l5 < k6)
                    {
                        drawTexturedModalRect(k5 - 5, l5 + 11, 96, 241, 11, 7);
                    }
                }
            }
        }

        Achievement achievement = null;
        float f3 = (float)(p_146552_1_ - i1) * field_146570_r;
        float f4 = (float)(p_146552_2_ - j1) * field_146570_r;
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();

        for (int i6 = 0; i6 < AchievementList.achievementList.size(); ++i6)
        {
            Achievement achievement2 = AchievementList.achievementList.get(i6);
            int l6 = achievement2.displayColumn * 24 - i;
            int j7 = achievement2.displayRow * 24 - j;

            if (l6 >= -24 && j7 >= -24 && (float)l6 <= 224.0F * field_146570_r && (float)j7 <= 155.0F * field_146570_r)
            {
                int l7 = statFileWriter.func_150874_c(achievement2);

                if (statFileWriter.hasAchievementUnlocked(achievement2))
                {
                    float f5 = 0.75F;
                    GlStateManager.color(f5, f5, f5, 1.0F);
                }
                else if (statFileWriter.canUnlockAchievement(achievement2))
                {
                    float f6 = 1.0F;
                    GlStateManager.color(f6, f6, f6, 1.0F);
                }
                else if (l7 < 3)
                {
                    float f7 = 0.3F;
                    GlStateManager.color(f7, f7, f7, 1.0F);
                }
                else if (l7 == 3)
                {
                    float f8 = 0.2F;
                    GlStateManager.color(f8, f8, f8, 1.0F);
                }
                else
                {
                    if (l7 != 4)
                    {
                        continue;
                    }

                    float f9 = 0.1F;
                    GlStateManager.color(f9, f9, f9, 1.0F);
                }

                mc.getTextureManager().bindTexture(GuiAchievements.ACHIEVEMENT_BACKGROUND);

                if (achievement2.getSpecial())
                {
                    drawTexturedModalRect(l6 - 2, j7 - 2, 26, 202, 26, 26);
                }
                else
                {
                    drawTexturedModalRect(l6 - 2, j7 - 2, 0, 202, 26, 26);
                }

                if (!statFileWriter.canUnlockAchievement(achievement2))
                {
                    float f10 = 0.1F;
                    GlStateManager.color(f10, f10, f10, 1.0F);
                    itemRender.func_175039_a(false);
                }

                GlStateManager.enableLighting();
                GlStateManager.enableCull();
                itemRender.renderItemAndEffectIntoGUI(achievement2.theItemStack, l6 + 3, j7 + 3);
                GlStateManager.blendFunc(770, 771);
                GlStateManager.disableLighting();

                if (!statFileWriter.canUnlockAchievement(achievement2))
                {
                    itemRender.func_175039_a(true);
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                if (f3 >= (float)l6 && f3 <= (float)(l6 + 22) && f4 >= (float)j7 && f4 <= (float)(j7 + 22))
                {
                    achievement = achievement2;
                }
            }
        }

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(GuiAchievements.ACHIEVEMENT_BACKGROUND);
        drawTexturedModalRect(k, l, 0, 0, field_146555_f, field_146557_g);
        zLevel = 0.0F;
        GlStateManager.depthFunc(515);
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        super.drawScreen(p_146552_1_, p_146552_2_, p_146552_3_);

        if (achievement != null)
        {
            String s = achievement.getStatName().getUnformattedText();
            String s1 = achievement.getDescription();
            int i7 = p_146552_1_ + 12;
            int k7 = p_146552_2_ - 4;
            int i8 = statFileWriter.func_150874_c(achievement);

            if (statFileWriter.canUnlockAchievement(achievement))
            {
                int j8 = Math.max(fontRendererObj.getStringWidth(s), 120);
                int i9 = fontRendererObj.splitStringWidth(s1, j8);

                if (statFileWriter.hasAchievementUnlocked(achievement))
                {
                    i9 += 12;
                }

                drawGradientRect(i7 - 3, k7 - 3, i7 + j8 + 3, k7 + i9 + 3 + 12, -1073741824, -1073741824);
                fontRendererObj.drawSplitString(s1, i7, k7 + 12, j8, -6250336);

                if (statFileWriter.hasAchievementUnlocked(achievement))
                {
                    fontRendererObj.drawStringWithShadow(I18n.format("achievement.taken"), (float)i7, (float)(k7 + i9 + 4), -7302913);
                }
            }
            else if (i8 == 3)
            {
                s = I18n.format("achievement.unknown");
                int k8 = Math.max(fontRendererObj.getStringWidth(s), 120);
                String s2 = (new ChatComponentTranslation("achievement.requires", achievement.parentAchievement.getStatName())).getUnformattedText();
                int i5 = fontRendererObj.splitStringWidth(s2, k8);
                drawGradientRect(i7 - 3, k7 - 3, i7 + k8 + 3, k7 + i5 + 12 + 3, -1073741824, -1073741824);
                fontRendererObj.drawSplitString(s2, i7, k7 + 12, k8, -9416624);
            }
            else if (i8 < 3)
            {
                int l8 = Math.max(fontRendererObj.getStringWidth(s), 120);
                String s3 = (new ChatComponentTranslation("achievement.requires", achievement.parentAchievement.getStatName())).getUnformattedText();
                int j9 = fontRendererObj.splitStringWidth(s3, l8);
                drawGradientRect(i7 - 3, k7 - 3, i7 + l8 + 3, k7 + j9 + 12 + 3, -1073741824, -1073741824);
                fontRendererObj.drawSplitString(s3, i7, k7 + 12, l8, -9416624);
            }
            else
            {
                s = null;
            }

            if (s != null)
            {
                fontRendererObj.drawStringWithShadow(s, (float)i7, (float)k7, statFileWriter.canUnlockAchievement(achievement) ? (achievement.getSpecial() ? -128 : -1) : (achievement.getSpecial() ? -8355776 : -8355712));
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        RenderHelper.disableStandardItemLighting();
    }

    private TextureAtlasSprite func_175371_a(Block p_175371_1_)
    {
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(p_175371_1_.getDefaultState());
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame()
    {
        return !loadingAchievements;
    }
}
