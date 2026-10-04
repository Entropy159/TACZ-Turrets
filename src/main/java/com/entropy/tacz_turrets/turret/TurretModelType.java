package com.entropy.tacz_turrets.turret;

import com.entropy.tacz_turrets.TACZTurrets;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Locale;

public enum TurretModelType {
    RETRO("turret"), MODERN("turret_modern"), INDUSTRIAL("turret_industrial"), SCIFI("turret_scifi"), SCRAP("turret_scrap"), TACTICAL("turret_tactical");

    private final ResourceLocation model;
    private final ResourceLocation[] textures;

    TurretModelType(String name) {
        model = TACZTurrets.id("geo/entity/" + name + ".geo.json");
        textures = Arrays.stream(TurretState.values()).map(state -> TACZTurrets.id("textures/entity/" + name + "_" + state.name().toLowerCase(Locale.ROOT) + ".png")).toArray(ResourceLocation[]::new);
    }

    public ResourceLocation model() {
        return model;
    }

    public ResourceLocation texture(TurretState state) {
        return textures[state.ordinal()];
    }
}
