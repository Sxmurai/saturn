package net.minecraft.client.gui;

import com.google.common.base.Predicate;
import com.google.common.primitives.Floats;
import java.io.IOException;
import java.util.Random;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.ChunkProviderSettings;

public class GuiCustomizeWorldScreen extends GuiScreen implements GuiSlider.FormatHelper, GuiPageButtonList.GuiResponder
{
    private final GuiCreateWorld field_175343_i;
    protected String field_175341_a = "Customize World Settings";
    protected String field_175333_f = "Page 1 of 3";
    protected String field_175335_g = "Basic Settings";
    protected String[] field_175342_h = new String[4];
    private GuiPageButtonList field_175349_r;
    private GuiButton field_175348_s;
    private GuiButton field_175347_t;
    private GuiButton field_175346_u;
    private GuiButton field_175345_v;
    private GuiButton field_175344_w;
    private GuiButton field_175352_x;
    private GuiButton field_175351_y;
    private GuiButton field_175350_z;
    private boolean field_175338_A = false;
    private int field_175339_B = 0;
    private boolean field_175340_C = false;
    private final Predicate<String> field_175332_D = new Predicate<String>()
    {
        public boolean apply(String p_apply_1_)
        {
            Float f = Floats.tryParse(p_apply_1_);
            return p_apply_1_.length() == 0 || f != null && Floats.isFinite(f.floatValue()) && f.floatValue() >= 0.0F;
        }
    };
    private final ChunkProviderSettings.Factory field_175334_E = new ChunkProviderSettings.Factory();
    private ChunkProviderSettings.Factory field_175336_F;

    /** A Random instance for this world customization */
    private final Random random = new Random();

    public GuiCustomizeWorldScreen(GuiScreen p_i45521_1_, String p_i45521_2_)
    {
        field_175343_i = (GuiCreateWorld)p_i45521_1_;
        func_175324_a(p_i45521_2_);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        int i = 0;
        int j = 0;

        if (field_175349_r != null)
        {
            i = field_175349_r.func_178059_e();
            j = field_175349_r.getAmountScrolled();
        }

        field_175341_a = I18n.format("options.customizeTitle");
        buttonList.clear();
        buttonList.add(field_175345_v = new GuiButton(302, 20, 5, 80, 20, I18n.format("createWorld.customize.custom.prev")));
        buttonList.add(field_175344_w = new GuiButton(303, width - 100, 5, 80, 20, I18n.format("createWorld.customize.custom.next")));
        buttonList.add(field_175346_u = new GuiButton(304, width / 2 - 187, height - 27, 90, 20, I18n.format("createWorld.customize.custom.defaults")));
        buttonList.add(field_175347_t = new GuiButton(301, width / 2 - 92, height - 27, 90, 20, I18n.format("createWorld.customize.custom.randomize")));
        buttonList.add(field_175350_z = new GuiButton(305, width / 2 + 3, height - 27, 90, 20, I18n.format("createWorld.customize.custom.presets")));
        buttonList.add(field_175348_s = new GuiButton(300, width / 2 + 98, height - 27, 90, 20, I18n.format("gui.done")));
        field_175346_u.enabled = field_175338_A;
        field_175352_x = new GuiButton(306, width / 2 - 55, 160, 50, 20, I18n.format("gui.yes"));
        field_175352_x.visible = false;
        buttonList.add(field_175352_x);
        field_175351_y = new GuiButton(307, width / 2 + 5, 160, 50, 20, I18n.format("gui.no"));
        field_175351_y.visible = false;
        buttonList.add(field_175351_y);

        if (field_175339_B != 0)
        {
            field_175352_x.visible = true;
            field_175351_y.visible = true;
        }

        func_175325_f();

        if (i != 0)
        {
            field_175349_r.func_181156_c(i);
            field_175349_r.scrollBy(j);
            func_175328_i();
        }
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        field_175349_r.handleMouseInput();
    }

