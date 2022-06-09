package net.minecraft.client.gui.achievement;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

public class GuiStats extends GuiScreen implements IProgressMeter
{
    protected GuiScreen parentScreen;
    protected String screenTitle = "Select world";
    private GuiStats.StatsGeneral generalStats;
    private GuiStats.StatsItem itemStats;
    private GuiStats.StatsBlock blockStats;
    private GuiStats.StatsMobsList mobStats;
    private final StatFileWriter field_146546_t;
    private GuiSlot displaySlot;

    /** When true, the game will be paused when the gui is shown */
    private boolean doesGuiPauseGame = true;

    public GuiStats(GuiScreen p_i1071_1_, StatFileWriter p_i1071_2_)
    {
        parentScreen = p_i1071_1_;
        field_146546_t = p_i1071_2_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        screenTitle = I18n.format("gui.stats");
        doesGuiPauseGame = true;
        mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS));
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        if (displaySlot != null)
        {
            displaySlot.handleMouseInput();
        }
    }

    public void func_175366_f()
    {
        generalStats = new GuiStats.StatsGeneral(mc);
        generalStats.registerScrollButtons(1, 1);
        itemStats = new GuiStats.StatsItem(mc);
        itemStats.registerScrollButtons(1, 1);
        blockStats = new GuiStats.StatsBlock(mc);
        blockStats.registerScrollButtons(1, 1);
        mobStats = new GuiStats.StatsMobsList(mc);
        mobStats.registerScrollButtons(1, 1);
    }

    public void createButtons()
    {
        buttonList.add(new GuiButton(0, width / 2 + 4, height - 28, 150, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(1, width / 2 - 160, height - 52, 80, 20, I18n.format("stat.generalButton")));
        GuiButton guibutton;
        buttonList.add(guibutton = new GuiButton(2, width / 2 - 80, height - 52, 80, 20, I18n.format("stat.blocksButton")));
        GuiButton guibutton1;
        buttonList.add(guibutton1 = new GuiButton(3, width / 2, height - 52, 80, 20, I18n.format("stat.itemsButton")));
        GuiButton guibutton2;
        buttonList.add(guibutton2 = new GuiButton(4, width / 2 + 80, height - 52, 80, 20, I18n.format("stat.mobsButton")));

        if (blockStats.getSize() == 0)
        {
            guibutton.enabled = false;
        }

        if (itemStats.getSize() == 0)
        {
            guibutton1.enabled = false;
        }

        if (mobStats.getSize() == 0)
        {
            guibutton2.enabled = false;
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 0)
            {
                mc.displayGuiScreen(parentScreen);
            }
            else if (button.id == 1)
            {
                displaySlot = generalStats;
            }
            else if (button.id == 3)
            {
                displaySlot = itemStats;
            }
            else if (button.id == 2)
            {
                displaySlot = blockStats;
            }
            else if (button.id == 4)
            {
                displaySlot = mobStats;
            }
            else
            {
                displaySlot.actionPerformed(button);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (doesGuiPauseGame)
        {
            drawDefaultBackground();
            drawCenteredString(fontRendererObj, I18n.format("multiplayer.downloadingStats"), width / 2, height / 2, 16777215);
            drawCenteredString(fontRendererObj, IProgressMeter.lanSearchStates[(int)(Minecraft.getSystemTime() / 150L % (long) IProgressMeter.lanSearchStates.length)], width / 2, height / 2 + fontRendererObj.FONT_HEIGHT * 2, 16777215);
        }
        else
        {
            displaySlot.drawScreen(mouseX, mouseY, partialTicks);
            drawCenteredString(fontRendererObj, screenTitle, width / 2, 20, 16777215);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    public void doneLoading()
    {
        if (doesGuiPauseGame)
        {
            func_175366_f();
            createButtons();
            displaySlot = generalStats;
            doesGuiPauseGame = false;
        }
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame()
    {
        return !doesGuiPauseGame;
    }

    private void drawStatsScreen(int p_146521_1_, int p_146521_2_, Item p_146521_3_)
    {
        drawButtonBackground(p_146521_1_ + 1, p_146521_2_ + 1);
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        itemRender.renderItemIntoGUI(new ItemStack(p_146521_3_, 1, 0), p_146521_1_ + 2, p_146521_2_ + 2);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    /**
     * Draws a gray box that serves as a button background.
     */
    private void drawButtonBackground(int p_146531_1_, int p_146531_2_)
    {
        drawSprite(p_146531_1_, p_146531_2_, 0, 0);
    }

    /**
     * Draws a sprite from assets/textures/gui/container/stats_icons.png
     */
    private void drawSprite(int p_146527_1_, int p_146527_2_, int p_146527_3_, int p_146527_4_)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(Gui.statIcons);
        float f = 0.0078125F;
        float f1 = 0.0078125F;
        int i = 18;
        int j = 18;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(p_146527_1_ + 0, p_146527_2_ + 18, zLevel).tex((float)(p_146527_3_ + 0) * 0.0078125F, (float)(p_146527_4_ + 18) * 0.0078125F).endVertex();
        worldrenderer.pos(p_146527_1_ + 18, p_146527_2_ + 18, zLevel).tex((float)(p_146527_3_ + 18) * 0.0078125F, (float)(p_146527_4_ + 18) * 0.0078125F).endVertex();
        worldrenderer.pos(p_146527_1_ + 18, p_146527_2_ + 0, zLevel).tex((float)(p_146527_3_ + 18) * 0.0078125F, (float)(p_146527_4_ + 0) * 0.0078125F).endVertex();
        worldrenderer.pos(p_146527_1_ + 0, p_146527_2_ + 0, zLevel).tex((float)(p_146527_3_ + 0) * 0.0078125F, (float)(p_146527_4_ + 0) * 0.0078125F).endVertex();
        tessellator.draw();
    }

    abstract class Stats extends GuiSlot
    {
        protected int field_148218_l = -1;
        protected List<StatCrafting> statsHolder;
        protected Comparator<StatCrafting> statSorter;
        protected int field_148217_o = -1;
        protected int field_148215_p;

        protected Stats(Minecraft mcIn)
        {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, 20);
            setShowSelectionBox(false);
            setHasListHeader(true, 20);
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
        }

        protected boolean isSelected(int slotIndex)
        {
            return false;
        }

        protected void drawBackground()
        {
            drawDefaultBackground();
        }

        protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_)
        {
            if (!Mouse.isButtonDown(0))
            {
                field_148218_l = -1;
            }

            if (field_148218_l == 0)
            {
                drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 0, 0);
            }
            else
            {
                drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 0, 18);
            }

            if (field_148218_l == 1)
            {
                drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 0, 0);
            }
            else
            {
                drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 0, 18);
            }

            if (field_148218_l == 2)
            {
                drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 0, 0);
            }
            else
            {
                drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 0, 18);
            }

            if (field_148217_o != -1)
            {
                int i = 79;
                int j = 18;

                if (field_148217_o == 1)
                {
                    i = 129;
                }
                else if (field_148217_o == 2)
                {
                    i = 179;
                }

                if (field_148215_p == 1)
                {
                    j = 36;
                }

                drawSprite(p_148129_1_ + i, p_148129_2_ + 1, j, 0);
            }
        }

        protected void func_148132_a(int p_148132_1_, int p_148132_2_)
        {
            field_148218_l = -1;

            if (p_148132_1_ >= 79 && p_148132_1_ < 115)
            {
                field_148218_l = 0;
            }
            else if (p_148132_1_ >= 129 && p_148132_1_ < 165)
            {
                field_148218_l = 1;
            }
            else if (p_148132_1_ >= 179 && p_148132_1_ < 215)
            {
                field_148218_l = 2;
            }

            if (field_148218_l >= 0)
            {
                func_148212_h(field_148218_l);
                mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            }
        }

        protected final int getSize()
        {
            return statsHolder.size();
        }

        protected final StatCrafting func_148211_c(int p_148211_1_)
        {
            return statsHolder.get(p_148211_1_);
        }

        protected abstract String func_148210_b(int p_148210_1_);

        protected void func_148209_a(StatBase p_148209_1_, int p_148209_2_, int p_148209_3_, boolean p_148209_4_)
        {
            if (p_148209_1_ != null)
            {
                String s = p_148209_1_.format(field_146546_t.readStat(p_148209_1_));
                drawString(fontRendererObj, s, p_148209_2_ - fontRendererObj.getStringWidth(s), p_148209_3_ + 5, p_148209_4_ ? 16777215 : 9474192);
            }
            else
            {
                String s1 = "-";
                drawString(fontRendererObj, s1, p_148209_2_ - fontRendererObj.getStringWidth(s1), p_148209_3_ + 5, p_148209_4_ ? 16777215 : 9474192);
            }
        }

        protected void func_148142_b(int p_148142_1_, int p_148142_2_)
        {
            if (p_148142_2_ >= top && p_148142_2_ <= bottom)
            {
                int i = getSlotIndexFromScreenCoords(p_148142_1_, p_148142_2_);
                int j = width / 2 - 92 - 16;

                if (i >= 0)
                {
                    if (p_148142_1_ < j + 40 || p_148142_1_ > j + 40 + 20)
                    {
                        return;
                    }

                    StatCrafting statcrafting = func_148211_c(i);
                    func_148213_a(statcrafting, p_148142_1_, p_148142_2_);
                }
                else
                {
                    String s = "";

                    if (p_148142_1_ >= j + 115 - 18 && p_148142_1_ <= j + 115)
                    {
                        s = func_148210_b(0);
                    }
                    else if (p_148142_1_ >= j + 165 - 18 && p_148142_1_ <= j + 165)
                    {
                        s = func_148210_b(1);
                    }
                    else
                    {
                        if (p_148142_1_ < j + 215 - 18 || p_148142_1_ > j + 215)
                        {
                            return;
                        }

                        s = func_148210_b(2);
                    }

                    s = ("" + I18n.format(s)).trim();

                    if (s.length() > 0)
                    {
                        int k = p_148142_1_ + 12;
                        int l = p_148142_2_ - 12;
                        int i1 = fontRendererObj.getStringWidth(s);
                        drawGradientRect(k - 3, l - 3, k + i1 + 3, l + 8 + 3, -1073741824, -1073741824);
                        fontRendererObj.drawStringWithShadow(s, (float)k, (float)l, -1);
                    }
                }
            }
        }

        protected void func_148213_a(StatCrafting p_148213_1_, int p_148213_2_, int p_148213_3_)
        {
            if (p_148213_1_ != null)
            {
                Item item = p_148213_1_.func_150959_a();
                ItemStack itemstack = new ItemStack(item);
                String s = itemstack.getUnlocalizedName();
                String s1 = ("" + I18n.format(s + ".name")).trim();

                if (s1.length() > 0)
                {
                    int i = p_148213_2_ + 12;
                    int j = p_148213_3_ - 12;
                    int k = fontRendererObj.getStringWidth(s1);
                    drawGradientRect(i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
                    fontRendererObj.drawStringWithShadow(s1, (float)i, (float)j, -1);
                }
            }
        }

        protected void func_148212_h(int p_148212_1_)
        {
            if (p_148212_1_ != field_148217_o)
            {
                field_148217_o = p_148212_1_;
                field_148215_p = -1;
            }
            else if (field_148215_p == -1)
            {
                field_148215_p = 1;
            }
            else
            {
                field_148217_o = -1;
                field_148215_p = 0;
            }

            Collections.sort(statsHolder, statSorter);
        }
    }

    class StatsBlock extends GuiStats.Stats
    {
        public StatsBlock(Minecraft mcIn)
        {
            super(mcIn);
            statsHolder = Lists.newArrayList();

            for (StatCrafting statcrafting : StatList.objectMineStats)
            {
                boolean flag = false;
                int i = Item.getIdFromItem(statcrafting.func_150959_a());

                if (field_146546_t.readStat(statcrafting) > 0)
                {
                    flag = true;
                }
                else if (StatList.objectUseStats[i] != null && field_146546_t.readStat(StatList.objectUseStats[i]) > 0)
                {
                    flag = true;
                }
                else if (StatList.objectCraftStats[i] != null && field_146546_t.readStat(StatList.objectCraftStats[i]) > 0)
                {
                    flag = true;
                }

                if (flag)
                {
                    statsHolder.add(statcrafting);
                }
            }

            statSorter = new Comparator<StatCrafting>()
            {
                public int compare(StatCrafting p_compare_1_, StatCrafting p_compare_2_)
                {
                    int j = Item.getIdFromItem(p_compare_1_.func_150959_a());
                    int k = Item.getIdFromItem(p_compare_2_.func_150959_a());
                    StatBase statbase = null;
                    StatBase statbase1 = null;

                    if (field_148217_o == 2)
                    {
                        statbase = StatList.mineBlockStatArray[j];
                        statbase1 = StatList.mineBlockStatArray[k];
                    }
                    else if (field_148217_o == 0)
                    {
                        statbase = StatList.objectCraftStats[j];
                        statbase1 = StatList.objectCraftStats[k];
                    }
                    else if (field_148217_o == 1)
                    {
                        statbase = StatList.objectUseStats[j];
                        statbase1 = StatList.objectUseStats[k];
                    }

                    if (statbase != null || statbase1 != null)
                    {
                        if (statbase == null)
                        {
                            return 1;
                        }

                        if (statbase1 == null)
                        {
                            return -1;
                        }

                        int l = field_146546_t.readStat(statbase);
                        int i1 = field_146546_t.readStat(statbase1);

                        if (l != i1)
                        {
                            return (l - i1) * field_148215_p;
                        }
                    }

                    return j - k;
                }
            };
        }

        protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_)
        {
            super.drawListHeader(p_148129_1_, p_148129_2_, p_148129_3_);

            if (field_148218_l == 0)
            {
                drawSprite(p_148129_1_ + 115 - 18 + 1, p_148129_2_ + 1 + 1, 18, 18);
            }
            else
            {
                drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 18, 18);
            }

            if (field_148218_l == 1)
            {
                drawSprite(p_148129_1_ + 165 - 18 + 1, p_148129_2_ + 1 + 1, 36, 18);
            }
            else
            {
                drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 36, 18);
            }

            if (field_148218_l == 2)
            {
                drawSprite(p_148129_1_ + 215 - 18 + 1, p_148129_2_ + 1 + 1, 54, 18);
            }
            else
            {
                drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 54, 18);
            }
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn)
        {
            StatCrafting statcrafting = func_148211_c(entryID);
            Item item = statcrafting.func_150959_a();
            drawStatsScreen(p_180791_2_ + 40, p_180791_3_, item);
            int i = Item.getIdFromItem(item);
            func_148209_a(StatList.objectCraftStats[i], p_180791_2_ + 115, p_180791_3_, entryID % 2 == 0);
            func_148209_a(StatList.objectUseStats[i], p_180791_2_ + 165, p_180791_3_, entryID % 2 == 0);
            func_148209_a(statcrafting, p_180791_2_ + 215, p_180791_3_, entryID % 2 == 0);
        }

        protected String func_148210_b(int p_148210_1_)
        {
            return p_148210_1_ == 0 ? "stat.crafted" : (p_148210_1_ == 1 ? "stat.used" : "stat.mined");
        }
    }

    class StatsGeneral extends GuiSlot
    {
        public StatsGeneral(Minecraft mcIn)
        {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, 10);
            setShowSelectionBox(false);
        }

        protected int getSize()
        {
            return StatList.generalStats.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
        }

        protected boolean isSelected(int slotIndex)
        {
            return false;
        }

        protected int getContentHeight()
        {
            return getSize() * 10;
        }

        protected void drawBackground()
        {
            drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn)
        {
            StatBase statbase = StatList.generalStats.get(entryID);
            drawString(fontRendererObj, statbase.getStatName().getUnformattedText(), p_180791_2_ + 2, p_180791_3_ + 1, entryID % 2 == 0 ? 16777215 : 9474192);
            String s = statbase.format(field_146546_t.readStat(statbase));
            drawString(fontRendererObj, s, p_180791_2_ + 2 + 213 - fontRendererObj.getStringWidth(s), p_180791_3_ + 1, entryID % 2 == 0 ? 16777215 : 9474192);
        }
    }

    class StatsItem extends GuiStats.Stats
    {
        public StatsItem(Minecraft mcIn)
        {
            super(mcIn);
            statsHolder = Lists.newArrayList();

            for (StatCrafting statcrafting : StatList.itemStats)
            {
                boolean flag = false;
                int i = Item.getIdFromItem(statcrafting.func_150959_a());

                if (field_146546_t.readStat(statcrafting) > 0)
                {
                    flag = true;
                }
                else if (StatList.objectBreakStats[i] != null && field_146546_t.readStat(StatList.objectBreakStats[i]) > 0)
                {
                    flag = true;
                }
                else if (StatList.objectCraftStats[i] != null && field_146546_t.readStat(StatList.objectCraftStats[i]) > 0)
                {
                    flag = true;
                }

                if (flag)
                {
                    statsHolder.add(statcrafting);
                }
            }

            statSorter = new Comparator<StatCrafting>()
            {
                public int compare(StatCrafting p_compare_1_, StatCrafting p_compare_2_)
                {
                    int j = Item.getIdFromItem(p_compare_1_.func_150959_a());
                    int k = Item.getIdFromItem(p_compare_2_.func_150959_a());
                    StatBase statbase = null;
                    StatBase statbase1 = null;

                    if (field_148217_o == 0)
                    {
                        statbase = StatList.objectBreakStats[j];
                        statbase1 = StatList.objectBreakStats[k];
                    }
                    else if (field_148217_o == 1)
                    {
                        statbase = StatList.objectCraftStats[j];
                        statbase1 = StatList.objectCraftStats[k];
                    }
                    else if (field_148217_o == 2)
                    {
                        statbase = StatList.objectUseStats[j];
                        statbase1 = StatList.objectUseStats[k];
                    }

                    if (statbase != null || statbase1 != null)
                    {
                        if (statbase == null)
                        {
                            return 1;
                        }

                        if (statbase1 == null)
                        {
                            return -1;
                        }

                        int l = field_146546_t.readStat(statbase);
                        int i1 = field_146546_t.readStat(statbase1);

                        if (l != i1)
                        {
                            return (l - i1) * field_148215_p;
                        }
                    }

                    return j - k;
                }
            };
        }

        protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_)
        {
            super.drawListHeader(p_148129_1_, p_148129_2_, p_148129_3_);

            if (field_148218_l == 0)
            {
                drawSprite(p_148129_1_ + 115 - 18 + 1, p_148129_2_ + 1 + 1, 72, 18);
            }
            else
            {
                drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 72, 18);
            }

            if (field_148218_l == 1)
            {
                drawSprite(p_148129_1_ + 165 - 18 + 1, p_148129_2_ + 1 + 1, 18, 18);
            }
            else
            {
                drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 18, 18);
            }

            if (field_148218_l == 2)
            {
                drawSprite(p_148129_1_ + 215 - 18 + 1, p_148129_2_ + 1 + 1, 36, 18);
            }
            else
            {
                drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 36, 18);
            }
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn)
        {
            StatCrafting statcrafting = func_148211_c(entryID);
            Item item = statcrafting.func_150959_a();
            drawStatsScreen(p_180791_2_ + 40, p_180791_3_, item);
            int i = Item.getIdFromItem(item);
            func_148209_a(StatList.objectBreakStats[i], p_180791_2_ + 115, p_180791_3_, entryID % 2 == 0);
            func_148209_a(StatList.objectCraftStats[i], p_180791_2_ + 165, p_180791_3_, entryID % 2 == 0);
            func_148209_a(statcrafting, p_180791_2_ + 215, p_180791_3_, entryID % 2 == 0);
        }

        protected String func_148210_b(int p_148210_1_)
        {
            return p_148210_1_ == 1 ? "stat.crafted" : (p_148210_1_ == 2 ? "stat.used" : "stat.depleted");
        }
    }

    class StatsMobsList extends GuiSlot
    {
        private final List<EntityList.EntityEggInfo> field_148222_l = Lists.newArrayList();

        public StatsMobsList(Minecraft mcIn)
        {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, fontRendererObj.FONT_HEIGHT * 4);
            setShowSelectionBox(false);

            for (EntityList.EntityEggInfo entitylist$entityegginfo : EntityList.entityEggs.values())
            {
                if (field_146546_t.readStat(entitylist$entityegginfo.field_151512_d) > 0 || field_146546_t.readStat(entitylist$entityegginfo.field_151513_e) > 0)
                {
                    field_148222_l.add(entitylist$entityegginfo);
                }
            }
        }

        protected int getSize()
        {
            return field_148222_l.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
        }

        protected boolean isSelected(int slotIndex)
        {
            return false;
        }

        protected int getContentHeight()
        {
            return getSize() * fontRendererObj.FONT_HEIGHT * 4;
        }

        protected void drawBackground()
        {
            drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn)
        {
            EntityList.EntityEggInfo entitylist$entityegginfo = field_148222_l.get(entryID);
            String s = I18n.format("entity." + EntityList.getStringFromID(entitylist$entityegginfo.spawnedID) + ".name");
            int i = field_146546_t.readStat(entitylist$entityegginfo.field_151512_d);
            int j = field_146546_t.readStat(entitylist$entityegginfo.field_151513_e);
            String s1 = I18n.format("stat.entityKills", Integer.valueOf(i), s);
            String s2 = I18n.format("stat.entityKilledBy", s, Integer.valueOf(j));

            if (i == 0)
            {
                s1 = I18n.format("stat.entityKills.none", s);
            }

            if (j == 0)
            {
                s2 = I18n.format("stat.entityKilledBy.none", s);
            }

            drawString(fontRendererObj, s, p_180791_2_ + 2 - 10, p_180791_3_ + 1, 16777215);
            drawString(fontRendererObj, s1, p_180791_2_ + 2, p_180791_3_ + 1 + fontRendererObj.FONT_HEIGHT, i == 0 ? 6316128 : 9474192);
            drawString(fontRendererObj, s2, p_180791_2_ + 2, p_180791_3_ + 1 + fontRendererObj.FONT_HEIGHT * 2, j == 0 ? 6316128 : 9474192);
        }
    }
}
