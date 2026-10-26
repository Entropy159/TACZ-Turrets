package com.entropy.tacz_turrets.compat;

//? if forge {
import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.world.RecruitsDiplomacyManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

import java.util.UUID;

public class RecruitsCompat {
    private static boolean broken;

    public static boolean isOwnRecruit(UUID owner, LivingEntity target) {
        if (broken) return false;
        try {
            return target instanceof AbstractRecruitEntity recruit && owner.equals(recruit.getOwnerUUID());
        } catch (LinkageError error) {
            broken = true;
            return false;
        }
    }

    public static boolean isAlliedFaction(PlayerTeam ownerTeam, Team targetTeam) {
        if (broken) return false;
        try {
            RecruitsDiplomacyManager diplomacy = FactionEvents.recruitsDiplomacyManager;
            return diplomacy != null && diplomacy.getRelation(ownerTeam.getName(), targetTeam.getName()) == RecruitsDiplomacyManager.DiplomacyStatus.ALLY;
        } catch (LinkageError error) {
            broken = true;
            return false;
        }
    }
}
//?}
