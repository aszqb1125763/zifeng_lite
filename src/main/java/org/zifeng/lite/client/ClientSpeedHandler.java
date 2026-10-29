package org.zifeng.lite.client;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import org.zifeng.lite.CapabilitySpeed;
import org.zifeng.lite.ClientSkillState;
import org.zifeng.lite.DraconicCompat;
import org.zifeng.lite.Skill;

/**
 * 客户端也要在 DE 的 PlayerTick 之后重算一次 flySpeed；
 * 否则服务端虽然同步了叠加值，DE 的客户端 tick 会把它改回龙甲自己的值。
 */
@SideOnly(Side.CLIENT)
public class ClientSpeedHandler {
    private static final float BASE_FLY_SPEED = 0.05F;
    private static final float FLY_SPEED_PER_LEVEL = 0.00625F;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player == null || player.worldObj == null || !player.worldObj.isRemote) {
            return;
        }
        boolean enabled = ClientSkillState.isEnabled(Skill.FLY_SPEED);
        int level = ClientSkillState.getFlySpeedLevel();
        float target = BASE_FLY_SPEED
                + (enabled ? level * FLY_SPEED_PER_LEVEL : 0.0F)
                + DraconicCompat.getFlightSpeedBonus(player);
        if (Math.abs(CapabilitySpeed.get(player.capabilities) - target) > 0.0001F) {
            CapabilitySpeed.set(player.capabilities, target);
        }

        // 1.7.10 的 flySpeed 只作用于水平；垂直由 motionY 控制。
        // 在 DE 的 START 之后、以及 END 各写一次，恢复垂直速度并跟随等级/龙甲。
        if (player instanceof EntityClientPlayerMP
                && player.capabilities.isFlying && !player.onGround && player.ridingEntity == null) {
            EntityClientPlayerMP client = (EntityClientPlayerMP) player;
            if (client.movementInput != null) {
                boolean up = client.movementInput.jump && !client.movementInput.sneak;
                boolean down = client.movementInput.sneak && !client.movementInput.jump;
                if (up || down) {
                    float multiplier = enabled ? 1.0F + level * 0.125F : 1.0F;
                    float vertical = Math.max(0.15F * multiplier,
                            0.225F * DraconicCompat.getFlightVerticalModifier(player));
                    player.motionY = up ? vertical : -vertical;
                }
            }
        }
    }
}
