package com.entropy.tacz_turrets.compat;

import net.minecraft.server.MinecraftServer;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.api.IPartyManagerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;

import java.util.UUID;

public class OpenPACCompat {
    public static boolean isAlly(MinecraftServer server, UUID owner, UUID target) {
        IPartyManagerAPI parties = OpenPACServerAPI.get(server).getPartyManager();
        IServerPartyAPI ownerParty = parties.getPartyByMember(owner);
        if (ownerParty == null) return false;
        if (ownerParty.getMemberInfo(target) != null) return true;
        IServerPartyAPI targetParty = parties.getPartyByMember(target);
        return targetParty != null && ownerParty.isAlly(targetParty.getId());
    }
}
