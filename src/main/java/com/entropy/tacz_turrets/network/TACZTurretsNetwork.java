package com.entropy.tacz_turrets.network;

import com.entropy.tacz_turrets.TACZTurrets;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class TACZTurretsNetwork {
    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(TACZTurrets.id("main"))
            .networkProtocolVersion(() -> VERSION)
            .clientAcceptedVersions(VERSION::equals)
            .serverAcceptedVersions(VERSION::equals)
            .simpleChannel();

    public static void register() {
        CHANNEL.registerMessage(0, ToggleAllyPacket.class, ToggleAllyPacket::encode, ToggleAllyPacket::decode, ToggleAllyPacket::handle);
    }
}
