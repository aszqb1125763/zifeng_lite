package org.zifeng.lite.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import org.zifeng.lite.Compat;
import org.zifeng.lite.Skill;
import org.zifeng.lite.SkillData;

/** C2S：切换技能，或调整飞行速度等级。 */
public class PacketAction implements IMessage {
    public static final int TOGGLE = 0;
    public static final int ADD_LEVEL = 1;

    private byte skillId;
    private byte action;
    private byte amount;

    public PacketAction() {
    }

    public PacketAction(Skill skill, int action, int amount) {
        this.skillId = (byte) skill.ordinal();
        this.action = (byte) action;
        this.amount = (byte) amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        skillId = buf.readByte();
        action = buf.readByte();
        amount = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(skillId);
        buf.writeByte(action);
        buf.writeByte(amount);
    }

    public static class Handler implements IMessageHandler<PacketAction, IMessage> {
        @Override
        public IMessage onMessage(PacketAction message, MessageContext ctx) {
            EntityPlayerMP player = Compat.getServerPlayer(ctx);
            if (player == null) {
                return null;
            }
            Skill skill = Skill.byId(message.skillId);
            if (skill == null) {
                return null;
            }
            SkillData.ensure(player);
            if (message.action == TOGGLE) {
                SkillData.setEnabled(player, skill, !SkillData.isEnabled(player, skill));
            } else if (message.action == ADD_LEVEL && skill == Skill.FLY_SPEED) {
                int level = SkillData.getFlySpeedLevel(player) + message.amount;
                SkillData.setFlySpeedLevel(player, level);
            }
            PacketHandler.INSTANCE.sendTo(new PacketState(player), player);
            return null;
        }
    }
}
