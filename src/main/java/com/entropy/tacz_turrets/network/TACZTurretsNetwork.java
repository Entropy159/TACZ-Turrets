package com.entropy.tacz_turrets.network;

//? if forge {
import com.entropy.tacz_turrets.TACZTurrets;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
//?} else {
/*import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
*///?}

public class TACZTurretsNetwork {
    //? if forge {
    private static final String VERSION = "2";

    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(TACZTurrets.id("main"))
            .networkProtocolVersion(() -> VERSION)
            .clientAcceptedVersions(VERSION::equals)
            .serverAcceptedVersions(VERSION::equals)
            .simpleChannel();

    public static void register() {
        CHANNEL.registerMessage(0, ToggleAllyPacket.class, ToggleAllyPacket::encode, ToggleAllyPacket::decode, ToggleAllyPacket::handle);
        CHANNEL.registerMessage(1, DesignatorFirePacket.class, DesignatorFirePacket::encode, DesignatorFirePacket::decode, DesignatorFirePacket::handle);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
    //?} else {
    /*public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2")
                .playToServer(ToggleAllyPacket.TYPE, ToggleAllyPacket.STREAM_CODEC, ToggleAllyPacket::handle)
                .playToServer(DesignatorFirePacket.TYPE, DesignatorFirePacket.STREAM_CODEC, DesignatorFirePacket::handle);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }
    *///?}
}
