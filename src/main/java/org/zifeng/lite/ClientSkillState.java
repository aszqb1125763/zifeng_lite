package org.zifeng.lite;

/**
 * 客户端显示用状态。只存基本类型，不引用任何 client-only 类，服务端类加载安全。
 */
public final class ClientSkillState {
    private static final boolean[] ENABLED = new boolean[Skill.values().length];
    private static int flySpeedLevel = SkillData.DEFAULT_FLY_SPEED_LEVEL;

    static {
        for (int i = 0; i < ENABLED.length; i++) {
            ENABLED[i] = true;
        }
    }

    private ClientSkillState() {
    }

    public static boolean isEnabled(Skill skill) {
        int id = skill.ordinal();
        return id >= 0 && id < ENABLED.length && ENABLED[id];
    }

    public static int getFlySpeedLevel() {
        return flySpeedLevel;
    }

    public static void update(boolean[] enabled, int level) {
        for (int i = 0; i < ENABLED.length; i++) {
            ENABLED[i] = i < enabled.length && enabled[i];
        }
        if (level < 0) {
            level = 0;
        }
        if (level > 100) {
            level = 100;
        }
        flySpeedLevel = level;
    }
}
