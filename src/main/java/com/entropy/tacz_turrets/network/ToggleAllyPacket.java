package com.entropy.tacz_turrets.network;

import com.entropy.tacz_turrets.menu.TurretMenu;
import com.entropy.tacz_turrets.util.TurretAllies;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ToggleAllyPacket {
    private final UUID target;

    public ToggleAllyPacket(UUID target) {
        this.target = target;
    }

    public static void encode(ToggleAllyPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.target);
    }

    public static ToggleAllyPacket decode(FriendlyByteBuf buf) {
        return new ToggleAllyPacket(buf.readUUID());
    }

    public static void handle(ToggleAllyPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) return;
            if (!(sender.containerMenu instanceof TurretMenu menu)) return;
            if (menu.getTurret() == null || !menu.getTurret().isOwnedBy(sender)) return;
            UUID owner = menu.getTurret().owner;
            if (owner == null || owner.equals(packet.target)) return;
            TurretAllies.get(sender.server).toggleAlly(owner, packet.target);
        });
        context.get().setPacketHandled(true);
    }
}
