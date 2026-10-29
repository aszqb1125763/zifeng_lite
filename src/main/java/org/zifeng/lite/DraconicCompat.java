package org.zifeng.lite;

import net.minecraft.entity.player.EntityPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 可选兼容 Draconic Evolution：
 * DE 的 CustomArmorHandler 每 tick 会把 flySpeed 设为
 * 0.05 + 0.05 * ArmorSummary.flightSpeedModifier。
 * 这里用反射读取同一个 modifier，避免硬依赖 DE 类。
 */
public final class DraconicCompat {
    private static boolean initialized;
    private static boolean unavailable;
    private static Method getSummary;
    private static Field flightSpeedModifier;
    private static Field flightVModifier;

    private DraconicCompat() {
    }

    private static synchronized void init() {
        if (initialized || unavailable) {
            return;
        }
        initialized = true;
        try {
            Class<?> summaryClass = Class.forName(
                    "com.brandon3055.draconicevolution.common.items.armor.ArmorSummary");
            getSummary = summaryClass.getMethod("get", EntityPlayer.class);
            try {
                flightSpeedModifier = summaryClass.getField("flightSpeedModifier");
            } catch (NoSuchFieldException e) {
                flightSpeedModifier = summaryClass.getDeclaredField("flightSpeedModifier");
                flightSpeedModifier.setAccessible(true);
            }
            try {
                flightVModifier = summaryClass.getField("flightVModifier");
            } catch (NoSuchFieldException e) {
                flightVModifier = summaryClass.getDeclaredField("flightVModifier");
                flightVModifier.setAccessible(true);
            }
        } catch (Throwable ignored) {
            unavailable = true;
            getSummary = null;
            flightSpeedModifier = null;
            flightVModifier = null;
        }
    }

    /** 返回 DE 的飞行速度加成（0.05 * modifier）；没有 DE/没穿龙甲时返回 0。 */
    public static float getFlightSpeedBonus(EntityPlayer player) {
        init();
        if (unavailable || getSummary == null || flightSpeedModifier == null || player == null) {
            return 0.0F;
        }
        try {
            Object summary = getSummary.invoke(null, player);
            if (summary == null) {
                return 0.0F;
            }
            float modifier = flightSpeedModifier.getFloat(summary);
            return modifier > 0.0F ? 0.05F * modifier : 0.0F;
        } catch (Throwable ignored) {
            return 0.0F;
        }
    }

    /** DE 的垂直飞行 modifier（ArmorSummary.flightVModifier）；没有 DE/没穿龙甲时返回 0。 */
    public static float getFlightVerticalModifier(EntityPlayer player) {
        init();
        if (unavailable || getSummary == null || flightVModifier == null || player == null) {
            return 0.0F;
        }
        try {
            Object summary = getSummary.invoke(null, player);
            if (summary == null) {
                return 0.0F;
            }
            return Math.max(0.0F, flightVModifier.getFloat(summary));
        } catch (Throwable ignored) {
            return 0.0F;
        }
    }
}
