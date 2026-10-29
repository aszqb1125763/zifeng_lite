package org.zifeng.lite;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.FoodStats;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 1.7.10 的普通 Forge 与 GTNH 2.9 对同一批 Minecraft 类使用不同命名
 * （SRG / MCP / Notch 混合）。这里按“候选名 + 参数/返回类型”反射，
 * 避免某一个方法名在当前运行时不存在就把玩家踢掉。
 */
public final class Compat {
    private static final Method GET_FOOD_STATS = findReturn(EntityPlayer.class, FoodStats.class,
            "getFoodStats", "func_71024_bL");
    private static final Method SET_FOOD_LEVEL = findVoid(FoodStats.class, new Class<?>[]{int.class},
            "setFoodLevel", "func_75114_a");
    private static final Method SET_SATURATION = findVoid(FoodStats.class, new Class<?>[]{float.class},
            "setFoodSaturationLevel", "func_75119_b");

    private static final Method IS_DAMAGEABLE = findReturn(ItemStack.class, boolean.class,
            "isItemStackDamageable", "func_77984_f");
    private static final Method GET_DAMAGE = findReturn(ItemStack.class, int.class,
            "getItemDamage", "func_77960_j");
    private static final Method SET_DAMAGE = findVoid(ItemStack.class, new Class<?>[]{int.class},
            "setItemDamage", "func_77964_b");

    private static final Method ADD_POTION = findVoid(EntityLivingBase.class,
            new Class<?>[]{PotionEffect.class}, "addPotionEffect", "func_70690_d");
    private static final Method REMOVE_POTION = findVoid(EntityLivingBase.class,
            new Class<?>[]{int.class}, "removePotionEffect", "func_70695_b");

    private static final Method SEND_ABILITIES = findVoid(EntityPlayerMP.class, new Class<?>[0],
            "sendPlayerAbilities", "func_71016_p");

    private static final int NIGHT_VISION_ID = 16;

    private Compat() {
    }

    private static Method findReturn(Class<?> owner, Class<?> returnType, String... names) {
        for (String name : names) {
            try {
                Method method = owner.getMethod(name);
                if (method.getReturnType() == returnType) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
                // next name / signature scan
            }
        }
        for (Method method : owner.getMethods()) {
            if (method.getReturnType() == returnType && method.getParameterTypes().length == 0) {
                return method;
            }
        }
        return null;
    }

    private static Method findVoid(Class<?> owner, Class<?>[] params, String... names) {
        for (String name : names) {
            try {
                Method method = owner.getMethod(name, params);
                if (method.getReturnType() == void.class) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
                // next name / signature scan
            }
        }
        for (Method method : owner.getMethods()) {
            if (method.getReturnType() == void.class
                    && java.util.Arrays.equals(method.getParameterTypes(), params)) {
                return method;
            }
        }
        return null;
    }

    public static void setFood(EntityPlayer player) {
        try {
            if (GET_FOOD_STATS == null) {
                return;
            }
            FoodStats stats = (FoodStats) GET_FOOD_STATS.invoke(player);
            if (stats == null) {
                return;
            }
            if (SET_FOOD_LEVEL != null) {
                SET_FOOD_LEVEL.invoke(stats, 20);
            }
            if (SET_SATURATION != null) {
                SET_SATURATION.invoke(stats, 20.0F);
            }
        } catch (Throwable ignored) {
            // optional skill must never crash the server tick
        }
    }

    public static boolean isDamageable(ItemStack stack) {
        if (stack == null || IS_DAMAGEABLE == null) {
            return false;
        }
        try {
            return (Boolean) IS_DAMAGEABLE.invoke(stack);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static int getDamage(ItemStack stack) {
        if (stack == null || GET_DAMAGE == null) {
            return 0;
        }
        try {
            return (Integer) GET_DAMAGE.invoke(stack);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static void setDamage(ItemStack stack, int damage) {
        if (stack == null || SET_DAMAGE == null) {
            return;
        }
        try {
            SET_DAMAGE.invoke(stack, damage);
        } catch (Throwable ignored) {
            // optional skill must never crash the server tick
        }
    }

    public static void addNightVision(EntityLivingBase player) {
        if (ADD_POTION == null) {
            return;
        }
        try {
            ADD_POTION.invoke(player, new PotionEffect(NIGHT_VISION_ID, 300, 0, true));
        } catch (Throwable ignored) {
            // constructor/class mapping may differ
        }
    }

    public static void removeNightVision(EntityLivingBase player) {
        if (REMOVE_POTION == null) {
            return;
        }
        try {
            REMOVE_POTION.invoke(player, NIGHT_VISION_ID);
        } catch (Throwable ignored) {
            // optional skill must never crash the server tick
        }
    }

    public static void sendAbilities(EntityPlayerMP player) {
        if (SEND_ABILITIES == null) {
            return;
        }
        try {
            SEND_ABILITIES.invoke(player);
        } catch (Throwable ignored) {
            // optional sync; client also applies its own speed
        }
    }

    /** PacketAction 里的 ctx.getServerHandler().playerEntity 也可能是 SRG 字段。 */
    public static EntityPlayerMP getServerPlayer(MessageContext ctx) {
        try {
            Object handler = ctx.getServerHandler();
            if (handler == null) {
                return null;
            }
            for (String name : new String[]{"playerEntity", "field_147369_b"}) {
                try {
                    Field field = handler.getClass().getField(name);
                    Object value = field.get(handler);
                    if (value instanceof EntityPlayerMP) {
                        return (EntityPlayerMP) value;
                    }
                } catch (NoSuchFieldException ignored) {
                    // next name
                }
            }
        } catch (Throwable ignored) {
            // no-op
        }
        return null;
    }
}
