package com.entropy.tacz_turrets.turret.state;

public enum TurretMode {
    AGGRESSIVE, CONSERVATIVE, ADAPTIVE;

    public TurretMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static TurretMode byName(String name) {
        try {
            return TurretMode.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AGGRESSIVE;
        }
    }
}
