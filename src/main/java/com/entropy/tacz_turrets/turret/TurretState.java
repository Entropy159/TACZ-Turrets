package com.entropy.tacz_turrets.turret;

import com.entropy.tacz_turrets.util.Enums;

public enum TurretState {
    ACTIVE, RELOADING, NO_AMMO, NO_GUN, DISABLED;

    public void setState(TurretEntity turret) {
        turret.getEntityData().set(TurretEntity.STATE, ordinal());
    }

    public static TurretState getState(TurretEntity turret) {
        return Enums.byOrdinal(TurretState.class, turret.getEntityData().get(TurretEntity.STATE));
    }
}
