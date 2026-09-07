package com.entropy.tacz_turrets.network;

import com.entropy.tacz_turrets.TACZTurrets;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class TACZTurretsNetwork {
    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(TACZTurrets.MODID, "main"))
            .networkProtocolVersion(() -> VERSION)
            .clientAcceptedVersions(VERSION::equals)
            .serverAcceptedVersions(VERSION::equals)
            .simpleChannel();

    public static void register() {
        CHANNEL.registerMessage(0, ToggleAllyPacket.class, ToggleAllyPacket::encode, ToggleAllyPacket::decode, ToggleAllyPacket::handle);
    }
}
