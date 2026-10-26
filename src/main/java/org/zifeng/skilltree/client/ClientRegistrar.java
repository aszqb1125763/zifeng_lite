package org.zifeng.skilltree.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Lite 版客户端注册（2026-10-26）。
 *
 * <p>只保留：打开技能树的 N 键、技能树内的开关/等级快捷键、客户端会话计时。
 * 技能点 HUD、真实血量数字、复活提示、木棍信息栏、光环/选区渲染、方块扫描高亮等
 * 一律不再注册。
 */
public class ClientRegistrar {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModKeyBindings::registerKeyBindings);
        MinecraftForge.EVENT_BUS.register(ModKeyBindingEvents.class);
        MinecraftForge.EVENT_BUS.register(ClientSession.class);
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) -> {
            ModKeyBindingEvents.onDisconnect();
            ClientGlobalState.reset();
        });
    }
}
