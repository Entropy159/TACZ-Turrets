package com.entropy.tacz_turrets.turret;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import net.minecraft.resources.ResourceLocation;
//? if forge {
import net.minecraftforge.fml.ModList;
//?} else {
/*import net.neoforged.fml.ModList;
*///?}

import java.util.Arrays;
import java.util.Locale;

public enum TurretType {
    MODERN("turret_modern", 200, 6.0D, 64, 1.0D, 1.0D, 1.0D),
    INDUSTRIAL("turret_industrial", 300, 10.0D, 56, 1.5D, 1.1D, 0.9D),
    SCRAP("turret_scrap", 120, 2.0D, 48, 2.0D, 1.0D, 0.8D),
    TACTICAL("turret_tactical", 220, 8.0D, 72, 0.75D, 1.25D, 1.15D),
    BRASS("turret_brass", 200, 6.0D, 64, 1.0D, 1.0D, 1.0D),
    STEEL("turret_steel", 200, 6.0D, 64, 1.0D, 1.0D, 1.0D),
    BASIC("turret_mekanism", "turret_basic", 200, 6.0D, 64, 1.0D, 1.0D, 1.0D),
    ADVANCED("turret_mekanism", "turret_advanced", 250, 8.0D, 72, 0.8D, 1.1D, 1.05D),
    ELITE("turret_mekanism", "turret_elite", 300, 10.0D, 80, 0.6D, 1.2D, 1.1D),
    ULTIMATE("turret_mekanism", "turret_ultimate", 400, 14.0D, 96, 0.4D, 1.35D, 1.2D);

    public final int defaultHealth;
    public final double defaultArmor;
    public final int defaultRange;
    public final double defaultInaccuracy;
    public final double defaultReloadSpeed;
    public final double defaultFireRate;
    private final ResourceLocation model;
    private final ResourceLocation[] textures;
    private final ResourceLocation paintMask;

    TurretType(String name, int health, double armor, int range, double inaccuracy, double reloadSpeed, double fireRate) {
        this(name, name, health, armor, range, inaccuracy, reloadSpeed, fireRate);
    }

    TurretType(String modelName, String name, int health, double armor, int range, double inaccuracy, double reloadSpeed, double fireRate) {
        defaultHealth = health;
        defaultArmor = armor;
        defaultRange = range;
        defaultInaccuracy = inaccuracy;
        defaultReloadSpeed = reloadSpeed;
        defaultFireRate = fireRate;
        model = TACZTurrets.id("geo/entity/" + modelName + ".geo.json");
        paintMask = TACZTurrets.id("textures/entity/" + modelName + "_paint.png");
        textures = Arrays.stream(TurretState.values()).map(state -> TACZTurrets.id("textures/entity/" + name + "_" + state.name().toLowerCase(Locale.ROOT) + ".png")).toArray(ResourceLocation[]::new);
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    private String requiredMod() {
        return switch (this) {
            case BRASS -> "create";
            case STEEL -> "immersiveengineering";
            case BASIC, ADVANCED, ELITE, ULTIMATE -> "mekanism";
            default -> null;
        };
    }

    public boolean isModded() {
        return requiredMod() != null;
    }

    public boolean isAvailable() {
        return !isModded() || ModList.get().isLoaded(requiredMod());
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

    public ResourceLocation paintMask() {
        return paintMask;
    }
}
