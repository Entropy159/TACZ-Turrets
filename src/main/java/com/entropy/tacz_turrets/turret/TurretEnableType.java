package com.entropy.tacz_turrets.turret;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public enum TurretEnableType {
    ALWAYS_ON(signal -> false), REDSTONE_ON(signal -> !signal), REDSTONE_OFF(signal -> signal), ALWAYS_OFF(signal -> true);

    private final Function<Boolean, Boolean> powerFunction;

    TurretEnableType(Function<Boolean, Boolean> function) {
        powerFunction = function;
    }

    public TurretEnableType next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static TurretEnableType byName(String name) {
        for (TurretEnableType type : values()) {
            if (type.name().equals(name)) return type;
        }
        return ALWAYS_ON;
    }

    public boolean shouldDisable(Level level, BlockPos blockPos) {
        return powerFunction.apply(level.hasNeighborSignal(blockPos));
    }
}
