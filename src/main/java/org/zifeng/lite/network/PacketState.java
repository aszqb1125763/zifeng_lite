package org.zifeng.lite.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import org.zifeng.lite.ClientSkillState;
import org.zifeng.lite.Skill;
import org.zifeng.lite.SkillData;

/** S2C：同步五项开关和飞行速度等级。 */
public class PacketState implements IMessage {
    private boolean[] enabled = new boolean[Skill.values().length];
    private int flySpeedLevel = SkillData.DEFAULT_FLY_SPEED_LEVEL;

    public PacketState() {
    }

    public PacketState(EntityPlayer player) {
        for (Skill skill : Skill.values()) {
            enabled[skill.ordinal()] = SkillData.isEnabled(player, skill);
        }
        flySpeedLevel = SkillData.getFlySpeedLevel(player);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readUnsignedByte();
        enabled = new boolean[count];
        for (int i = 0; i < count; i++) {
            enabled[i] = buf.readBoolean();
        }
        flySpeedLevel = buf.readUnsignedByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(enabled.length);
        for (boolean value : enabled) {
            buf.writeBoolean(value);
        }
        buf.writeByte(flySpeedLevel);
    }

    public static class Handler implements IMessageHandler<PacketState, IMessage> {
        @Override
        public IMessage onMessage(PacketState message, MessageContext ctx) {
            ClientSkillState.update(message.enabled, message.flySpeedLevel);
            return null;
        }
    }
}
