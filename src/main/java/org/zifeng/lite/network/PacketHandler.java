package org.zifeng.lite.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import org.zifeng.lite.ZifengLite;

public final class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(ZifengLite.MODID);

    private PacketHandler() {
    }

    public static void init() {
        INSTANCE.registerMessage(PacketAction.Handler.class, PacketAction.class, 0, Side.SERVER);
        INSTANCE.registerMessage(PacketState.Handler.class, PacketState.class, 1, Side.CLIENT);
    }
}
