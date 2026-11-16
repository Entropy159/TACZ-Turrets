package com.entropy.tacz_turrets.network;

import com.entropy.tacz_turrets.item.LaserDesignatorItem;
import net.minecraft.server.level.ServerPlayer;
//? if forge {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
//?} else {
/*import com.entropy.tacz_turrets.TACZTurrets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
*///?}

//? if forge {
public record DesignatorFirePacket(boolean firing) {
    public static void encode(DesignatorFirePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.firing);
    }

    public static DesignatorFirePacket decode(FriendlyByteBuf buf) {
        return new DesignatorFirePacket(buf.readBoolean());
    }

    public static void handle(DesignatorFirePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> packet.apply(context.get().getSender()));
        context.get().setPacketHandled(true);
    }
//?} else {
/*public record DesignatorFirePacket(boolean firing) implements CustomPacketPayload {
    public static final Type<DesignatorFirePacket> TYPE = new Type<>(TACZTurrets.id("designator_fire"));
    public static final StreamCodec<ByteBuf, DesignatorFirePacket> STREAM_CODEC = ByteBufCodecs.BOOL.map(DesignatorFirePacket::new, DesignatorFirePacket::firing);

    @Override
    public Type<DesignatorFirePacket> type() {
        return TYPE;
    }

    public static void handle(DesignatorFirePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.apply(context.player() instanceof ServerPlayer player ? player : null));
    }
*///?}

    private void apply(ServerPlayer sender) {
        if (sender != null) LaserDesignatorItem.setFiring(sender, firing);
    }
}
