package org.zifeng.lite.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.zifeng.lite.Skill;
import org.zifeng.lite.network.PacketAction;
import org.zifeng.lite.network.PacketHandler;

@SideOnly(Side.CLIENT)
public class ClientKeyHandler {
    public static KeyBinding OPEN;
    public static KeyBinding UNBREAKABLE;
    public static KeyBinding FLIGHT;
    public static KeyBinding SATURATION;
    public static KeyBinding FLY_SPEED;
    public static KeyBinding NIGHT_VISION;
    public static KeyBinding FLY_SPEED_LEVEL;

    public static void init() {
        OPEN = new KeyBinding("key.zifeng_lite.open", Keyboard.KEY_N, "key.categories.zifeng_lite");
        UNBREAKABLE = new KeyBinding("key.zifeng_lite.unbreakable", Keyboard.KEY_U, "key.categories.zifeng_lite");
        FLIGHT = new KeyBinding("key.zifeng_lite.flight", Keyboard.KEY_I, "key.categories.zifeng_lite");
        SATURATION = new KeyBinding("key.zifeng_lite.saturation", Keyboard.KEY_O, "key.categories.zifeng_lite");
        FLY_SPEED = new KeyBinding("key.zifeng_lite.fly_speed", Keyboard.KEY_J, "key.categories.zifeng_lite");
        NIGHT_VISION = new KeyBinding("key.zifeng_lite.night_vision", Keyboard.KEY_P, "key.categories.zifeng_lite");
        FLY_SPEED_LEVEL = new KeyBinding("key.zifeng_lite.fly_speed_level", Keyboard.KEY_K, "key.categories.zifeng_lite");

        ClientRegistry.registerKeyBinding(OPEN);
        ClientRegistry.registerKeyBinding(UNBREAKABLE);
        ClientRegistry.registerKeyBinding(FLIGHT);
        ClientRegistry.registerKeyBinding(SATURATION);
        ClientRegistry.registerKeyBinding(FLY_SPEED);
        ClientRegistry.registerKeyBinding(NIGHT_VISION);
        ClientRegistry.registerKeyBinding(FLY_SPEED_LEVEL);
    }

    public static String keyName(KeyBinding binding) {
        if (binding == null) {
            return "?";
        }
        String name = Keyboard.getKeyName(binding.getKeyCode());
        return name == null ? "?" : name;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!Keyboard.getEventKeyState()) {
            return;
        }
        int key = Keyboard.getEventKey();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return;
        }
        if (key == OPEN.getKeyCode()) {
            if (mc.currentScreen instanceof GuiSkillTree) {
                mc.displayGuiScreen(null);
            } else if (mc.currentScreen == null) {
                mc.displayGuiScreen(new GuiSkillTree());
            }
            return;
        }
        if (mc.currentScreen != null) {
            return;
        }
        if (key == UNBREAKABLE.getKeyCode()) {
            toggle(Skill.UNBREAKABLE);
        } else if (key == FLIGHT.getKeyCode()) {
            toggle(Skill.FLIGHT);
        } else if (key == SATURATION.getKeyCode()) {
            toggle(Skill.SATURATION);
        } else if (key == FLY_SPEED.getKeyCode()) {
            toggle(Skill.FLY_SPEED);
        } else if (key == NIGHT_VISION.getKeyCode()) {
            toggle(Skill.NIGHT_VISION);
        } else if (key == FLY_SPEED_LEVEL.getKeyCode()) {
            int amount = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                    || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? 10 : 1;
            PacketHandler.INSTANCE.sendToServer(
                    new PacketAction(Skill.FLY_SPEED, PacketAction.ADD_LEVEL, amount));
        }
    }

    private void toggle(Skill skill) {
        PacketHandler.INSTANCE.sendToServer(new PacketAction(skill, PacketAction.TOGGLE, 0));
    }
}
