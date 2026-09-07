package com.entropy.tacz_turrets.turret.state;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.turret.TurretEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public enum TurretState {
    ACTIVE("Active"), RELOADING("Reloading"), NO_AMMO("No Ammo"), NO_GUN("No Gun"), DISABLED("Disabled");

    public static final EntityDataAccessor<String> stateName = SynchedEntityData.defineId(TurretEntity.class, EntityDataSerializers.STRING);

    public final String name;
    private final String texture;

    TurretState(String name) {
        this.name = name;
        texture = name.toLowerCase(Locale.ROOT).replaceAll(" ", "_");
    }

    public void setState(TurretEntity turret) {
        turret.getEntityData().set(stateName, name);
    }

    public ResourceLocation getPath() {
        return TACZTurrets.id("textures/entity/turret_" + texture + ".png");
    }

    public static TurretState getState(TurretEntity turret) {
        for (TurretState state : values()) {
            if (turret.getEntityData().get(stateName).equals(state.name)) return state;
        }
        return NO_GUN;
    }
}
