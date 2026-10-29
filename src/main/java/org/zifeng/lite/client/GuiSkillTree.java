package org.zifeng.lite.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.zifeng.lite.ClientSkillState;
import org.zifeng.lite.Skill;
import org.zifeng.lite.network.PacketAction;
import org.zifeng.lite.network.PacketHandler;

/** 1.7.10 原生技能树界面：五行走廊，无技能点、无 HUD。 */
@SideOnly(Side.CLIENT)
public class GuiSkillTree extends GuiScreen {
    private static final int PANEL_W = 380;
    private static final int PANEL_H = 292;
    private static final int ROW_H = 40;
    private static final int ROW_START = 54;

    private int left;
    private int top;

    private static final int C_PANEL = 0xFF101A2E;
    private static final int C_ROW = 0xFF16233B;
    private static final int C_ROW_HOVER = 0xFF1E3150;
    private static final int C_BORDER = 0xFF3A6EA5;
    private static final int C_TEXT = 0xFFFFFFFF;
    private static final int C_MUTED = 0xFF9FB3C8;
    private static final int C_GREEN = 0xFF55FF55;
    private static final int C_BLUE = 0xFF78A8D8;
    private static final int C_GOLD = 0xFFFFC845;
    private static final int C_PURPLE = 0xFFAA78FF;
    private static final int C_RED = 0xFFE06060;

    @Override
    public void initGui() {
        super.initGui();
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        Gui.drawRect(0, 0, width, height, 0xC0080B16);

        Gui.drawRect(left, top, left + PANEL_W, top + PANEL_H, C_PANEL);
        drawBorder(left, top, left + PANEL_W, top + PANEL_H, C_BORDER);
        Gui.drawRect(left + 1, top + 1, left + PANEL_W - 1, top + 3, C_BLUE);

        drawCenteredString(fontRendererObj, stat("gui.zifeng_lite.title"),
                width / 2, top + 11, C_TEXT);
        drawCenteredString(fontRendererObj, stat("gui.zifeng_lite.subtitle"),
                width / 2, top + 27, C_GOLD);
        Gui.drawRect(left + 16, top + 45, left + PANEL_W - 16, top + 46, 0xFF2A4162);

        if (mouseX >= left && mouseX <= left + PANEL_W && mouseY >= top && mouseY <= top + PANEL_H) {
            // hover row
            int row = (mouseY - (top + ROW_START)) / ROW_H;
            if (row >= 0 && row < Skill.values().length) {
                int rowY = top + ROW_START + row * ROW_H;
                if (mouseY >= rowY && mouseY < rowY + ROW_H - 4) {
                    Gui.drawRect(left + 8, rowY, left + PANEL_W - 8, rowY + ROW_H - 4, C_ROW_HOVER);
                }
            }
        }

        for (int i = 0; i < Skill.values().length; i++) {
            drawSkillRow(i, mouseX, mouseY);
        }

        drawCenteredString(fontRendererObj, stat("gui.zifeng_lite.footer"),
                width / 2, top + PANEL_H - 26, C_MUTED);
        drawCenteredString(fontRendererObj, stat("gui.zifeng_lite.footer2"),
                width / 2, top + PANEL_H - 14, 0xFF6F86A3);
    }

    private void drawSkillRow(int index, int mouseX, int mouseY) {
        Skill skill = Skill.values()[index];
        int rowY = top + ROW_START + index * ROW_H;
        int x = left + 8;
        int right = left + PANEL_W - 8;
        boolean enabled = ClientSkillState.isEnabled(skill);
        int accent = accentFor(skill);

        Gui.drawRect(x, rowY, right, rowY + ROW_H - 4, C_ROW);
        Gui.drawRect(x, rowY, x + 4, rowY + ROW_H - 4, accent);

        // 左侧小图标（纯色方块 + 内框）
        int icon = rowY + 8;
        Gui.drawRect(x + 12, icon, x + 36, icon + 24, 0xFF0C1424);
        drawBorder(x + 12, icon, x + 36, icon + 24, accent);
        Gui.drawRect(x + 18, icon + 6, x + 30, icon + 18, accent);

        String name = stat(skill.getNameKey());
        fontRendererObj.drawStringWithShadow(name, x + 46, rowY + 7, enabled ? C_TEXT : C_MUTED);
        fontRendererObj.drawString(stat(skill.getDescKey()), x + 46, rowY + 21, 0xFF8FA4BC);

        String key = "[" + ClientKeyHandler.keyName(keyFor(skill)) + "]";
        int keyW = fontRendererObj.getStringWidth(key);
        fontRendererObj.drawString(key, right - keyW - 6, rowY + 8, C_BLUE);

        String toggleText = stat(enabled ? "gui.zifeng_lite.on" : "gui.zifeng_lite.off");
        int toggleX = right - 6 - 36;
        int toggleY = rowY + 21;

        if (skill == Skill.FLY_SPEED) {
            int level = ClientSkillState.getFlySpeedLevel();
            String levelText = stat("gui.zifeng_lite.level") + " " + level;
            int levelW = fontRendererObj.getStringWidth(levelText);
            int levelX = toggleX - 8 - levelW;
            fontRendererObj.drawString(levelText, levelX, rowY + 21,
                    enabled ? C_GOLD : C_MUTED);

            int plusX = levelX - 26;
            int minusX = plusX - 24;
            drawSmallButton(minusX, rowY + 21, "-", mouseX, mouseY, enabled);
            drawSmallButton(plusX, rowY + 21, "+", mouseX, mouseY, enabled);
        }

        drawToggleButton(toggleX, toggleY, 36, 18, toggleText, enabled);
    }

