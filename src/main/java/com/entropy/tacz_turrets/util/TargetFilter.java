package com.entropy.tacz_turrets.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TargetFilter {
    private static final TargetFilter EMPTY = new TargetFilter(Set.of(), List.of());

    private final Set<ResourceLocation> ids;
    private final List<TagKey<EntityType<?>>> tags;

    private TargetFilter(Set<ResourceLocation> ids, List<TagKey<EntityType<?>>> tags) {
        this.ids = ids;
        this.tags = tags;
    }

    public static TargetFilter of(List<? extends String> entries) {
        if (entries == null || entries.isEmpty()) return EMPTY;
        Set<ResourceLocation> ids = new HashSet<>();
        List<TagKey<EntityType<?>>> tags = new ArrayList<>();
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) continue;
            String trimmed = entry.trim();
            if (trimmed.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(trimmed.substring(1));
                if (tagId != null) tags.add(TagKey.create(Registries.ENTITY_TYPE, tagId));
            } else {
                ResourceLocation id = ResourceLocation.tryParse(trimmed);
                if (id != null) ids.add(id);
            }
        }
        return ids.isEmpty() && tags.isEmpty() ? EMPTY : new TargetFilter(ids, tags);
    }

    public static boolean isValidEntry(Object entry) {
        if (!(entry instanceof String string) || string.isBlank()) return false;
        String trimmed = string.trim();
        return ResourceLocation.tryParse(trimmed.startsWith("#") ? trimmed.substring(1) : trimmed) != null;
    }

    public boolean isEmpty() {
        return ids.isEmpty() && tags.isEmpty();
    }

    public boolean matches(EntityType<?> type) {
        if (isEmpty()) return false;
        if (!ids.isEmpty() && ids.contains(EntityType.getKey(type))) return true;
        for (TagKey<EntityType<?>> tag : tags) {
            if (type.is(tag)) return true;
        }
        return false;
    }
}
