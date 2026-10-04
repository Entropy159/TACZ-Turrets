package com.entropy.tacz_turrets.network;

//? if forge {
import com.entropy.tacz_turrets.TACZTurrets;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
//?} else {
/*import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
*///?}

public class TACZTurretsNetwork {
    //? if forge {
    private static final String VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(TACZTurrets.id("main"))
            .networkProtocolVersion(() -> VERSION)
            .clientAcceptedVersions(VERSION::equals)
            .serverAcceptedVersions(VERSION::equals)
            .simpleChannel();

    public static void register() {
        CHANNEL.registerMessage(0, ToggleAllyPacket.class, ToggleAllyPacket::encode, ToggleAllyPacket::decode, ToggleAllyPacket::handle);
    }

    public static void sendToServer(ToggleAllyPacket packet) {
        CHANNEL.sendToServer(packet);
    }
    //?} else {
    /*public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(ToggleAllyPacket.TYPE, ToggleAllyPacket.STREAM_CODEC, ToggleAllyPacket::handle);
    }

    public static void sendToServer(ToggleAllyPacket packet) {
        PacketDistributor.sendToServer(packet);
    }
    *///?}
}
