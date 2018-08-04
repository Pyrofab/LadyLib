package ladylib.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import ladylib.LadyLib;
import ladylib.installer.ModEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = LadyLib.MOD_ID, value = Side.CLIENT)
public class GuiModInstaller extends GuiScreen {
    private static final int BUTTON_ID = 88037256;

    @SubscribeEvent
    public static void onGuiScreenInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen gui = event.getGui();

        if (gui instanceof GuiMainMenu) {
            if (ModEntry.getLadysnakeMods().isEmpty()) {
                return;
            }
            String target = I18n.format("fml.menu.mods");

            List<GuiButton> buttons = event.getButtonList();
            for (GuiButton b : buttons)
                if (target.equals(b.displayString)) {
                    GuiButton installer = new GuiButton(BUTTON_ID, b.x + (-24), b.y, 20, 20, "LS");
                    // check for colliding buttons
                    boolean collision = false;
                    // we are only moving along the x axis, we shouldn't have to check buttons that are not on the same line
                    List<GuiButton> possibleCollisions = buttons.stream().filter(other -> other.y < installer.y + installer.height && other.y + other.height > installer.y).collect(Collectors.toList());
                    do {
                        for (GuiButton other : possibleCollisions) {
                            if (other.x < installer.x + installer.width && other.x + other.width > installer.x) {
                                installer.x = other.x - installer.width;
                                collision = true;
                            }
                        }
                    } while (collision);
                    buttons.add(installer);
                    return;
                }
        }
    }

    @SubscribeEvent
    public static void onGuiScreenActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.getGui() instanceof GuiMainMenu && event.getButton().id == BUTTON_ID) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiModInstaller(event.getGui()));
        }
    }

    /**
     * reee forge stop making your useful classes private
     */
    public enum SortType implements Comparator<ModEntry> {
        NORMAL(24),
        A_TO_Z(25){ @Override protected int compare(String name1, String name2){ return name1.compareTo(name2); }},
        Z_TO_A(26){ @Override protected int compare(String name1, String name2){ return name2.compareTo(name1); }};

        private int buttonID;

        SortType(int buttonID) {
            this.buttonID = buttonID;
        }

        @Nullable
        public static SortType getTypeForButton(GuiButton button) {
            for (SortType t : values()) {
                if (t.buttonID == button.id) {
                    return t;
                }
            }
            return null;
        }

        protected int compare(String name1, String name2){ return 0; }

        @Override
        public int compare(ModEntry o1, ModEntry o2) {
            String name1 = StringUtils.stripControlCodes(o1.getName()).toLowerCase();
            String name2 = StringUtils.stripControlCodes(o2.getName()).toLowerCase();
            return compare(name1, name2);
        }
    }

    private GuiScreen mainMenu;
    private GuiInstallerModList modList;

    private int buttonMargin = 1;
    private int numButtons = SortType.values().length;

    private String lastFilterText = "";
    private String hoveringText;

    private GuiTextField search;
    private boolean sorted = false;
    private SortType sortType = SortType.NORMAL;

    public GuiModInstaller(GuiScreen mainMenu) {
        this.mainMenu = mainMenu;
    }

    @Override
    public void initGui() {
        // Mod list
        this.modList = new GuiInstallerModList(this, ModEntry.getLadysnakeMods(), this.width - 30, 35);

        // "Done" button
        this.buttonList.add(new GuiButton(6, ((this.width) / 2) - 100, this.height - 38, I18n.format("gui.done")));

        // Search bar
        search = new GuiTextField(0, fontRenderer, 12 + 3 * this.modList.getListWidth() / 4, this.modList.getTop() - 17, this.modList.getListWidth() / 4 - 4, 14);
        search.setFocused(true);
        search.setCanLoseFocus(true);

        // Sorting buttons
        int width = (this.modList.getListWidth() / 3 / numButtons);
        int x = 10, y = 10;
        GuiButton normalSort = new GuiButton(SortType.NORMAL.buttonID, x, y, width - buttonMargin, 20, I18n.format("fml.menu.mods.normal"));
        normalSort.enabled = false;
        buttonList.add(normalSort);
        x += width + buttonMargin;
        GuiButton alphabeticalSort = new GuiButton(SortType.A_TO_Z.buttonID, x, y, width - buttonMargin, 20, "A-Z");
        buttonList.add(alphabeticalSort);
        x += width + buttonMargin;
        buttonList.add(new GuiButton(SortType.Z_TO_A.buttonID, x, y, width - buttonMargin, 20, "Z-A"));
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        search.mouseClicked(x, y, button);
        if (button == 1 && x >= search.x && x < search.x + search.width && y >= search.y && y < search.y + search.height) {
            search.setText("");
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    @Override
    protected void keyTyped(char c, int keyCode) throws IOException {
        super.keyTyped(c, keyCode);
        search.textboxKeyTyped(c, keyCode);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen() {
        super.updateScreen();
        search.updateCursorCounter();

        if (!search.getText().equals(lastFilterText) || !sorted) {
            modList.reloadMods();
            sorted = true;
        }

        lastFilterText = search.getText();
    }

    public String getSearchText() {
        return search.getText().toLowerCase(Locale.ENGLISH);
    }

    public SortType getSortType() {
        return sortType;
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.modList.drawScreen(mouseX, mouseY, partialTicks);
        int left = ((this.width - 38) / 2) + 30;
        this.drawCenteredString(this.fontRenderer, "Mod List", left, 16, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);

        String text = I18n.format("fml.menu.mods.search");
        int x = (/*(10 +*/ search.x - 5/* + width) / 2*/) - (getFontRenderer().getStringWidth(text)/* / 2*/);
        getFontRenderer().drawString(text, x, search.y + 5, 0xFFFFFF);
        search.drawTextBox();
        if (hoveringText != null) {
            GuiUtils.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(this.hoveringText)), mouseX, mouseY, width, height, -1, fontRenderer);
            hoveringText = null;
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            SortType type = SortType.getTypeForButton(button);

            if (type != null) {
                for (GuiButton b : buttonList) {
                    if (SortType.getTypeForButton(b) != null) {
                        b.enabled = true;
                    }
                }
                button.enabled = false;
                sorted = false;
                sortType = type;
            } else {
                switch (button.id) {
                    case 6: {
                        this.mc.displayGuiScreen(this.mainMenu);
                        return;
                    }
                }
            }
        }
        super.actionPerformed(button);
    }

    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();
        this.modList.handleMouseInput(mouseX, mouseY);
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void setHoveringText(String hoveringText) {
        this.hoveringText = hoveringText;
    }
}
