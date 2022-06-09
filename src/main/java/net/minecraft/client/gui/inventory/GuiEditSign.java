package net.minecraft.client.gui.inventory;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

public class GuiEditSign extends GuiScreen
{
    /** Reference to the sign object. */
    private final TileEntitySign tileSign;

    /** Counts the number of screen updates. */
    private int updateCounter;

    /** The index of the line that is being edited. */
    private int editLine;

    /** "Done" button for the GUI. */
    private GuiButton doneBtn;

    public GuiEditSign(TileEntitySign teSign)
    {
        tileSign = teSign;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        buttonList.add(doneBtn = new GuiButton(0, width / 2 - 100, height / 4 + 120, I18n.format("gui.done")));
        tileSign.setEditable(false);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        NetHandlerPlayClient nethandlerplayclient = mc.getNetHandler();

        if (nethandlerplayclient != null)
        {
            nethandlerplayclient.addToSendQueue(new C12PacketUpdateSign(tileSign.getPos(), tileSign.signText));
        }

        tileSign.setEditable(true);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        ++updateCounter;
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
                tileSign.markDirty();
                mc.displayGuiScreen(null);
            }
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 200)
        {
            editLine = editLine - 1 & 3;
        }

        if (keyCode == 208 || keyCode == 28 || keyCode == 156)
        {
            editLine = editLine + 1 & 3;
        }

        String s = tileSign.signText[editLine].getUnformattedText();

        if (keyCode == 14 && s.length() > 0)
        {
            s = s.substring(0, s.length() - 1);
        }

        if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && fontRendererObj.getStringWidth(s + typedChar) <= 90)
        {
            s = s + typedChar;
        }

        tileSign.signText[editLine] = new ChatComponentText(s);

        if (keyCode == 1)
        {
            actionPerformed(doneBtn);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("sign.edit"), width / 2, 40, 16777215);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)(width / 2), 0.0F, 50.0F);
        float f = 93.75F;
        GlStateManager.scale(-f, -f, -f);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        Block block = tileSign.getBlockType();

        if (block == Blocks.standing_sign)
        {
            float f1 = (float)(tileSign.getBlockMetadata() * 360) / 16.0F;
            GlStateManager.rotate(f1, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -1.0625F, 0.0F);
        }
        else
        {
            int i = tileSign.getBlockMetadata();
            float f2 = 0.0F;

            if (i == 2)
            {
                f2 = 180.0F;
            }

            if (i == 4)
            {
                f2 = 90.0F;
            }

            if (i == 5)
            {
                f2 = -90.0F;
            }

            GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -1.0625F, 0.0F);
        }

        if (updateCounter / 6 % 2 == 0)
        {
            tileSign.lineBeingEdited = editLine;
        }

        TileEntityRendererDispatcher.instance.renderTileEntityAt(tileSign, -0.5D, -0.75D, -0.5D, 0.0F);
        tileSign.lineBeingEdited = -1;
        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
