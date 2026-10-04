package com.entropy.tacz_turrets.compat;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;

import java.util.UUID;

public class FTBTeamsCompat {
    public static boolean isAlly(UUID owner, UUID target) {
        FTBTeamsAPI.API api = FTBTeamsAPI.api();
        return api.isManagerLoaded() && api.getManager().getTeamForPlayerID(owner).map(team -> team.getRankForPlayer(target).isAllyOrBetter()).orElse(false);
    }
}
