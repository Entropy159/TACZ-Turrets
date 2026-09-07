package com.entropy.tacz_turrets.turret.state;

public enum PlayerTargeting {
    NEVER, RETALIATE, ALL;

    public PlayerTargeting next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static PlayerTargeting byName(String name) {
        try {
            return PlayerTargeting.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RETALIATE;
        }
    }
}
