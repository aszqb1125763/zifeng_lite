package org.zifeng.skilltree.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
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
 * Lite 版服务端事件入口（26.1.2 NeoForge）。
 *
 * <p>只做五项技能真正需要的事：登录归一化、创造飞行、飞行速度同步、饱食度、永久夜视。
 * 不再复用原版 160 技能 tick 链，避免未学技能的“回收 buff”副作用。
 */
public final class LiteTickEvents {
    private static final Set<UUID> FLIGHT_GRANTED = new HashSet<>();
    private static final int INFINITE_DURATION = 1_000_000;

    private LiteTickEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.level() == null || player instanceof net.neoforged.neoforge.common.util.FakePlayer) {
            return;
        }
        PlayerSkillRecord record = PlayerSkillSavedData.get(player.level()).getOrCreatePlayer(player.getUUID());
        tickFlight(player, record);
        tickFlightSpeed(player, record);
        if (player.tickCount % 20 == 0) {
            tickNightVision(player, record);
            tickSaturation(player, record);
        }
    }

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
            abilities.setFlyingSpeed(0.05F);
            player.onUpdateAbilities();
        }
    }

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

    public static void ensure(ServerPlayer player) {
        if (player == null || player.level() == null) {
            return;
        }
        PlayerSkillSavedData data = PlayerSkillSavedData.get(player.level());
        PlayerSkillRecord record = data.getOrCreatePlayer(player.getUUID());
        record.normalizeLiteSkills();
        data.setDirty();
        SkillEffects.applyAll(player, record);
        ModNetwork.sendToPlayer(player, SkillTreeDataS2CPacket.from(record));
    }

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
