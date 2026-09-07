package com.entropy.tacz_turrets.util;

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

public class TurretAllies extends SavedData {
    private static final String FILE_NAME = "tacz_turrets_allies";
    private static final String OWNERS_TAG = "Owners";
    private static final String OWNER_TAG = "Owner";
    private static final String ALLIES_TAG = "Allies";

    private final Map<UUID, Set<UUID>> allies = new HashMap<>();

    public static TurretAllies get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TurretAllies::load, TurretAllies::new, FILE_NAME);
    }

    public static TurretAllies load(CompoundTag tag) {
        TurretAllies data = new TurretAllies();
        ListTag owners = tag.getList(OWNERS_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < owners.size(); index++) {
            CompoundTag entry = owners.getCompound(index);
            if (!entry.hasUUID(OWNER_TAG)) continue;
            Set<UUID> trusted = new HashSet<>();
            ListTag list = entry.getList(ALLIES_TAG, Tag.TAG_INT_ARRAY);
            for (int ally = 0; ally < list.size(); ally++) {
                trusted.add(NbtUtils.loadUUID(list.get(ally)));
            }
            data.allies.put(entry.getUUID(OWNER_TAG), trusted);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag owners = new ListTag();
        allies.forEach((owner, trusted) -> {
            if (trusted.isEmpty()) return;
            CompoundTag entry = new CompoundTag();
            entry.putUUID(OWNER_TAG, owner);
            ListTag list = new ListTag();
            trusted.forEach(ally -> list.add(NbtUtils.createUUID(ally)));
            entry.put(ALLIES_TAG, list);
            owners.add(entry);
        });
        tag.put(OWNERS_TAG, owners);
        return tag;
    }

    public boolean isAlly(UUID owner, UUID other) {
        Set<UUID> trusted = allies.get(owner);
        return trusted != null && trusted.contains(other);
    }

    public Set<UUID> getAllies(UUID owner) {
        return Set.copyOf(allies.getOrDefault(owner, Set.of()));
    }

    public boolean addAlly(UUID owner, UUID ally) {
        if (owner.equals(ally)) return false;
        if (!allies.computeIfAbsent(owner, key -> new HashSet<>()).add(ally)) return false;
        setDirty();
        return true;
    }

    public boolean removeAlly(UUID owner, UUID ally) {
        Set<UUID> trusted = allies.get(owner);
        if (trusted == null || !trusted.remove(ally)) return false;
        setDirty();
        return true;
    }

    public boolean toggleAlly(UUID owner, UUID ally) {
        if (isAlly(owner, ally)) {
            removeAlly(owner, ally);
            return false;
        }
        addAlly(owner, ally);
        return true;
    }
}
