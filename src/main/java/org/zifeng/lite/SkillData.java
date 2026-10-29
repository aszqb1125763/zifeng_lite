package org.zifeng.lite;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * 玩家技能数据，存在 player.getEntityData() 的 ZifengLite 子标签里。
 * 服务端权威；客户端只通过 PacketState 同步显示。
 */
public final class SkillData {
    public static final String ROOT = "ZifengLite";
    public static final String FLY_SPEED_LEVEL = "fly_speed_level";
    public static final String INITIALIZED = "lite_initialized";
    public static final int DEFAULT_FLY_SPEED_LEVEL = 8;

    private SkillData() {
    }

    private static String key(Skill skill) {
        return "enabled_" + skill.getId();
    }

    public static NBTTagCompound getTag(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        NBTTagCompound tag;
        if (data.hasKey(ROOT)) {
            tag = data.getCompoundTag(ROOT);
        } else {
            tag = new NBTTagCompound();
            data.setTag(ROOT, tag);
        }
        boolean changed = false;
        // 首次创建 / 旧数据迁移：一次性全部设为开启，避免旧测试标签里残留的 false 影响新玩家。
        if (!tag.hasKey(INITIALIZED)) {
            for (Skill skill : Skill.values()) {
                tag.setBoolean(key(skill), true);
            }
            tag.setInteger(FLY_SPEED_LEVEL, DEFAULT_FLY_SPEED_LEVEL);
            tag.setBoolean(INITIALIZED, true);
            changed = true;
        }
        for (Skill skill : Skill.values()) {
            if (!tag.hasKey(key(skill))) {
                tag.setBoolean(key(skill), true);
                changed = true;
            }
        }
        if (!tag.hasKey(FLY_SPEED_LEVEL)) {
            tag.setInteger(FLY_SPEED_LEVEL, DEFAULT_FLY_SPEED_LEVEL);
            changed = true;
        }
        if (changed) {
            data.setTag(ROOT, tag);
        }
        return tag;
    }

    public static void ensure(EntityPlayer player) {
        getTag(player);
    }

    public static boolean isEnabled(EntityPlayer player, Skill skill) {
        return getTag(player).getBoolean(key(skill));
    }

    public static void setEnabled(EntityPlayer player, Skill skill, boolean enabled) {
        NBTTagCompound tag = getTag(player);
        tag.setBoolean(key(skill), enabled);
        player.getEntityData().setTag(ROOT, tag);
    }

    public static int getFlySpeedLevel(EntityPlayer player) {
        int level = getTag(player).getInteger(FLY_SPEED_LEVEL);
        if (level < 0) {
            return 0;
        }
        if (level > 100) {
            return 100;
        }
        return level;
    }

    public static void setFlySpeedLevel(EntityPlayer player, int level) {
        if (level < 0) {
            level = 0;
        }
        if (level > 100) {
            level = 100;
        }
        NBTTagCompound tag = getTag(player);
        tag.setInteger(FLY_SPEED_LEVEL, level);
        player.getEntityData().setTag(ROOT, tag);
    }

    /** 死亡/换维度时把旧数据复制给新玩家实体。 */
    public static void copy(EntityPlayer from, EntityPlayer to) {
        if (from == null || to == null) {
            return;
        }
        NBTTagCompound source = from.getEntityData().getCompoundTag(ROOT);
        if (source.hasNoTags()) {
            return;
        }
        to.getEntityData().setTag(ROOT, source.copy());
    }
}
