package org.zifeng.skilltree;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.zifeng.skilltree.client.ClientRegistrar;
import org.zifeng.skilltree.event.LiteTickEvents;
import org.zifeng.skilltree.event.SkillEvents;
import org.zifeng.skilltree.init.ModAttributes;
import org.zifeng.skilltree.network.ModNetwork;

/**
 * Zifeng Lite 1.21.1 主类。
 *
 * <p>只保留：技能树界面 + 五项常驻技能（不毁 / 飞行 / 不饿 / 飞行速度 / 夜视）。
 * 星能转换机、技能点、HUD、光环、木棍、馈赠、机械共鸣、垂钓、兼容模块、
 * 指令、方块与物品注册全部不再注册。
 */
@Mod(SkillTreeMod.MOD_ID)
public class SkillTreeMod {
    public static final String MOD_ID = "zifeng_lite";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SkillTreeMod(IEventBus modEventBus, ModContainer modContainer) {
        ModAttributes.ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(ModNetwork::register);
        modEventBus.addListener(SkillEvents::registerPlayerAttributes);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(Config::onConfigChanged);

        NeoForge.EVENT_BUS.register(SkillEvents.class);
        NeoForge.EVENT_BUS.register(LiteTickEvents.class);

        if (FMLLoader.getDist().isClient()) {
            ClientRegistrar.register(modEventBus);
        }
        LOGGER.info("[{}] Lite 版加载完成：五项免费技能，无 HUD / 无技能点。", MOD_ID);
    }
}
