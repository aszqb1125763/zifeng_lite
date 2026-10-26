package org.zifeng.skilltree.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.zifeng.skilltree.data.PlayerSkillRecord;
import org.zifeng.skilltree.data.PlayerSkillSavedData;
import org.zifeng.skilltree.network.ModNetwork;
import org.zifeng.skilltree.network.SkillTreeDataS2CPacket;
import org.zifeng.skilltree.skill.SkillEffects;
import org.zifeng.skilltree.skill.Skills;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Lite 版服务端事件入口（2026-10-26）。
 *
 * <p>只做五项技能真正需要的事，避免复用原版 {@code UltimateEvents.tickPlayer}
 * 里为 160 个技能准备的分支（以及其中对未学技能的“回收 buff”副作用）：
 * <ol>
 *   <li>登录 / 重生 / 死亡克隆时，把玩家数据归一化为“五项技能免费满级、默认开启”；</li>
 *   <li>每 tick：创造飞行权限、飞行速度同步；每 20 tick：饱食度、永久夜视；</li>
 *   <li>把最新技能数据回发客户端，保证刚进世界就能用快捷键开关。</li>
 * </ol>
 */
public final class LiteTickEvents {
    /** 本模组授予飞行权限的玩家（用于关闭技能/登出时精确回收，不误关创造模式或其他模组飞行） */
    private static final Set<UUID> FLIGHT_GRANTED = new HashSet<>();

    /** 与原版 UltimateEvents 一致：无限时长判定阈值 */
    private static final int INFINITE_DURATION = 1_000_000;

    private LiteTickEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Forge 1.20.1 的 PlayerTickEvent 每 tick 触发 START + END 两次，只取一次
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.serverLevel() == null || player instanceof net.minecraftforge.common.util.FakePlayer) {
            return;
        }

        PlayerSkillRecord record = PlayerSkillSavedData.get(player.serverLevel()).getOrCreatePlayer(player.getUUID());
        tickFlight(player, record);
        tickFlightSpeed(player, record);
        if (player.tickCount % 20 == 0) {
            tickNightVision(player, record);
            tickSaturation(player, record);
        }
    }

    /** 子枫的苍穹之翼：创造式飞行权限（只授予/回收本模组给的那一份） */
    private static void tickFlight(ServerPlayer player, PlayerSkillRecord record) {
        boolean favor = record.getLearnedPoints(Skills.ULT_FAVOR) > 0 && record.isEnabled(Skills.ULT_FAVOR);
        Abilities abilities = player.getAbilities();
        if (favor) {
            if (!abilities.instabuild && !abilities.mayfly) {
                abilities.mayfly = true;
                player.onUpdateAbilities();
            }
            FLIGHT_GRANTED.add(player.getUUID());
        } else if (FLIGHT_GRANTED.remove(player.getUUID())) {
            if (!abilities.instabuild) {
                abilities.mayfly = false;
                abilities.flying = false;
            }
            player.onUpdateAbilities();
        }
    }

    /** 御空翱翔：把 FLYING_SPEED 属性（÷8）同步到原版 abilities.flyingSpeed */
    private static void tickFlightSpeed(ServerPlayer player, PlayerSkillRecord record) {
        AttributeInstance flyAttr = player.getAttribute(Attributes.FLYING_SPEED);
        if (flyAttr == null) {
            return;
        }
        Abilities abilities = player.getAbilities();
        double points = SkillEffects.effLevel(record, Skills.FLY);
        if (points > 0) {
            float speed = (float) (flyAttr.getValue() / 8.0);
            if (Math.abs(abilities.getFlyingSpeed() - speed) > 0.0001F) {
                abilities.setFlyingSpeed(speed);
                player.onUpdateAbilities();
            }
        } else if (record.getLearnedPoints(Skills.FLY) > 0
                && Math.abs(abilities.getFlyingSpeed() - 0.05F) > 0.0001F) {
            // 技能关闭 / 生效等级 0 → 还原原版默认飞行速度
            abilities.setFlyingSpeed(0.05F);
            player.onUpdateAbilities();
        }
    }

    /** 星瞳夜视：开启时给无限时长夜视；关闭时只回收本模组的无限夜视 */
    private static void tickNightVision(ServerPlayer player, PlayerSkillRecord record) {
        boolean on = record.getLearnedPoints(Skills.NIGHT_VISION) > 0 && record.isEnabled(Skills.NIGHT_VISION);
        if (on) {
            var effect = player.getEffect(MobEffects.NIGHT_VISION);
            if (effect == null) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
            }
        } else {
            var effect = player.getEffect(MobEffects.NIGHT_VISION);
            if (effect != null && effect.getDuration() >= INFINITE_DURATION) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
    }

    /** 饱食无忧：饱食度与饱和度永远满值（20 tick 节流，避免频繁标记 FoodData 脏） */
    private static void tickSaturation(ServerPlayer player, PlayerSkillRecord record) {
        if (record.getLearnedPoints(Skills.SATURATION) > 0 && record.isEnabled(Skills.SATURATION)) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0F);
        }
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ensure(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ensure(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ensure(player);
        }
    }

    /** 归一化数据 + 重挂属性 + 回发客户端数据（幂等） */
    public static void ensure(ServerPlayer player) {
        if (player == null || player.serverLevel() == null) {
            return;
        }
        PlayerSkillSavedData data = PlayerSkillSavedData.get(player.serverLevel());
        PlayerSkillRecord record = data.getOrCreatePlayer(player.getUUID());
        record.normalizeLiteSkills();
        data.setDirty();
        SkillEffects.applyAll(player, record);
        ModNetwork.sendToPlayer(player, SkillTreeDataS2CPacket.from(record));
    }

    /** 登出 / 切存档清理：回收本模组授予的飞行权限并还原飞行速度 */
    public static void onLogout(ServerPlayer player) {
        if (player == null) {
            return;
        }
        boolean granted = FLIGHT_GRANTED.remove(player.getUUID());
        Abilities abilities = player.getAbilities();
        if (granted && !abilities.instabuild) {
            abilities.mayfly = false;
            abilities.flying = false;
        }
        if (Math.abs(abilities.getFlyingSpeed() - 0.05F) > 0.0001F) {
            abilities.setFlyingSpeed(0.05F);
        }
        player.onUpdateAbilities();
    }
}