    private void drawToggleButton(int x, int y, int w, int h, String text, boolean enabled) {
        int color = enabled ? C_GREEN : C_RED;
        Gui.drawRect(x, y, x + w, y + h, enabled ? 0xFF173A24 : 0xFF3A1717);
        drawBorder(x, y, x + w, y + h, color);
        int textW = fontRendererObj.getStringWidth(text);
        fontRendererObj.drawString(text, x + (w - textW) / 2, y + 5, color);
    }

    private void drawSmallButton(int x, int y, String label, int mouseX, int mouseY, boolean enabled) {
        boolean hover = mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 18;
        int border = enabled ? C_GOLD : 0xFF4A5A70;
        Gui.drawRect(x, y, x + 20, y + 18, hover ? 0xFF263C5A : 0xFF0C1424);
        drawBorder(x, y, x + 20, y + 18, border);
        fontRendererObj.drawString(label, x + 8, y + 5, border);
    }

    private void drawBorder(int x1, int y1, int x2, int y2, int color) {
        Gui.drawRect(x1, y1, x2, y1 + 1, color);
        Gui.drawRect(x1, y2 - 1, x2, y2, color);
        Gui.drawRect(x1, y1, x1 + 1, y2, color);
        Gui.drawRect(x2 - 1, y1, x2, y2, color);
    }

    private int accentFor(Skill skill) {
        switch (skill) {
            case UNBREAKABLE:
            case NIGHT_VISION:
                return C_GREEN;
            case FLIGHT:
                return C_BLUE;
            case SATURATION:
                return C_GOLD;
            case FLY_SPEED:
                return C_PURPLE;
            default:
                return C_BLUE;
        }
    }

    private KeyBinding keyFor(Skill skill) {
        switch (skill) {
            case UNBREAKABLE:
                return ClientKeyHandler.UNBREAKABLE;
            case FLIGHT:
                return ClientKeyHandler.FLIGHT;
            case SATURATION:
                return ClientKeyHandler.SATURATION;
            case FLY_SPEED:
                return ClientKeyHandler.FLY_SPEED;
            case NIGHT_VISION:
                return ClientKeyHandler.NIGHT_VISION;
            default:
                return null;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }
        int row = (mouseY - (top + ROW_START)) / ROW_H;
        if (row < 0 || row >= Skill.values().length) {
            return;
        }
        int rowY = top + ROW_START + row * ROW_H;
        if (mouseY < rowY || mouseY >= rowY + ROW_H - 4) {
            return;
        }
        Skill skill = Skill.values()[row];
        int right = left + PANEL_W - 8;
        int toggleX = right - 6 - 36;
        int toggleY = rowY + 21;

        if (skill == Skill.FLY_SPEED) {
            String levelText = stat("gui.zifeng_lite.level") + " "
                    + ClientSkillState.getFlySpeedLevel();
            int levelW = fontRendererObj.getStringWidth(levelText);
            int levelX = toggleX - 8 - levelW;
            int plusX = levelX - 26;
            int minusX = plusX - 24;
            if (mouseY >= rowY + 21 && mouseY < rowY + 39) {
                int step = levelStep();
                if (mouseX >= minusX && mouseX < minusX + 20) {
                    sendLevel(-step);
                    return;
                }
                if (mouseX >= plusX && mouseX < plusX + 20) {
                    sendLevel(step);
                    return;
                }
            }
        }

        // 只有点中右侧开关框才切换，点行内其它位置不触发。
        if (mouseX >= toggleX && mouseX < toggleX + 36
                && mouseY >= toggleY && mouseY < toggleY + 18) {
            PacketHandler.INSTANCE.sendToServer(new PacketAction(skill, PacketAction.TOGGLE, 0));
        }
    }

    private void sendLevel(int amount) {
        PacketHandler.INSTANCE.sendToServer(
                new PacketAction(Skill.FLY_SPEED, PacketAction.ADD_LEVEL, amount));
    }

    /** 普通 +1，Shift +10，Ctrl +100。 */
    private int levelStep() {
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            return 100;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            return 10;
        }
        return 1;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_N || keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private String stat(String key) {
        return StatCollector.translateToLocal(key);
    }
}
