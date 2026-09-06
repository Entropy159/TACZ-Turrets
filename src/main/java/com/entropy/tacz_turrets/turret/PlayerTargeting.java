package com.entropy.tacz_turrets.turret;

public enum PlayerTargeting {
    NEVER, RETALIATE, ALL;

    public PlayerTargeting next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static PlayerTargeting byName(String name) {
        for (PlayerTargeting targeting : values()) {
            if (targeting.name().equals(name)) return targeting;
        }
        return RETALIATE;
    }
}
