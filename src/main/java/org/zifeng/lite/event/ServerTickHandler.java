package org.zifeng.lite.event;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import org.zifeng.lite.CapabilitySpeed;
import org.zifeng.lite.Compat;
import org.zifeng.lite.DraconicCompat;
import org.zifeng.lite.Skill;
import org.zifeng.lite.SkillData;

/** 服务端每 tick 应用五项技能。 */
public class ServerTickHandler {
    private static final float BASE_FLY_SPEED = 0.05F;
    private static final float FLY_SPEED_PER_LEVEL = 0.00625F;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player == null || player.worldObj == null || player.worldObj.isRemote
                || !(player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP mp = (EntityPlayerMP) player;
        SkillData.ensure(mp);

        // DE 的 CustomArmorHandler 在 START 阶段把 flySpeed 重置成龙甲值；
        // 我们在 LOWEST/START 再写一次，保证本 tick 的移动使用目标速度。
        if (event.phase == TickEvent.Phase.START) {
            applyFlySpeed(mp);
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        applyUnbreakable(mp);
        applyFlight(mp);
        applyFlySpeed(mp);
        if (mp.ticksExisted % 20 == 0) {
            applySaturation(mp);
            applyNightVision(mp);
        }
    }

    private void applyUnbreakable(EntityPlayer player) {
        if (!SkillData.isEnabled(player, Skill.UNBREAKABLE)) {
            return;
        }
        resetDamage(player.inventory.mainInventory);
        resetDamage(player.inventory.armorInventory);
    }

    private void resetDamage(ItemStack[] stacks) {
        if (stacks == null) {
            return;
        }
        for (ItemStack stack : stacks) {
            if (stack != null && Compat.isDamageable(stack) && Compat.getDamage(stack) > 0) {
                Compat.setDamage(stack, 0);
            }
        }
    }

    private void applyFlight(EntityPlayerMP player) {
        boolean enabled = SkillData.isEnabled(player, Skill.FLIGHT);
        boolean changed = false;
        if (enabled) {
            if (!player.capabilities.allowFlying) {
                player.capabilities.allowFlying = true;
                changed = true;
            }
        } else if (!player.capabilities.isCreativeMode) {
            if (player.capabilities.isFlying) {
                player.capabilities.isFlying = false;
                changed = true;
            }
            if (player.capabilities.allowFlying) {
                player.capabilities.allowFlying = false;
                changed = true;
            }
        }
        if (changed) {
            Compat.sendAbilities(player);
        }
    }

    private void applyFlySpeed(EntityPlayerMP player) {
        boolean enabled = SkillData.isEnabled(player, Skill.FLY_SPEED);
        int level = SkillData.getFlySpeedLevel(player);
        float target = BASE_FLY_SPEED
                + (enabled ? level * FLY_SPEED_PER_LEVEL : 0.0F)
                + DraconicCompat.getFlightSpeedBonus(player);
        if (Math.abs(CapabilitySpeed.get(player.capabilities) - target) > 0.0001F) {
            CapabilitySpeed.set(player.capabilities, target);
            Compat.sendAbilities(player);
        }
    }

    private void applySaturation(EntityPlayerMP player) {
        if (!SkillData.isEnabled(player, Skill.SATURATION)) {
            return;
        }
        Compat.setFood(player);
    }

    private void applyNightVision(EntityPlayerMP player) {
        if (SkillData.isEnabled(player, Skill.NIGHT_VISION)) {
            if (player.ticksExisted % 20 == 0) {
                Compat.addNightVision(player);
            }
        } else {
            Compat.removeNightVision(player);
        }
    }
}
