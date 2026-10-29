package org.zifeng.lite;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;
import org.zifeng.lite.event.PlayerEvents;
import org.zifeng.lite.event.ServerTickHandler;
import org.zifeng.lite.network.PacketHandler;

/**
 * Zifeng Lite 1.7.10 Forge 版。
 *
 * <p>五项免费常驻技能：不毁 / 飞行 / 不饿 / 飞行速度 / 夜视。
 * 无技能点、无 HUD、无前置。
 */
@Mod(modid = ZifengLite.MODID, name = ZifengLite.NAME, version = ZifengLite.VERSION,
        acceptedMinecraftVersions = "[1.7.10]")
public class ZifengLite {
    public static final String MODID = "zifeng_lite";
    public static final String NAME = "Zifeng Lite";
    public static final String VERSION = "1.6.0-lite";

    @SidedProxy(clientSide = "org.zifeng.lite.client.ClientProxy",
            serverSide = "org.zifeng.lite.CommonProxy")
    public static CommonProxy proxy;

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        PacketHandler.init();
        PlayerEvents playerEvents = new PlayerEvents();
        FMLCommonHandler.instance().bus().register(playerEvents);
        MinecraftForge.EVENT_BUS.register(playerEvents);
        FMLCommonHandler.instance().bus().register(new ServerTickHandler());
        proxy.preInit(event);
        logger.info("[{}] Zifeng Lite 1.7.10 初始化完成", MODID);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("[{}] 五项免费技能加载完成", MODID);
    }
}
