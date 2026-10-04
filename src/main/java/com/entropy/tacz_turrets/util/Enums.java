package com.entropy.tacz_turrets.util;

public final class Enums {
    public static <E extends Enum<E>> E next(E value) {
        E[] values = value.getDeclaringClass().getEnumConstants();
        return values[(value.ordinal() + 1) % values.length];
    }

    public static <E extends Enum<E>> E byName(String name, E fallback) {
        for (E value : fallback.getDeclaringClass().getEnumConstants()) {
            if (value.name().equals(name)) return value;
        }
        return fallback;
    }

    public static <E extends Enum<E>> E byOrdinal(Class<E> type, int ordinal) {
        E[] values = type.getEnumConstants();
        return values[Math.floorMod(ordinal, values.length)];
    }
}
