package com.entropy.tacz_turrets.turret;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public enum TurretEnableType {
    ALWAYS_ON, REDSTONE_ON, REDSTONE_OFF, ALWAYS_OFF;

    public boolean shouldDisable(Level level, BlockPos pos) {
        return switch (this) {
            case ALWAYS_ON -> false;
            case REDSTONE_ON -> !level.hasNeighborSignal(pos);
            case REDSTONE_OFF -> level.hasNeighborSignal(pos);
            case ALWAYS_OFF -> true;
        };
    }
}
