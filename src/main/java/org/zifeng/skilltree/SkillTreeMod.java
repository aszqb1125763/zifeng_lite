package org.zifeng.skilltree;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.zifeng.skilltree.client.ClientRegistrar;
import org.zifeng.skilltree.event.LiteTickEvents;
import org.zifeng.skilltree.event.SkillEvents;
import org.zifeng.skilltree.init.ModAttributes;
import org.zifeng.skilltree.network.ModNetwork;

/**
 * Lite 版主类（2026-10-26）。
 *
 * <p>只保留：技能树界面 + 五项常驻技能（不毁 / 飞行 / 不饿 / 飞行速度 / 夜视）。
 * 星能转换机、技能点、HUD、光环、木棍、馈赠、机械共鸣、垂钓、枪械、兼容模块、
 * 指令、方块与物品注册全部不再注册（游戏内不存在）。
 */
@Mod(SkillTreeMod.MOD_ID)
public class SkillTreeMod {
    public static final String MOD_ID = "zifeng_lite";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SkillTreeMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 五项技能里飞行速度仍走 FLYING_SPEED 属性；保留自定义属性注册以兼容原属性表代码
        ModAttributes.ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(SkillEvents::registerPlayerAttributes);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(Config::onConfigChanged);

        ModNetwork.register();

        // 玩家登录/重生/数据归一化 + 五项技能常驻 tick
        MinecraftForge.EVENT_BUS.register(SkillEvents.class);
        MinecraftForge.EVENT_BUS.register(LiteTickEvents.class);

        if (FMLLoader.getDist().isClient()) {
            ClientRegistrar.register(modEventBus);
        }
        LOGGER.info("[{}] Lite 版加载完成：五项免费技能，无 HUD / 无技能点。", MOD_ID);
    }
}
