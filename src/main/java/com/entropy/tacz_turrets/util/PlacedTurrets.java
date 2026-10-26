package com.entropy.tacz_turrets.util;

//? if neoforge
/*import net.minecraft.core.HolderLookup;*/
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlacedTurrets extends SavedData {
    private static final String FILE_NAME = "tacz_turrets_placed";
    private static final String OWNERS_TAG = "Owners";
    private static final String OWNER_TAG = "Owner";
    private static final String TURRETS_TAG = "Turrets";

    private final Map<UUID, Set<UUID>> turrets = new HashMap<>();

    public static PlacedTurrets get(MinecraftServer server) {
        //? if forge {
        return server.overworld().getDataStorage().computeIfAbsent(PlacedTurrets::load, PlacedTurrets::new, FILE_NAME);
        //?} else {
        /*return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(PlacedTurrets::new, PlacedTurrets::load), FILE_NAME);
        *///?}
    }

    //? if forge {
    public static PlacedTurrets load(CompoundTag tag) {
    //?} else {
    /*public static PlacedTurrets load(CompoundTag tag, HolderLookup.Provider registries) {
    *///?}
        PlacedTurrets data = new PlacedTurrets();
        ListTag owners = tag.getList(OWNERS_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < owners.size(); index++) {
            CompoundTag entry = owners.getCompound(index);
            if (!entry.hasUUID(OWNER_TAG)) continue;
            Set<UUID> placed = new HashSet<>();
            ListTag list = entry.getList(TURRETS_TAG, Tag.TAG_INT_ARRAY);
            for (int turret = 0; turret < list.size(); turret++) {
                placed.add(NbtUtils.loadUUID(list.get(turret)));
            }
            data.turrets.put(entry.getUUID(OWNER_TAG), placed);
        }
        return data;
    }

    @Override
    //? if forge {
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
    //?} else {
    /*public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
    *///?}
        ListTag owners = new ListTag();
        turrets.forEach((owner, placed) -> {
            if (placed.isEmpty()) return;
            CompoundTag entry = new CompoundTag();
            entry.putUUID(OWNER_TAG, owner);
            ListTag list = new ListTag();
            placed.forEach(turret -> list.add(NbtUtils.createUUID(turret)));
            entry.put(TURRETS_TAG, list);
            owners.add(entry);
        });
        tag.put(OWNERS_TAG, owners);
        return tag;
    }

    public int count(UUID owner) {
        return turrets.getOrDefault(owner, Set.of()).size();
    }

    public void add(UUID owner, UUID turret) {
        if (turrets.computeIfAbsent(owner, key -> new HashSet<>()).add(turret)) setDirty();
    }

    public void remove(UUID owner, UUID turret) {
        Set<UUID> placed = turrets.get(owner);
        if (placed != null && placed.remove(turret)) setDirty();
    }
}
