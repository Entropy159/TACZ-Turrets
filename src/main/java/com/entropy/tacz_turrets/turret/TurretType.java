package com.entropy.tacz_turrets.turret;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Locale;

public enum TurretType {
    MODERN("turret_modern", 200, 6.0D, 64, 1.0D, 1.0D),
    INDUSTRIAL("turret_industrial", 300, 10.0D, 56, 1.5D, 1.1D),
    SCIFI("turret_scifi", 180, 4.0D, 80, 0.5D, 1.15D),
    SCRAP("turret_scrap", 120, 2.0D, 48, 2.0D, 1.0D),
    TACTICAL("turret_tactical", 220, 8.0D, 72, 0.75D, 1.25D);

    public final int defaultHealth;
    public final double defaultArmor;
    public final int defaultRange;
    public final double defaultInaccuracy;
    public final double defaultReloadSpeed;
    private final ResourceLocation model;
    private final ResourceLocation[] textures;

    TurretType(String name, int health, double armor, int range, double inaccuracy, double reloadSpeed) {
        defaultHealth = health;
        defaultArmor = armor;
        defaultRange = range;
        defaultInaccuracy = inaccuracy;
        defaultReloadSpeed = reloadSpeed;
        model = TACZTurrets.id("geo/entity/" + name + ".geo.json");
        textures = Arrays.stream(TurretState.values()).map(state -> TACZTurrets.id("textures/entity/" + name + "_" + state.name().toLowerCase(Locale.ROOT) + ".png")).toArray(ResourceLocation[]::new);
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public TACZTurretsConfig.TurretStats stats() {
        return TACZTurretsConfig.stats(this);
    }

    public ResourceLocation model() {
        return model;
    }

    public ResourceLocation texture(TurretState state) {
        return textures[state.ordinal()];
    }
}
