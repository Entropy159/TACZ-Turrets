package com.entropy.tacz_turrets.turret;

public enum TurretMode {
    AGGRESSIVE, CONSERVATIVE, ADAPTIVE;

    public TurretMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static TurretMode byName(String name) {
        for (TurretMode mode : values()) {
            if (mode.name().equals(name)) return mode;
        }
        return AGGRESSIVE;
    }
}
