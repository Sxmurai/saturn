package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class GuiScreenBook extends GuiScreen
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");

    /** The player editing the book */
    private final EntityPlayer editingPlayer;
    private final ItemStack bookObj;

    /** Whether the book is signed or can still be edited */
    private final boolean bookIsUnsigned;

    /**
     * Whether the book's title or contents has been modified since being opened
     */
    private boolean bookIsModified;

    /** Determines if the signing screen is open */
    private boolean bookGettingSigned;

    /** Update ticks since the gui was opened */
    private int updateCount;
    private final int bookImageWidth = 192;
    private final int bookImageHeight = 192;
    private int bookTotalPages = 1;
    private int currPage;
    private NBTTagList bookPages;
    private String bookTitle = "";
    private List<IChatComponent> field_175386_A;
    private int field_175387_B = -1;
    private GuiScreenBook.NextPageButton buttonNextPage;
    private GuiScreenBook.NextPageButton buttonPreviousPage;
    private GuiButton buttonDone;

    /** The GuiButton to sign this book. */
    private GuiButton buttonSign;
    private GuiButton buttonFinalize;
    private GuiButton buttonCancel;

    public GuiScreenBook(EntityPlayer player, ItemStack book, boolean isUnsigned)
    {
        editingPlayer = player;
        bookObj = book;
        bookIsUnsigned = isUnsigned;

        if (book.hasTagCompound())
        {
            NBTTagCompound nbttagcompound = book.getTagCompound();
            bookPages = nbttagcompound.getTagList("pages", 8);

            if (bookPages != null)
            {
                bookPages = (NBTTagList) bookPages.copy();
                bookTotalPages = bookPages.tagCount();

                if (bookTotalPages < 1)
                {
                    bookTotalPages = 1;
                }
            }
        }

        if (bookPages == null && isUnsigned)
        {
            bookPages = new NBTTagList();
            bookPages.appendTag(new NBTTagString(""));
            bookTotalPages = 1;
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
        ++updateCount;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        if (bookIsUnsigned)
        {
            buttonList.add(buttonSign = new GuiButton(3, width / 2 - 100, 4 + bookImageHeight, 98, 20, I18n.format("book.signButton")));
            buttonList.add(buttonDone = new GuiButton(0, width / 2 + 2, 4 + bookImageHeight, 98, 20, I18n.format("gui.done")));
            buttonList.add(buttonFinalize = new GuiButton(5, width / 2 - 100, 4 + bookImageHeight, 98, 20, I18n.format("book.finalizeButton")));
            buttonList.add(buttonCancel = new GuiButton(4, width / 2 + 2, 4 + bookImageHeight, 98, 20, I18n.format("gui.cancel")));
        }
        else
        {
            buttonList.add(buttonDone = new GuiButton(0, width / 2 - 100, 4 + bookImageHeight, 200, 20, I18n.format("gui.done")));
        }

        int i = (width - bookImageWidth) / 2;
        int j = 2;
        buttonList.add(buttonNextPage = new GuiScreenBook.NextPageButton(1, i + 120, j + 154, true));
        buttonList.add(buttonPreviousPage = new GuiScreenBook.NextPageButton(2, i + 38, j + 154, false));
        updateButtons();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    private void updateButtons()
    {
        buttonNextPage.visible = !bookGettingSigned && (currPage < bookTotalPages - 1 || bookIsUnsigned);
        buttonPreviousPage.visible = !bookGettingSigned && currPage > 0;
        buttonDone.visible = !bookIsUnsigned || !bookGettingSigned;

        if (bookIsUnsigned)
        {
            buttonSign.visible = !bookGettingSigned;
            buttonCancel.visible = bookGettingSigned;
            buttonFinalize.visible = bookGettingSigned;
            buttonFinalize.enabled = bookTitle.trim().length() > 0;
        }
    }

    private void sendBookToServer(boolean publish) throws IOException
    {
        if (bookIsUnsigned && bookIsModified)
        {
            if (bookPages != null)
            {
                while (bookPages.tagCount() > 1)
                {
                    String s = bookPages.getStringTagAt(bookPages.tagCount() - 1);

                    if (s.length() != 0)
                    {
                        break;
                    }

                    bookPages.removeTag(bookPages.tagCount() - 1);
                }

                if (bookObj.hasTagCompound())
                {
                    NBTTagCompound nbttagcompound = bookObj.getTagCompound();
                    nbttagcompound.setTag("pages", bookPages);
                }
                else
                {
                    bookObj.setTagInfo("pages", bookPages);
                }

                String s2 = "MC|BEdit";

                if (publish)
                {
                    s2 = "MC|BSign";
                    bookObj.setTagInfo("author", new NBTTagString(editingPlayer.getName()));
                    bookObj.setTagInfo("title", new NBTTagString(bookTitle.trim()));

                    for (int i = 0; i < bookPages.tagCount(); ++i)
                    {
                        String s1 = bookPages.getStringTagAt(i);
                        IChatComponent ichatcomponent = new ChatComponentText(s1);
                        s1 = IChatComponent.Serializer.componentToJson(ichatcomponent);
                        bookPages.set(i, new NBTTagString(s1));
                    }

                    bookObj.setItem(Items.written_book);
                }

                PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
                packetbuffer.writeItemStackToBuffer(bookObj);
                mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload(s2, packetbuffer));
            }
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
                mc.displayGuiScreen(null);
                sendBookToServer(false);
            }
            else if (button.id == 3 && bookIsUnsigned)
            {
                bookGettingSigned = true;
            }
            else if (button.id == 1)
            {
                if (currPage < bookTotalPages - 1)
                {
                    ++currPage;
                }
                else if (bookIsUnsigned)
                {
                    addNewPage();

                    if (currPage < bookTotalPages - 1)
                    {
                        ++currPage;
                    }
                }
            }
            else if (button.id == 2)
            {
                if (currPage > 0)
                {
                    --currPage;
                }
            }
            else if (button.id == 5 && bookGettingSigned)
            {
                sendBookToServer(true);
                mc.displayGuiScreen(null);
            }
            else if (button.id == 4 && bookGettingSigned)
            {
                bookGettingSigned = false;
            }

            updateButtons();
        }
    }

    private void addNewPage()
    {
        if (bookPages != null && bookPages.tagCount() < 50)
        {
            bookPages.appendTag(new NBTTagString(""));
            ++bookTotalPages;
            bookIsModified = true;
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);

        if (bookIsUnsigned)
        {
            if (bookGettingSigned)
            {
                keyTypedInTitle(typedChar, keyCode);
            }
            else
            {
                keyTypedInBook(typedChar, keyCode);
            }
        }
    }

    /**
     * Processes keystrokes when editing the text of a book
     */
    private void keyTypedInBook(char typedChar, int keyCode)
    {
        if (GuiScreen.isKeyComboCtrlV(keyCode))
        {
            pageInsertIntoCurrent(GuiScreen.getClipboardString());
        }
        else
        {
            switch (keyCode)
            {
                case 14:
                    String s = pageGetCurrent();

                    if (s.length() > 0)
                    {
                        pageSetCurrent(s.substring(0, s.length() - 1));
                    }

                    return;

                case 28:
                case 156:
                    pageInsertIntoCurrent("\n");
                    return;

                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar))
                    {
                        pageInsertIntoCurrent(Character.toString(typedChar));
                    }
            }
        }
    }

    /**
     * Processes keystrokes when editing the title of a book
     */
    private void keyTypedInTitle(char p_146460_1_, int p_146460_2_) throws IOException
    {
        switch (p_146460_2_)
        {
            case 14:
                if (!bookTitle.isEmpty())
                {
                    bookTitle = bookTitle.substring(0, bookTitle.length() - 1);
                    updateButtons();
                }

                return;

            case 28:
            case 156:
                if (!bookTitle.isEmpty())
                {
                    sendBookToServer(true);
                    mc.displayGuiScreen(null);
                }

                return;

            default:
                if (bookTitle.length() < 16 && ChatAllowedCharacters.isAllowedCharacter(p_146460_1_))
                {
                    bookTitle = bookTitle + p_146460_1_;
                    updateButtons();
                    bookIsModified = true;
                }
        }
    }

    /**
     * Returns the entire text of the current page as determined by currPage
     */
    private String pageGetCurrent()
    {
        return bookPages != null && currPage >= 0 && currPage < bookPages.tagCount() ? bookPages.getStringTagAt(currPage) : "";
    }

    /**
     * Sets the text of the current page as determined by currPage
     */
    private void pageSetCurrent(String p_146457_1_)
    {
        if (bookPages != null && currPage >= 0 && currPage < bookPages.tagCount())
        {
            bookPages.set(currPage, new NBTTagString(p_146457_1_));
            bookIsModified = true;
        }
    }

    /**
     * Processes any text getting inserted into the current page, enforcing the page size limit
     */
    private void pageInsertIntoCurrent(String p_146459_1_)
    {
        String s = pageGetCurrent();
        String s1 = s + p_146459_1_;
        int i = fontRendererObj.splitStringWidth(s1 + "" + EnumChatFormatting.BLACK + "_", 118);

        if (i <= 128 && s1.length() < 256)
        {
            pageSetCurrent(s1);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(GuiScreenBook.bookGuiTextures);
        int i = (width - bookImageWidth) / 2;
        int j = 2;
        drawTexturedModalRect(i, j, 0, 0, bookImageWidth, bookImageHeight);

        if (bookGettingSigned)
        {
            String s = bookTitle;

            if (bookIsUnsigned)
            {
                if (updateCount / 6 % 2 == 0)
                {
                    s = s + "" + EnumChatFormatting.BLACK + "_";
                }
                else
                {
                    s = s + "" + EnumChatFormatting.GRAY + "_";
                }
            }

            String s1 = I18n.format("book.editTitle");
            int k = fontRendererObj.getStringWidth(s1);
            fontRendererObj.drawString(s1, i + 36 + (116 - k) / 2, j + 16 + 16, 0);
            int l = fontRendererObj.getStringWidth(s);
            fontRendererObj.drawString(s, i + 36 + (116 - l) / 2, j + 48, 0);
            String s2 = I18n.format("book.byAuthor", editingPlayer.getName());
            int i1 = fontRendererObj.getStringWidth(s2);
            fontRendererObj.drawString(EnumChatFormatting.DARK_GRAY + s2, i + 36 + (116 - i1) / 2, j + 48 + 10, 0);
            String s3 = I18n.format("book.finalizeWarning");
            fontRendererObj.drawSplitString(s3, i + 36, j + 80, 116, 0);
        }
        else
        {
            String s4 = I18n.format("book.pageIndicator", Integer.valueOf(currPage + 1), Integer.valueOf(bookTotalPages));
            String s5 = "";

            if (bookPages != null && currPage >= 0 && currPage < bookPages.tagCount())
            {
                s5 = bookPages.getStringTagAt(currPage);
            }

            if (bookIsUnsigned)
            {
                if (fontRendererObj.getBidiFlag())
                {
                    s5 = s5 + "_";
                }
                else if (updateCount / 6 % 2 == 0)
                {
                    s5 = s5 + "" + EnumChatFormatting.BLACK + "_";
                }
                else
                {
                    s5 = s5 + "" + EnumChatFormatting.GRAY + "_";
                }
            }
            else if (field_175387_B != currPage)
            {
                if (ItemEditableBook.validBookTagContents(bookObj.getTagCompound()))
                {
                    try
                    {
                        IChatComponent ichatcomponent = IChatComponent.Serializer.jsonToComponent(s5);
                        field_175386_A = ichatcomponent != null ? GuiUtilRenderComponents.func_178908_a(ichatcomponent, 116, fontRendererObj, true, true) : null;
                    }
                    catch (JsonParseException var13)
                    {
                        field_175386_A = null;
                    }
                }
                else
                {
                    ChatComponentText chatcomponenttext = new ChatComponentText(EnumChatFormatting.DARK_RED + "* Invalid book tag *");
                    field_175386_A = Lists.newArrayList(chatcomponenttext);
                }

                field_175387_B = currPage;
            }

            int j1 = fontRendererObj.getStringWidth(s4);
            fontRendererObj.drawString(s4, i - j1 + bookImageWidth - 44, j + 16, 0);

            if (field_175386_A == null)
            {
                fontRendererObj.drawSplitString(s5, i + 36, j + 16 + 16, 116, 0);
            }
            else
            {
                int k1 = Math.min(128 / fontRendererObj.FONT_HEIGHT, field_175386_A.size());

                for (int l1 = 0; l1 < k1; ++l1)
                {
                    IChatComponent ichatcomponent2 = field_175386_A.get(l1);
                    fontRendererObj.drawString(ichatcomponent2.getUnformattedText(), i + 36, j + 16 + 16 + l1 * fontRendererObj.FONT_HEIGHT, 0);
                }

                IChatComponent ichatcomponent1 = func_175385_b(mouseX, mouseY);

                if (ichatcomponent1 != null)
                {
                    handleComponentHover(ichatcomponent1, mouseX, mouseY);
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (mouseButton == 0)
        {
            IChatComponent ichatcomponent = func_175385_b(mouseX, mouseY);

            if (handleComponentClick(ichatcomponent))
            {
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Executes the click event specified by the given chat component
     */
    protected boolean handleComponentClick(IChatComponent p_175276_1_)
    {
        ClickEvent clickevent = p_175276_1_ == null ? null : p_175276_1_.getChatStyle().getChatClickEvent();

        if (clickevent == null)
        {
            return false;
        }
        else if (clickevent.getAction() == ClickEvent.Action.CHANGE_PAGE)
        {
            String s = clickevent.getValue();

            try
            {
                int i = Integer.parseInt(s) - 1;

                if (i >= 0 && i < bookTotalPages && i != currPage)
                {
                    currPage = i;
                    updateButtons();
                    return true;
                }
            }
            catch (Throwable var5)
            {
            }

            return false;
        }
        else
        {
            boolean flag = super.handleComponentClick(p_175276_1_);

            if (flag && clickevent.getAction() == ClickEvent.Action.RUN_COMMAND)
            {
                mc.displayGuiScreen(null);
            }

            return flag;
        }
    }

    public IChatComponent func_175385_b(int p_175385_1_, int p_175385_2_)
    {
        if (field_175386_A == null)
        {
            return null;
        }
        else
        {
            int i = p_175385_1_ - (width - bookImageWidth) / 2 - 36;
            int j = p_175385_2_ - 2 - 16 - 16;

            if (i >= 0 && j >= 0)
            {
                int k = Math.min(128 / fontRendererObj.FONT_HEIGHT, field_175386_A.size());

                if (i <= 116 && j < mc.fontRendererObj.FONT_HEIGHT * k + k)
                {
                    int l = j / mc.fontRendererObj.FONT_HEIGHT;

                    if (l >= 0 && l < field_175386_A.size())
                    {
                        IChatComponent ichatcomponent = field_175386_A.get(l);
                        int i1 = 0;

                        for (IChatComponent ichatcomponent1 : ichatcomponent)
                        {
                            if (ichatcomponent1 instanceof ChatComponentText)
                            {
                                i1 += mc.fontRendererObj.getStringWidth(((ChatComponentText)ichatcomponent1).getChatComponentText_TextValue());

                                if (i1 > i)
                                {
                                    return ichatcomponent1;
                                }
                            }
                        }
                    }

                    return null;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    static class NextPageButton extends GuiButton
    {
        private final boolean field_146151_o;

        public NextPageButton(int p_i46316_1_, int p_i46316_2_, int p_i46316_3_, boolean p_i46316_4_)
        {
            super(p_i46316_1_, p_i46316_2_, p_i46316_3_, 23, 13, "");
            field_146151_o = p_i46316_4_;
        }

        public void drawButton(Minecraft mc, int mouseX, int mouseY)
        {
            if (visible)
            {
                boolean flag = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(GuiScreenBook.bookGuiTextures);
                int i = 0;
                int j = 192;

                if (flag)
                {
                    i += 23;
                }

                if (!field_146151_o)
                {
                    j += 13;
                }

                drawTexturedModalRect(xPosition, yPosition, i, j, 23, 13);
            }
        }
    }
}
