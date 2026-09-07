package com.entropy.tacz_turrets.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemFilter {
    private static final ItemFilter EMPTY = new ItemFilter(Set.of(), List.of());

    private final Set<ResourceLocation> ids;
    private final List<TagKey<Item>> tags;

    private ItemFilter(Set<ResourceLocation> ids, List<TagKey<Item>> tags) {
        this.ids = ids;
        this.tags = tags;
    }

    public static ItemFilter of(List<? extends String> entries) {
        if (entries == null || entries.isEmpty()) return EMPTY;
        Set<ResourceLocation> ids = new HashSet<>();
        List<TagKey<Item>> tags = new ArrayList<>();
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) continue;
            String trimmed = entry.trim();
            if (trimmed.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(trimmed.substring(1));
                if (tagId != null) tags.add(TagKey.create(Registries.ITEM, tagId));
            } else {
                ResourceLocation id = ResourceLocation.tryParse(trimmed);
                if (id != null) ids.add(id);
            }
        }
        return ids.isEmpty() && tags.isEmpty() ? EMPTY : new ItemFilter(ids, tags);
    }

    public static boolean isValidEntry(Object entry) {
        if (!(entry instanceof String string) || string.isBlank()) return false;
        String trimmed = string.trim();
        return ResourceLocation.tryParse(trimmed.startsWith("#") ? trimmed.substring(1) : trimmed) != null;
    }

    public boolean isEmpty() {
        return ids.isEmpty() && tags.isEmpty();
    }

    public boolean matches(ItemStack stack) {
        if (isEmpty() || stack.isEmpty()) return false;
        if (!ids.isEmpty() && ids.contains(ForgeRegistries.ITEMS.getKey(stack.getItem()))) return true;
        for (TagKey<Item> tag : tags) {
            if (stack.is(tag)) return true;
        }
        return false;
    }
}