    private void func_175325_f()
    {
        GuiPageButtonList.GuiListEntry[] aguipagebuttonlist$guilistentry = new GuiPageButtonList.GuiListEntry[] {new GuiPageButtonList.GuiSlideEntry(160, I18n.format("createWorld.customize.custom.seaLevel"), true, this, 1.0F, 255.0F, (float) field_175336_F.seaLevel), new GuiPageButtonList.GuiButtonEntry(148, I18n.format("createWorld.customize.custom.useCaves"), true, field_175336_F.useCaves), new GuiPageButtonList.GuiButtonEntry(150, I18n.format("createWorld.customize.custom.useStrongholds"), true, field_175336_F.useStrongholds), new GuiPageButtonList.GuiButtonEntry(151, I18n.format("createWorld.customize.custom.useVillages"), true, field_175336_F.useVillages), new GuiPageButtonList.GuiButtonEntry(152, I18n.format("createWorld.customize.custom.useMineShafts"), true, field_175336_F.useMineShafts), new GuiPageButtonList.GuiButtonEntry(153, I18n.format("createWorld.customize.custom.useTemples"), true, field_175336_F.useTemples), new GuiPageButtonList.GuiButtonEntry(210, I18n.format("createWorld.customize.custom.useMonuments"), true, field_175336_F.useMonuments), new GuiPageButtonList.GuiButtonEntry(154, I18n.format("createWorld.customize.custom.useRavines"), true, field_175336_F.useRavines), new GuiPageButtonList.GuiButtonEntry(149, I18n.format("createWorld.customize.custom.useDungeons"), true, field_175336_F.useDungeons), new GuiPageButtonList.GuiSlideEntry(157, I18n.format("createWorld.customize.custom.dungeonChance"), true, this, 1.0F, 100.0F, (float) field_175336_F.dungeonChance), new GuiPageButtonList.GuiButtonEntry(155, I18n.format("createWorld.customize.custom.useWaterLakes"), true, field_175336_F.useWaterLakes), new GuiPageButtonList.GuiSlideEntry(158, I18n.format("createWorld.customize.custom.waterLakeChance"), true, this, 1.0F, 100.0F, (float) field_175336_F.waterLakeChance), new GuiPageButtonList.GuiButtonEntry(156, I18n.format("createWorld.customize.custom.useLavaLakes"), true, field_175336_F.useLavaLakes), new GuiPageButtonList.GuiSlideEntry(159, I18n.format("createWorld.customize.custom.lavaLakeChance"), true, this, 10.0F, 100.0F, (float) field_175336_F.lavaLakeChance), new GuiPageButtonList.GuiButtonEntry(161, I18n.format("createWorld.customize.custom.useLavaOceans"), true, field_175336_F.useLavaOceans), new GuiPageButtonList.GuiSlideEntry(162, I18n.format("createWorld.customize.custom.fixedBiome"), true, this, -1.0F, 37.0F, (float) field_175336_F.fixedBiome), new GuiPageButtonList.GuiSlideEntry(163, I18n.format("createWorld.customize.custom.biomeSize"), true, this, 1.0F, 8.0F, (float) field_175336_F.biomeSize), new GuiPageButtonList.GuiSlideEntry(164, I18n.format("createWorld.customize.custom.riverSize"), true, this, 1.0F, 5.0F, (float) field_175336_F.riverSize)};
        GuiPageButtonList.GuiListEntry[] aguipagebuttonlist$guilistentry1 = new GuiPageButtonList.GuiListEntry[] {new GuiPageButtonList.GuiLabelEntry(416, I18n.format("tile.dirt.name"), false), null, new GuiPageButtonList.GuiSlideEntry(165, I18n.format("createWorld.customize.custom.size"), false, this, 1.0F, 50.0F, (float) field_175336_F.dirtSize), new GuiPageButtonList.GuiSlideEntry(166, I18n.format("createWorld.customize.custom.count"), false, this, 0.0F, 40.0F, (float) field_175336_F.dirtCount), new GuiPageButtonList.GuiSlideEntry(167, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.dirtMinHeight), new GuiPageButtonList.GuiSlideEntry(168, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.dirtMaxHeight), new GuiPageButtonList.GuiLabelEntry(417, I18n.format("tile.gravel.name"), false), null, new GuiPageButtonList.GuiSlideEntry(169, I18n.format("createWorld.customize.custom.size"), false, this, 1.0F, 50.0F, (float) field_175336_F.gravelSize), new GuiPageButtonList.GuiSlideEntry(170, I18n.format("createWorld.customize.custom.count"), false, this, 0.0F, 40.0F, (float) field_175336_F.gravelCount), new GuiPageButtonList.GuiSlideEntry(171, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.gravelMinHeight), new GuiPageButtonList.GuiSlideEntry(172, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.gravelMaxHeight), new GuiPageButtonList.GuiLabelEntry(418, I18n.format("tile.stone.granite.name"), false), null, new GuiPageButtonList.GuiSlideEntry(173, I18n.format("createWorld.customize.custom.size"), false, this, 1.0F, 50.0F, (float) field_175336_F.graniteSize), new GuiPageButtonList.GuiSlideEntry(174, I18n.format("createWorld.customize.custom.count"), false, this, 0.0F, 40.0F, (float) field_175336_F.graniteCount), new GuiPageButtonList.GuiSlideEntry(175, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.graniteMinHeight), new GuiPageButtonList.GuiSlideEntry(176, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.graniteMaxHeight), new GuiPageButtonList.GuiLabelEntry(419, I18n.format("tile.stone.diorite.name"), false), null, new GuiPageButtonList.GuiSlideEntry(177, I18n.format("createWorld.customize.custom.size"), false, this, 1.0F, 50.0F, (float) field_175336_F.dioriteSize), new GuiPageButtonList.GuiSlideEntry(178, I18n.format("createWorld.customize.custom.count"), false, this, 0.0F, 40.0F, (float) field_175336_F.dioriteCount), new GuiPageButtonList.GuiSlideEntry(179, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.dioriteMinHeight), new GuiPageButtonList.GuiSlideEntry(180, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.dioriteMaxHeight), new GuiPageButtonList.GuiLabelEntry(420, I18n.format("tile.stone.andesite.name"), false), null, new GuiPageButtonList.GuiSlideEntry(181, I18n.format("createWorld.customize.custom.size"), false, this, 1.0F, 50.0F, (float) field_175336_F.andesiteSize), new GuiPageButtonList.GuiSlideEntry(182, I18n.format("createWorld.customize.custom.count"), false, this, 0.0F, 40.0F, (float) field_175336_F.andesiteCount), new GuiPageButtonList.GuiSlideEntry(183, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.andesiteMinHeight), new GuiPageButtonList.GuiSlideEntry(184, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.andesiteMaxHeight), new GuiPageButtonList.GuiLabelEntry(421, I18n.format("tile.oreCoal.name"), false), null, new GuiPageButtonList.GuiSlideEntry(185, I18n.format("createWorld.customize.custom.size"), false, this, 1.0F, 50.0F, (float) field_175336_F.coalSize), new GuiPageButtonList.GuiSlideEntry(186, I18n.format("createWorld.customize.custom.count"), false, this, 0.0F, 40.0F, (float) field_175336_F.coalCount), new GuiPageButtonList.GuiSlideEntry(187, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.coalMinHeight), new GuiPageButtonList.GuiSlideEntry(189, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.coalMaxHeight), new GuiPageButtonList.GuiLabelEntry(422, I18n.format("tile.oreIron.name"), false), null, new GuiPageButtonList.GuiSlideEntry(190, I18n.format("createWorld.customize.custom.size"), false, this, 1.0F, 50.0F, (float) field_175336_F.ironSize), new GuiPageButtonList.GuiSlideEntry(191, I18n.format("createWorld.customize.custom.count"), false, this, 0.0F, 40.0F, (float) field_175336_F.ironCount), new GuiPageButtonList.GuiSlideEntry(192, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.ironMinHeight), new GuiPageButtonList.GuiSlideEntry(193, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.ironMaxHeight), new GuiPageButtonList.GuiLabelEntry(423, I18n.format("tile.oreGold.name"), false), null, new GuiPageButtonList.GuiSlideEntry(194, I18n.format("createWorld.customize.custom.size"), false, this, 1.0F, 50.0F, (float) field_175336_F.goldSize), new GuiPageButtonList.GuiSlideEntry(195, I18n.format("createWorld.customize.custom.count"), false, this, 0.0F, 40.0F, (float) field_175336_F.goldCount), new GuiPageButtonList.GuiSlideEntry(196, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.goldMinHeight), new GuiPageButtonList.GuiSlideEntry(197, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.goldMaxHeight), new GuiPageButtonList.GuiLabelEntry(424, I18n.format("tile.oreRedstone.name"), false), null, new GuiPageButtonList.GuiSlideEntry(198, I18n.format("createWorld.customize.custom.size"), false, this, 1.0F, 50.0F, (float) field_175336_F.redstoneSize), new GuiPageButtonList.GuiSlideEntry(199, I18n.format("createWorld.customize.custom.count"), false, this, 0.0F, 40.0F, (float) field_175336_F.redstoneCount), new GuiPageButtonList.GuiSlideEntry(200, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.redstoneMinHeight), new GuiPageButtonList.GuiSlideEntry(201, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.redstoneMaxHeight), new GuiPageButtonList.GuiLabelEntry(425, I18n.format("tile.oreDiamond.name"), false), null, new GuiPageButtonList.GuiSlideEntry(202, I18n.format("createWorld.customize.custom.size"), false, this, 1.0F, 50.0F, (float) field_175336_F.diamondSize), new GuiPageButtonList.GuiSlideEntry(203, I18n.format("createWorld.customize.custom.count"), false, this, 0.0F, 40.0F, (float) field_175336_F.diamondCount), new GuiPageButtonList.GuiSlideEntry(204, I18n.format("createWorld.customize.custom.minHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.diamondMinHeight), new GuiPageButtonList.GuiSlideEntry(205, I18n.format("createWorld.customize.custom.maxHeight"), false, this, 0.0F, 255.0F, (float) field_175336_F.diamondMaxHeight), new GuiPageButtonList.GuiLabelEntry(426, I18n.format("tile.oreLapis.name"), false), null, new GuiPageButtonList.GuiSlideEntry(206, I18n.format("createWorld.customize.custom.size"), false, this, 1.0F, 50.0F, (float) field_175336_F.lapisSize), new GuiPageButtonList.GuiSlideEntry(207, I18n.format("createWorld.customize.custom.count"), false, this, 0.0F, 40.0F, (float) field_175336_F.lapisCount), new GuiPageButtonList.GuiSlideEntry(208, I18n.format("createWorld.customize.custom.center"), false, this, 0.0F, 255.0F, (float) field_175336_F.lapisCenterHeight), new GuiPageButtonList.GuiSlideEntry(209, I18n.format("createWorld.customize.custom.spread"), false, this, 0.0F, 255.0F, (float) field_175336_F.lapisSpread)};
        GuiPageButtonList.GuiListEntry[] aguipagebuttonlist$guilistentry2 = new GuiPageButtonList.GuiListEntry[] {new GuiPageButtonList.GuiSlideEntry(100, I18n.format("createWorld.customize.custom.mainNoiseScaleX"), false, this, 1.0F, 5000.0F, field_175336_F.mainNoiseScaleX), new GuiPageButtonList.GuiSlideEntry(101, I18n.format("createWorld.customize.custom.mainNoiseScaleY"), false, this, 1.0F, 5000.0F, field_175336_F.mainNoiseScaleY), new GuiPageButtonList.GuiSlideEntry(102, I18n.format("createWorld.customize.custom.mainNoiseScaleZ"), false, this, 1.0F, 5000.0F, field_175336_F.mainNoiseScaleZ), new GuiPageButtonList.GuiSlideEntry(103, I18n.format("createWorld.customize.custom.depthNoiseScaleX"), false, this, 1.0F, 2000.0F, field_175336_F.depthNoiseScaleX), new GuiPageButtonList.GuiSlideEntry(104, I18n.format("createWorld.customize.custom.depthNoiseScaleZ"), false, this, 1.0F, 2000.0F, field_175336_F.depthNoiseScaleZ), new GuiPageButtonList.GuiSlideEntry(105, I18n.format("createWorld.customize.custom.depthNoiseScaleExponent"), false, this, 0.01F, 20.0F, field_175336_F.depthNoiseScaleExponent), new GuiPageButtonList.GuiSlideEntry(106, I18n.format("createWorld.customize.custom.baseSize"), false, this, 1.0F, 25.0F, field_175336_F.baseSize), new GuiPageButtonList.GuiSlideEntry(107, I18n.format("createWorld.customize.custom.coordinateScale"), false, this, 1.0F, 6000.0F, field_175336_F.coordinateScale), new GuiPageButtonList.GuiSlideEntry(108, I18n.format("createWorld.customize.custom.heightScale"), false, this, 1.0F, 6000.0F, field_175336_F.heightScale), new GuiPageButtonList.GuiSlideEntry(109, I18n.format("createWorld.customize.custom.stretchY"), false, this, 0.01F, 50.0F, field_175336_F.stretchY), new GuiPageButtonList.GuiSlideEntry(110, I18n.format("createWorld.customize.custom.upperLimitScale"), false, this, 1.0F, 5000.0F, field_175336_F.upperLimitScale), new GuiPageButtonList.GuiSlideEntry(111, I18n.format("createWorld.customize.custom.lowerLimitScale"), false, this, 1.0F, 5000.0F, field_175336_F.lowerLimitScale), new GuiPageButtonList.GuiSlideEntry(112, I18n.format("createWorld.customize.custom.biomeDepthWeight"), false, this, 1.0F, 20.0F, field_175336_F.biomeDepthWeight), new GuiPageButtonList.GuiSlideEntry(113, I18n.format("createWorld.customize.custom.biomeDepthOffset"), false, this, 0.0F, 20.0F, field_175336_F.biomeDepthOffset), new GuiPageButtonList.GuiSlideEntry(114, I18n.format("createWorld.customize.custom.biomeScaleWeight"), false, this, 1.0F, 20.0F, field_175336_F.biomeScaleWeight), new GuiPageButtonList.GuiSlideEntry(115, I18n.format("createWorld.customize.custom.biomeScaleOffset"), false, this, 0.0F, 20.0F, field_175336_F.biomeScaleOffset)};
        GuiPageButtonList.GuiListEntry[] aguipagebuttonlist$guilistentry3 = new GuiPageButtonList.GuiListEntry[] {new GuiPageButtonList.GuiLabelEntry(400, I18n.format("createWorld.customize.custom.mainNoiseScaleX") + ":", false), new GuiPageButtonList.EditBoxEntry(132, String.format("%5.3f", Float.valueOf(field_175336_F.mainNoiseScaleX)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(401, I18n.format("createWorld.customize.custom.mainNoiseScaleY") + ":", false), new GuiPageButtonList.EditBoxEntry(133, String.format("%5.3f", Float.valueOf(field_175336_F.mainNoiseScaleY)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(402, I18n.format("createWorld.customize.custom.mainNoiseScaleZ") + ":", false), new GuiPageButtonList.EditBoxEntry(134, String.format("%5.3f", Float.valueOf(field_175336_F.mainNoiseScaleZ)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(403, I18n.format("createWorld.customize.custom.depthNoiseScaleX") + ":", false), new GuiPageButtonList.EditBoxEntry(135, String.format("%5.3f", Float.valueOf(field_175336_F.depthNoiseScaleX)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(404, I18n.format("createWorld.customize.custom.depthNoiseScaleZ") + ":", false), new GuiPageButtonList.EditBoxEntry(136, String.format("%5.3f", Float.valueOf(field_175336_F.depthNoiseScaleZ)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(405, I18n.format("createWorld.customize.custom.depthNoiseScaleExponent") + ":", false), new GuiPageButtonList.EditBoxEntry(137, String.format("%2.3f", Float.valueOf(field_175336_F.depthNoiseScaleExponent)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(406, I18n.format("createWorld.customize.custom.baseSize") + ":", false), new GuiPageButtonList.EditBoxEntry(138, String.format("%2.3f", Float.valueOf(field_175336_F.baseSize)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(407, I18n.format("createWorld.customize.custom.coordinateScale") + ":", false), new GuiPageButtonList.EditBoxEntry(139, String.format("%5.3f", Float.valueOf(field_175336_F.coordinateScale)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(408, I18n.format("createWorld.customize.custom.heightScale") + ":", false), new GuiPageButtonList.EditBoxEntry(140, String.format("%5.3f", Float.valueOf(field_175336_F.heightScale)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(409, I18n.format("createWorld.customize.custom.stretchY") + ":", false), new GuiPageButtonList.EditBoxEntry(141, String.format("%2.3f", Float.valueOf(field_175336_F.stretchY)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(410, I18n.format("createWorld.customize.custom.upperLimitScale") + ":", false), new GuiPageButtonList.EditBoxEntry(142, String.format("%5.3f", Float.valueOf(field_175336_F.upperLimitScale)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(411, I18n.format("createWorld.customize.custom.lowerLimitScale") + ":", false), new GuiPageButtonList.EditBoxEntry(143, String.format("%5.3f", Float.valueOf(field_175336_F.lowerLimitScale)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(412, I18n.format("createWorld.customize.custom.biomeDepthWeight") + ":", false), new GuiPageButtonList.EditBoxEntry(144, String.format("%2.3f", Float.valueOf(field_175336_F.biomeDepthWeight)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(413, I18n.format("createWorld.customize.custom.biomeDepthOffset") + ":", false), new GuiPageButtonList.EditBoxEntry(145, String.format("%2.3f", Float.valueOf(field_175336_F.biomeDepthOffset)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(414, I18n.format("createWorld.customize.custom.biomeScaleWeight") + ":", false), new GuiPageButtonList.EditBoxEntry(146, String.format("%2.3f", Float.valueOf(field_175336_F.biomeScaleWeight)), false, field_175332_D), new GuiPageButtonList.GuiLabelEntry(415, I18n.format("createWorld.customize.custom.biomeScaleOffset") + ":", false), new GuiPageButtonList.EditBoxEntry(147, String.format("%2.3f", Float.valueOf(field_175336_F.biomeScaleOffset)), false, field_175332_D)};
        field_175349_r = new GuiPageButtonList(mc, width, height, 32, height - 32, 25, this, aguipagebuttonlist$guilistentry, aguipagebuttonlist$guilistentry1, aguipagebuttonlist$guilistentry2, aguipagebuttonlist$guilistentry3);

        for (int i = 0; i < 4; ++i)
        {
            field_175342_h[i] = I18n.format("createWorld.customize.custom.page" + i);
        }

        func_175328_i();
    }

    public String func_175323_a()
    {
        return field_175336_F.toString().replace("\n", "");
    }

    public void func_175324_a(String p_175324_1_)
    {
        if (p_175324_1_ != null && p_175324_1_.length() != 0)
        {
            field_175336_F = ChunkProviderSettings.Factory.jsonToFactory(p_175324_1_);
        }
        else
        {
            field_175336_F = new ChunkProviderSettings.Factory();
        }
    }

    public void func_175319_a(int p_175319_1_, String p_175319_2_)
    {
        float f = 0.0F;

        try
        {
            f = Float.parseFloat(p_175319_2_);
        }
        catch (NumberFormatException var5)
        {
        }

        float f1 = 0.0F;

        switch (p_175319_1_)
        {
            case 132:
                f1 = field_175336_F.mainNoiseScaleX = MathHelper.clamp_float(f, 1.0F, 5000.0F);
                break;

            case 133:
                f1 = field_175336_F.mainNoiseScaleY = MathHelper.clamp_float(f, 1.0F, 5000.0F);
                break;

            case 134:
                f1 = field_175336_F.mainNoiseScaleZ = MathHelper.clamp_float(f, 1.0F, 5000.0F);
                break;

            case 135:
                f1 = field_175336_F.depthNoiseScaleX = MathHelper.clamp_float(f, 1.0F, 2000.0F);
                break;

            case 136:
                f1 = field_175336_F.depthNoiseScaleZ = MathHelper.clamp_float(f, 1.0F, 2000.0F);
                break;

            case 137:
                f1 = field_175336_F.depthNoiseScaleExponent = MathHelper.clamp_float(f, 0.01F, 20.0F);
                break;

            case 138:
                f1 = field_175336_F.baseSize = MathHelper.clamp_float(f, 1.0F, 25.0F);
                break;

            case 139:
                f1 = field_175336_F.coordinateScale = MathHelper.clamp_float(f, 1.0F, 6000.0F);
                break;

            case 140:
                f1 = field_175336_F.heightScale = MathHelper.clamp_float(f, 1.0F, 6000.0F);
                break;

            case 141:
                f1 = field_175336_F.stretchY = MathHelper.clamp_float(f, 0.01F, 50.0F);
                break;

            case 142:
                f1 = field_175336_F.upperLimitScale = MathHelper.clamp_float(f, 1.0F, 5000.0F);
                break;

            case 143:
                f1 = field_175336_F.lowerLimitScale = MathHelper.clamp_float(f, 1.0F, 5000.0F);
                break;

            case 144:
                f1 = field_175336_F.biomeDepthWeight = MathHelper.clamp_float(f, 1.0F, 20.0F);
                break;

            case 145:
                f1 = field_175336_F.biomeDepthOffset = MathHelper.clamp_float(f, 0.0F, 20.0F);
                break;

            case 146:
                f1 = field_175336_F.biomeScaleWeight = MathHelper.clamp_float(f, 1.0F, 20.0F);
                break;

            case 147:
                f1 = field_175336_F.biomeScaleOffset = MathHelper.clamp_float(f, 0.0F, 20.0F);
        }

        if (f1 != f && f != 0.0F)
        {
            ((GuiTextField) field_175349_r.func_178061_c(p_175319_1_)).setText(func_175330_b(p_175319_1_, f1));
        }

        ((GuiSlider) field_175349_r.func_178061_c(p_175319_1_ - 132 + 100)).func_175218_a(f1, false);

        if (!field_175336_F.equals(field_175334_E))
        {
            func_181031_a(true);
        }
    }

    private void func_181031_a(boolean p_181031_1_)
    {
        field_175338_A = p_181031_1_;
        field_175346_u.enabled = p_181031_1_;
    }

    public String getText(int id, String name, float value)
    {
        return name + ": " + func_175330_b(id, value);
    }

    private String func_175330_b(int p_175330_1_, float p_175330_2_)
    {
        switch (p_175330_1_)
        {
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 107:
            case 108:
            case 110:
            case 111:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 139:
            case 140:
            case 142:
            case 143:
                return String.format("%5.3f", Float.valueOf(p_175330_2_));

            case 105:
            case 106:
            case 109:
            case 112:
            case 113:
            case 114:
            case 115:
            case 137:
            case 138:
            case 141:
            case 144:
            case 145:
            case 146:
            case 147:
                return String.format("%2.3f", Float.valueOf(p_175330_2_));

            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
            case 160:
            case 161:
            default:
                return String.format("%d", Integer.valueOf((int)p_175330_2_));

            case 162:
                if (p_175330_2_ < 0.0F)
                {
                    return I18n.format("gui.all");
                }
                else if ((int)p_175330_2_ >= BiomeGenBase.hell.biomeID)
                {
                    BiomeGenBase biomegenbase1 = BiomeGenBase.getBiomeGenArray()[(int)p_175330_2_ + 2];
                    return biomegenbase1 != null ? biomegenbase1.biomeName : "?";
                }
                else
                {
                    BiomeGenBase biomegenbase = BiomeGenBase.getBiomeGenArray()[(int)p_175330_2_];
                    return biomegenbase != null ? biomegenbase.biomeName : "?";
                }
        }
    }

    public void func_175321_a(int p_175321_1_, boolean p_175321_2_)
    {
        switch (p_175321_1_)
        {
            case 148:
                field_175336_F.useCaves = p_175321_2_;
                break;

            case 149:
                field_175336_F.useDungeons = p_175321_2_;
                break;

            case 150:
                field_175336_F.useStrongholds = p_175321_2_;
                break;

            case 151:
                field_175336_F.useVillages = p_175321_2_;
                break;

            case 152:
                field_175336_F.useMineShafts = p_175321_2_;
                break;

            case 153:
                field_175336_F.useTemples = p_175321_2_;
                break;

            case 154:
                field_175336_F.useRavines = p_175321_2_;
                break;

            case 155:
                field_175336_F.useWaterLakes = p_175321_2_;
                break;

            case 156:
                field_175336_F.useLavaLakes = p_175321_2_;
                break;

            case 161:
                field_175336_F.useLavaOceans = p_175321_2_;
                break;

            case 210:
                field_175336_F.useMonuments = p_175321_2_;
        }

        if (!field_175336_F.equals(field_175334_E))
        {
            func_181031_a(true);
        }
    }

    public void onTick(int id, float value)
    {
        switch (id)
        {
            case 100:
                field_175336_F.mainNoiseScaleX = value;
                break;

            case 101:
                field_175336_F.mainNoiseScaleY = value;
                break;

            case 102:
                field_175336_F.mainNoiseScaleZ = value;
                break;

            case 103:
                field_175336_F.depthNoiseScaleX = value;
                break;

            case 104:
                field_175336_F.depthNoiseScaleZ = value;
                break;

            case 105:
                field_175336_F.depthNoiseScaleExponent = value;
                break;

            case 106:
                field_175336_F.baseSize = value;
                break;

            case 107:
                field_175336_F.coordinateScale = value;
                break;

            case 108:
                field_175336_F.heightScale = value;
                break;

            case 109:
                field_175336_F.stretchY = value;
                break;

            case 110:
                field_175336_F.upperLimitScale = value;
                break;

            case 111:
                field_175336_F.lowerLimitScale = value;
                break;

            case 112:
                field_175336_F.biomeDepthWeight = value;
                break;

            case 113:
                field_175336_F.biomeDepthOffset = value;
                break;

            case 114:
                field_175336_F.biomeScaleWeight = value;
                break;

            case 115:
                field_175336_F.biomeScaleOffset = value;

            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
            case 155:
            case 156:
            case 161:
            case 188:
            default:
                break;

            case 157:
                field_175336_F.dungeonChance = (int)value;
                break;

            case 158:
                field_175336_F.waterLakeChance = (int)value;
                break;

            case 159:
                field_175336_F.lavaLakeChance = (int)value;
                break;

            case 160:
                field_175336_F.seaLevel = (int)value;
                break;

            case 162:
                field_175336_F.fixedBiome = (int)value;
                break;

            case 163:
                field_175336_F.biomeSize = (int)value;
                break;

            case 164:
                field_175336_F.riverSize = (int)value;
                break;

            case 165:
                field_175336_F.dirtSize = (int)value;
                break;

            case 166:
                field_175336_F.dirtCount = (int)value;
                break;

            case 167:
                field_175336_F.dirtMinHeight = (int)value;
                break;

            case 168:
                field_175336_F.dirtMaxHeight = (int)value;
                break;

            case 169:
                field_175336_F.gravelSize = (int)value;
                break;

            case 170:
                field_175336_F.gravelCount = (int)value;
                break;

            case 171:
                field_175336_F.gravelMinHeight = (int)value;
                break;

            case 172:
                field_175336_F.gravelMaxHeight = (int)value;
                break;

            case 173:
                field_175336_F.graniteSize = (int)value;
                break;

            case 174:
                field_175336_F.graniteCount = (int)value;
                break;

            case 175:
                field_175336_F.graniteMinHeight = (int)value;
                break;

            case 176:
                field_175336_F.graniteMaxHeight = (int)value;
                break;

            case 177:
                field_175336_F.dioriteSize = (int)value;
                break;

            case 178:
                field_175336_F.dioriteCount = (int)value;
                break;

            case 179:
                field_175336_F.dioriteMinHeight = (int)value;
                break;

            case 180:
                field_175336_F.dioriteMaxHeight = (int)value;
                break;

            case 181:
                field_175336_F.andesiteSize = (int)value;
                break;

            case 182:
                field_175336_F.andesiteCount = (int)value;
                break;

            case 183:
                field_175336_F.andesiteMinHeight = (int)value;
                break;

            case 184:
                field_175336_F.andesiteMaxHeight = (int)value;
                break;

            case 185:
                field_175336_F.coalSize = (int)value;
                break;

            case 186:
                field_175336_F.coalCount = (int)value;
                break;

            case 187:
                field_175336_F.coalMinHeight = (int)value;
                break;

            case 189:
                field_175336_F.coalMaxHeight = (int)value;
                break;

            case 190:
                field_175336_F.ironSize = (int)value;
                break;

            case 191:
                field_175336_F.ironCount = (int)value;
                break;

            case 192:
                field_175336_F.ironMinHeight = (int)value;
                break;

            case 193:
                field_175336_F.ironMaxHeight = (int)value;
                break;

            case 194:
                field_175336_F.goldSize = (int)value;
                break;

            case 195:
                field_175336_F.goldCount = (int)value;
                break;

            case 196:
                field_175336_F.goldMinHeight = (int)value;
                break;

            case 197:
                field_175336_F.goldMaxHeight = (int)value;
                break;

            case 198:
                field_175336_F.redstoneSize = (int)value;
                break;

            case 199:
                field_175336_F.redstoneCount = (int)value;
                break;

            case 200:
                field_175336_F.redstoneMinHeight = (int)value;
                break;

            case 201:
                field_175336_F.redstoneMaxHeight = (int)value;
                break;

            case 202:
                field_175336_F.diamondSize = (int)value;
                break;

            case 203:
                field_175336_F.diamondCount = (int)value;
                break;

            case 204:
                field_175336_F.diamondMinHeight = (int)value;
                break;

            case 205:
                field_175336_F.diamondMaxHeight = (int)value;
                break;

            case 206:
                field_175336_F.lapisSize = (int)value;
                break;

            case 207:
                field_175336_F.lapisCount = (int)value;
                break;

            case 208:
                field_175336_F.lapisCenterHeight = (int)value;
                break;

            case 209:
                field_175336_F.lapisSpread = (int)value;
        }

        if (id >= 100 && id < 116)
        {
            Gui gui = field_175349_r.func_178061_c(id - 100 + 132);

            if (gui != null)
            {
                ((GuiTextField)gui).setText(func_175330_b(id, value));
            }
        }

        if (!field_175336_F.equals(field_175334_E))
        {
            func_181031_a(true);
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            switch (button.id)
            {
                case 300:
                    field_175343_i.chunkProviderSettingsJson = field_175336_F.toString();
                    mc.displayGuiScreen(field_175343_i);
                    break;

                case 301:
                    for (int i = 0; i < field_175349_r.getSize(); ++i)
                    {
                        GuiPageButtonList.GuiEntry guipagebuttonlist$guientry = field_175349_r.getListEntry(i);
                        Gui gui = guipagebuttonlist$guientry.func_178022_a();

                        if (gui instanceof GuiButton)
                        {
                            GuiButton guibutton = (GuiButton)gui;

                            if (guibutton instanceof GuiSlider)
                            {
                                float f = ((GuiSlider)guibutton).func_175217_d() * (0.75F + random.nextFloat() * 0.5F) + (random.nextFloat() * 0.1F - 0.05F);
                                ((GuiSlider)guibutton).func_175219_a(MathHelper.clamp_float(f, 0.0F, 1.0F));
                            }
                            else if (guibutton instanceof GuiListButton)
                            {
                                ((GuiListButton)guibutton).func_175212_b(random.nextBoolean());
                            }
                        }

                        Gui gui1 = guipagebuttonlist$guientry.func_178021_b();

                        if (gui1 instanceof GuiButton)
                        {
                            GuiButton guibutton1 = (GuiButton)gui1;

                            if (guibutton1 instanceof GuiSlider)
                            {
                                float f1 = ((GuiSlider)guibutton1).func_175217_d() * (0.75F + random.nextFloat() * 0.5F) + (random.nextFloat() * 0.1F - 0.05F);
                                ((GuiSlider)guibutton1).func_175219_a(MathHelper.clamp_float(f1, 0.0F, 1.0F));
                            }
                            else if (guibutton1 instanceof GuiListButton)
                            {
                                ((GuiListButton)guibutton1).func_175212_b(random.nextBoolean());
                            }
                        }
                    }

                    return;

                case 302:
                    field_175349_r.func_178071_h();
                    func_175328_i();
                    break;

                case 303:
                    field_175349_r.func_178064_i();
                    func_175328_i();
                    break;

                case 304:
                    if (field_175338_A)
                    {
                        func_175322_b(304);
                    }

                    break;

                case 305:
                    mc.displayGuiScreen(new GuiScreenCustomizePresets(this));
                    break;

                case 306:
                    func_175331_h();
                    break;

                case 307:
                    field_175339_B = 0;
                    func_175331_h();
            }
        }
    }

    private void func_175326_g()
    {
        field_175336_F.func_177863_a();
        func_175325_f();
        func_181031_a(false);
    }

    private void func_175322_b(int p_175322_1_)
    {
        field_175339_B = p_175322_1_;
        func_175329_a(true);
    }

    private void func_175331_h() throws IOException
    {
        switch (field_175339_B)
        {
            case 300:
                actionPerformed((GuiListButton) field_175349_r.func_178061_c(300));
                break;

            case 304:
                func_175326_g();
        }

        field_175339_B = 0;
        field_175340_C = true;
        func_175329_a(false);
    }

    private void func_175329_a(boolean p_175329_1_)
    {
        field_175352_x.visible = p_175329_1_;
        field_175351_y.visible = p_175329_1_;
        field_175347_t.enabled = !p_175329_1_;
        field_175348_s.enabled = !p_175329_1_;
        field_175345_v.enabled = !p_175329_1_;
        field_175344_w.enabled = !p_175329_1_;
        field_175346_u.enabled = field_175338_A && !p_175329_1_;
        field_175350_z.enabled = !p_175329_1_;
        field_175349_r.func_181155_a(!p_175329_1_);
    }

    private void func_175328_i()
    {
        field_175345_v.enabled = field_175349_r.func_178059_e() != 0;
        field_175344_w.enabled = field_175349_r.func_178059_e() != field_175349_r.func_178057_f() - 1;
        field_175333_f = I18n.format("book.pageIndicator", Integer.valueOf(field_175349_r.func_178059_e() + 1), Integer.valueOf(field_175349_r.func_178057_f()));
        field_175335_g = field_175342_h[field_175349_r.func_178059_e()];
        field_175347_t.enabled = field_175349_r.func_178059_e() != field_175349_r.func_178057_f() - 1;
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);

        if (field_175339_B == 0)
        {
            switch (keyCode)
            {
                case 200:
                    func_175327_a(1.0F);
                    break;

                case 208:
                    func_175327_a(-1.0F);
                    break;

                default:
                    field_175349_r.func_178062_a(typedChar, keyCode);
            }
        }
    }

    private void func_175327_a(float p_175327_1_)
    {
        Gui gui = field_175349_r.func_178056_g();

        if (gui instanceof GuiTextField)
        {
            float f = p_175327_1_;

            if (GuiScreen.isShiftKeyDown())
            {
                f = p_175327_1_ * 0.1F;

                if (GuiScreen.isCtrlKeyDown())
                {
                    f *= 0.1F;
                }
            }
            else if (GuiScreen.isCtrlKeyDown())
            {
                f = p_175327_1_ * 10.0F;

                if (GuiScreen.isAltKeyDown())
                {
                    f *= 10.0F;
                }
            }

            GuiTextField guitextfield = (GuiTextField)gui;
            Float f1 = Floats.tryParse(guitextfield.getText());

            if (f1 != null)
            {
                f1 = Float.valueOf(f1.floatValue() + f);
                int i = guitextfield.getId();
                String s = func_175330_b(guitextfield.getId(), f1.floatValue());
                guitextfield.setText(s);
                func_175319_a(i, s);
            }
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (field_175339_B == 0 && !field_175340_C)
        {
            field_175349_r.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * Called when a mouse button is released.  Args : mouseX, mouseY, releaseButton
     */
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (field_175340_C)
        {
            field_175340_C = false;
        }
        else if (field_175339_B == 0)
        {
            field_175349_r.mouseReleased(mouseX, mouseY, state);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        field_175349_r.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, field_175341_a, width / 2, 2, 16777215);
        drawCenteredString(fontRendererObj, field_175333_f, width / 2, 12, 16777215);
        drawCenteredString(fontRendererObj, field_175335_g, width / 2, 22, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (field_175339_B != 0)
        {
            Gui.drawRect(0, 0, width, height, Integer.MIN_VALUE);
            drawHorizontalLine(width / 2 - 91, width / 2 + 90, 99, -2039584);
            drawHorizontalLine(width / 2 - 91, width / 2 + 90, 185, -6250336);
            drawVerticalLine(width / 2 - 91, 99, 185, -2039584);
            drawVerticalLine(width / 2 + 90, 99, 185, -6250336);
            float f = 85.0F;
            float f1 = 180.0F;
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            mc.getTextureManager().bindTexture(Gui.optionsBackground);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            float f2 = 32.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos(width / 2 - 90, 185.0D, 0.0D).tex(0.0D, 2.65625D).color(64, 64, 64, 64).endVertex();
            worldrenderer.pos(width / 2 + 90, 185.0D, 0.0D).tex(5.625D, 2.65625D).color(64, 64, 64, 64).endVertex();
            worldrenderer.pos(width / 2 + 90, 100.0D, 0.0D).tex(5.625D, 0.0D).color(64, 64, 64, 64).endVertex();
            worldrenderer.pos(width / 2 - 90, 100.0D, 0.0D).tex(0.0D, 0.0D).color(64, 64, 64, 64).endVertex();
            tessellator.draw();
            drawCenteredString(fontRendererObj, I18n.format("createWorld.customize.custom.confirmTitle"), width / 2, 105, 16777215);
            drawCenteredString(fontRendererObj, I18n.format("createWorld.customize.custom.confirm1"), width / 2, 125, 16777215);
            drawCenteredString(fontRendererObj, I18n.format("createWorld.customize.custom.confirm2"), width / 2, 135, 16777215);
            field_175352_x.drawButton(mc, mouseX, mouseY);
            field_175351_y.drawButton(mc, mouseX, mouseY);
        }
    }
}
