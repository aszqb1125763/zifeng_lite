package org.zifeng.lite.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import org.zifeng.lite.SkillData;
import org.zifeng.lite.network.PacketHandler;
import org.zifeng.lite.network.PacketState;

/** 登录、死亡重生时初始化/复制/同步数据。 */
public class PlayerEvents {
    @SubscribeEvent
    public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            SkillData.ensure(player);
            PacketHandler.INSTANCE.sendTo(new PacketState(player), player);
        }
    }

    @SubscribeEvent
    public void onClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        if (event.original != null && event.entityPlayer != null) {
            SkillData.copy(event.original, event.entityPlayer);
        }
        if (event.entityPlayer instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.entityPlayer;
            SkillData.ensure(player);
            PacketHandler.INSTANCE.sendTo(new PacketState(player), player);
        }
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            SkillData.ensure(player);
            PacketHandler.INSTANCE.sendTo(new PacketState(player), player);
        }
    }
}
