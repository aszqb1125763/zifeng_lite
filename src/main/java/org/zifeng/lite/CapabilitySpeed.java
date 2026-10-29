package org.zifeng.lite;

import net.minecraft.entity.player.PlayerCapabilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * GTNH 2.9 的运行时是混合命名：
 * - 原版方法仍是 SRG（如 NBTTagCompound.func_74764_b）
 * - Forge 给 PlayerCapabilities 补的 setFlySpeed/getFlySpeed 是 MCP 名
 * 普通 1.7.10 Forge 的 reobf jar 则把这两个方法映射成 func_75092_a/func_75093_a。
 * 这里按运行时实际存在的方法名反射调用，避免任一环境 NoSuchMethodError。
 */
public final class CapabilitySpeed {
    private static final Method GET;
    private static final Method SET;
    private static final Field FIELD;

    static {
        GET = find(new String[]{"getFlySpeed", "func_75093_a", "a"}, new Class<?>[0]);
        SET = find(new String[]{"setFlySpeed", "func_75092_a", "a"}, new Class<?>[]{float.class});
        FIELD = findField(new String[]{"flySpeed", "field_75096_f", "field_75092_a", "f"});
    }

    private CapabilitySpeed() {
    }

    private static Method find(String[] names, Class<?>[] params) {
        for (String name : names) {
            try {
                return PlayerCapabilities.class.getMethod(name, params);
            } catch (NoSuchMethodException ignored) {
                // try next name
            }
        }
        return null;
    }

    private static Field findField(String[] names) {
        for (String name : names) {
            try {
                Field field = PlayerCapabilities.class.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // try next name
            }
        }
        return null;
    }

    public static float get(PlayerCapabilities capabilities) {
        if (FIELD != null) {
            try {
                return FIELD.getFloat(capabilities);
            } catch (ReflectiveOperationException ignored) {
                // fall through to method
            }
        }
        if (GET != null) {
            try {
                Object value = GET.invoke(capabilities);
                if (value instanceof Float) {
                    return (Float) value;
                }
            } catch (ReflectiveOperationException ignored) {
                // keep vanilla speed
            }
        }
        return 0.05F;
    }

    public static void set(PlayerCapabilities capabilities, float value) {
        if (FIELD != null) {
            try {
                FIELD.setFloat(capabilities, value);
                return;
            } catch (ReflectiveOperationException ignored) {
                // fall through to method
            }
        }
        if (SET != null) {
            try {
                SET.invoke(capabilities, value);
            } catch (ReflectiveOperationException ignored) {
                // keep vanilla speed
            }
        }
    }
}
