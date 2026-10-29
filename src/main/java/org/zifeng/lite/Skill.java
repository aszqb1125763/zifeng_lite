package org.zifeng.lite;

/**
 * 五项 Lite 技能。顺序即网络包/界面顺序，不要随意调整。
 */
public enum Skill {
    UNBREAKABLE("unbreakable", "skill.zifeng_lite.unbreakable", "skill.zifeng_lite.unbreakable.desc"),
    FLIGHT("flight", "skill.zifeng_lite.flight", "skill.zifeng_lite.flight.desc"),
    SATURATION("saturation", "skill.zifeng_lite.saturation", "skill.zifeng_lite.saturation.desc"),
    FLY_SPEED("fly_speed", "skill.zifeng_lite.fly_speed", "skill.zifeng_lite.fly_speed.desc"),
    NIGHT_VISION("night_vision", "skill.zifeng_lite.night_vision", "skill.zifeng_lite.night_vision.desc");

    private final String id;
    private final String nameKey;
    private final String descKey;

    Skill(String id, String nameKey, String descKey) {
        this.id = id;
        this.nameKey = nameKey;
        this.descKey = descKey;
    }

    public String getId() {
        return id;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getDescKey() {
        return descKey;
    }

    public static Skill byId(int id) {
        Skill[] values = values();
        if (id < 0 || id >= values.length) {
            return null;
        }
        return values[id];
    }
}
