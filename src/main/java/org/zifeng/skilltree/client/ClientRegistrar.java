package org.zifeng.skilltree.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Lite 版客户端注册（26.1.2）：只保留 N 键、技能快捷键、客户端会话计时。
 * HUD、渲染器、转换机界面、选区/光环等一律不再注册。
 */
public class ClientRegistrar {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModKeyBindings::registerKeyBindings);
        NeoForge.EVENT_BUS.register(ModKeyBindingEvents.class);
        NeoForge.EVENT_BUS.register(ClientSession.class);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) -> {
            ModKeyBindingEvents.onDisconnect();
            ClientGlobalState.reset();
        });
    }
}
