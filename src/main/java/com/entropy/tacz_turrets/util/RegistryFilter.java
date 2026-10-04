package com.entropy.tacz_turrets.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistryFilter<T> {
    private final Registry<T> registry;
    private final Set<ResourceLocation> ids = new HashSet<>();
    private final List<TagKey<T>> tags = new ArrayList<>();

    private RegistryFilter(Registry<T> registry) {
        this.registry = registry;
    }

    public static <T> RegistryFilter<T> of(Registry<T> registry, List<? extends String> entries) {
        RegistryFilter<T> filter = new RegistryFilter<>(registry);
        for (String entry : entries) {
            String trimmed = entry.trim();
            boolean tag = trimmed.startsWith("#");
            ResourceLocation id = ResourceLocation.tryParse(tag ? trimmed.substring(1) : trimmed);
            if (id == null) continue;
            if (tag) {
                filter.tags.add(TagKey.create(registry.key(), id));
            } else {
                filter.ids.add(id);
            }
        }
        return filter;
    }

    public static boolean isValidEntry(Object entry) {
        if (!(entry instanceof String string) || string.isBlank()) return false;
        String trimmed = string.trim();
        return ResourceLocation.tryParse(trimmed.startsWith("#") ? trimmed.substring(1) : trimmed) != null;
    }

    public boolean isEmpty() {
        return ids.isEmpty() && tags.isEmpty();
    }

    public boolean matches(T value) {
        return ids.contains(registry.getKey(value)) || tags.stream().anyMatch(registry.wrapAsHolder(value)::is);
    }
}
