package com.entropy.tacz_turrets.network;

import com.entropy.tacz_turrets.menu.TurretMenu;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.entropy.tacz_turrets.util.TurretAllies;
import net.minecraft.server.level.ServerPlayer;
//? if forge {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
//?} else {
/*import com.entropy.tacz_turrets.TACZTurrets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
*///?}

import java.util.UUID;
//? if forge
import java.util.function.Supplier;

//? if forge {
public record ToggleAllyPacket(UUID target) {
    public static void encode(ToggleAllyPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.target);
    }

    public static ToggleAllyPacket decode(FriendlyByteBuf buf) {
        return new ToggleAllyPacket(buf.readUUID());
    }

    public static void handle(ToggleAllyPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> packet.apply(context.get().getSender()));
        context.get().setPacketHandled(true);
    }
//?} else {
/*public record ToggleAllyPacket(UUID target) implements CustomPacketPayload {
    public static final Type<ToggleAllyPacket> TYPE = new Type<>(TACZTurrets.id("toggle_ally"));
    public static final StreamCodec<ByteBuf, ToggleAllyPacket> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(ToggleAllyPacket::new, ToggleAllyPacket::target);

    @Override
    public Type<ToggleAllyPacket> type() {
        return TYPE;
    }

    public static void handle(ToggleAllyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.apply(context.player() instanceof ServerPlayer player ? player : null));
    }
*///?}

    private void apply(ServerPlayer sender) {
        if (sender == null || !(sender.containerMenu instanceof TurretMenu menu)) return;
        TurretEntity turret = menu.getTurret();
        if (turret == null || !turret.isOwnedBy(sender) || turret.owner == null || turret.owner.equals(target)) return;
        TurretAllies.get(sender.server).toggleAlly(turret.owner, target);
    }
}
