package com.entropy.tacz_turrets.turret;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.registry.EntityTypeRegistry;
import com.entropy.tacz_turrets.util.RegistryFilter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record SentrySpawn(Where where, ResourceLocation loadout, List<String> structures, List<String> biomes, float chance, SentryLoadout.Range count, float ceilingChance) {
    private static final int ATTEMPTS = 16;
    private static final int CAVE_SCAN = 32;
    private static final Codec<Float> CHANCE = Codec.floatRange(0.0F, 1.0F);
    public static final Codec<SentrySpawn> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.comapFlatMap(Where::parse, where -> where.name().toLowerCase(Locale.ROOT)).fieldOf("where").forGetter(SentrySpawn::where),
            ResourceLocation.CODEC.optionalFieldOf("loadout", SentryLoadout.DEFAULT).forGetter(SentrySpawn::loadout),
            Codec.STRING.listOf().optionalFieldOf("structures", List.of()).forGetter(SentrySpawn::structures),
            Codec.STRING.listOf().optionalFieldOf("biomes", List.of()).forGetter(SentrySpawn::biomes),
            CHANCE.optionalFieldOf("chance", 1.0F).forGetter(SentrySpawn::chance),
            SentryLoadout.Range.CODEC.optionalFieldOf("count", new SentryLoadout.Range(1, 1)).forGetter(SentrySpawn::count),
            CHANCE.optionalFieldOf("ceiling_chance", 0.0F).forGetter(SentrySpawn::ceilingChance)
    ).apply(instance, SentrySpawn::new));
    private static volatile Map<ResourceLocation, SentrySpawn> spawns = Map.of();

    public enum Where {
        STRUCTURE, SURFACE, CAVE;

        static DataResult<Where> parse(String name) {
            for (Where where : values()) if (where.name().equalsIgnoreCase(name)) return DataResult.success(where);
            return DataResult.error(() -> "Unknown sentry spawn place " + name);
        }
    }

    public static void placeInStructure(WorldGenLevel level, StructureStart start, BoundingBox chunkBox) {
        if (spawns.isEmpty() || start.getPieces().isEmpty()) return;
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        spawns.forEach((id, spawn) -> {
            if (spawn.where != Where.STRUCTURE || !matches(registry, spawn.structures, start.getStructure())) return;
            RandomSource random = RandomSource.create(level.getSeed() ^ start.getChunkPos().toLong() * 31L ^ id.hashCode());
            if (random.nextFloat() >= spawn.chance) return;
            for (int count = spawn.count.roll(random); count > 0; count--) {
                BoundingBox box = start.getPieces().get(random.nextInt(start.getPieces().size())).getBoundingBox();
                int x = Mth.randomBetweenInclusive(random, box.minX(), box.maxX());
                int z = Mth.randomBetweenInclusive(random, box.minZ(), box.maxZ());
                boolean ceiling = random.nextFloat() < spawn.ceilingChance;
                RandomSource retry = RandomSource.create(random.nextLong());
                if (x < chunkBox.minX() || x > chunkBox.maxX() || z < chunkBox.minZ() || z > chunkBox.maxZ()) continue;
                int minX = Math.max(box.minX(), chunkBox.minX()), maxX = Math.min(box.maxX(), chunkBox.maxX());
                int minZ = Math.max(box.minZ(), chunkBox.minZ()), maxZ = Math.min(box.maxZ(), chunkBox.maxZ());
                for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
                    BlockPos spot = scan(level, x, z, box.maxY(), box.minY(), ceiling);
                    if (spot != null && spawn.place(level, spot, ceiling, retry, MobSpawnType.STRUCTURE)) break;
                    x = Mth.randomBetweenInclusive(retry, minX, maxX);
                    z = Mth.randomBetweenInclusive(retry, minZ, maxZ);
                }
            }
        });
    }

    public static void placeNaturally(WorldGenLevel level, ChunkPos chunkPos) {
        if (spawns.isEmpty() || isProtected(level, chunkPos)) return;
        spawns.forEach((id, spawn) -> {
            if (spawn.where == Where.STRUCTURE || !(spawn.where == Where.SURFACE ? TACZTurretsConfig.SURFACE_SENTRY_SPAWNS : TACZTurretsConfig.CAVE_SENTRY_SPAWNS).get()) return;
            RandomSource random = RandomSource.create(level.getSeed() ^ chunkPos.toLong() * 31L ^ id.hashCode());
            if (random.nextFloat() >= spawn.chance) return;
            for (int count = spawn.count.roll(random); count > 0; count--) {
                for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
                    int x = chunkPos.getMinBlockX() + random.nextInt(16);
                    int z = chunkPos.getMinBlockZ() + random.nextInt(16);
                    boolean ceiling = spawn.where == Where.CAVE && random.nextFloat() < spawn.ceilingChance;
                    BlockPos spot = spawn.where == Where.SURFACE ? surface(level, x, z) : cave(level, random, x, z, ceiling);
                    if (spot != null && spawn.place(level, spot, ceiling, random, MobSpawnType.CHUNK_GENERATION)) break;
                }
            }
        });
    }

    private static boolean isProtected(WorldGenLevel level, ChunkPos chunkPos) {
        int radius = TACZTurretsConfig.SENTRY_SPAWN_PROTECTION.get();
        if (radius <= 0 || level.getLevel().dimension() != Level.OVERWORLD) return false;
        //? if forge {
        double dx = chunkPos.getMiddleBlockX() - level.getLevelData().getXSpawn(), dz = chunkPos.getMiddleBlockZ() - level.getLevelData().getZSpawn();
        //?} else {
        /*double dx = chunkPos.getMiddleBlockX() - level.getLevelData().getSpawnPos().getX(), dz = chunkPos.getMiddleBlockZ() - level.getLevelData().getSpawnPos().getZ();
        *///?}
        return dx * dx + dz * dz < (double) radius * radius;
    }

    @Nullable
    private static BlockPos surface(WorldGenLevel level, int x, int z) {
        BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
        return isSpot(level, pos, false) ? pos : null;
    }

    @Nullable
    private static BlockPos cave(WorldGenLevel level, RandomSource random, int x, int z, boolean ceiling) {
        int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z) - 8;
        int bottom = level.getMinBuildHeight() + 8;
        if (top <= bottom) return null;
        int y = Mth.randomBetweenInclusive(random, bottom, top);
        return ceiling ? scan(level, x, z, y, Math.min(top, y + CAVE_SCAN), true) : scan(level, x, z, y, Math.max(bottom, y - CAVE_SCAN), false);
    }

    @Nullable
    private static BlockPos scan(WorldGenLevel level, int x, int z, int from, int to, boolean ceiling) {
        int step = from <= to ? 1 : -1;
        for (int y = from; y != to + step; y += step) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isSpot(level, pos, ceiling)) return pos;
        }
        return null;
    }

    private static boolean isSpot(WorldGenLevel level, BlockPos pos, boolean ceiling) {
        BlockPos support = ceiling ? pos.above() : pos.below();
        return isOpen(level, pos) && isOpen(level, ceiling ? pos.below() : pos.above()) && level.getBlockState(support).isFaceSturdy(level, support, ceiling ? Direction.DOWN : Direction.UP);
    }

    private static boolean isOpen(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty();
    }

    private static <T> boolean matches(Registry<T> registry, List<String> entries, T value) {
        return entries.isEmpty() || RegistryFilter.of(registry, entries).matches(value);
    }

    private boolean place(WorldGenLevel level, BlockPos pos, boolean ceiling, RandomSource random, MobSpawnType spawnType) {
        if (SentryLoadout.get(loadout).map(sentry -> sentry.enabledTypes().isEmpty()).orElse(true)) return false;
        if (!matches(level.registryAccess().registryOrThrow(Registries.BIOME), biomes, level.getBiome(pos).value())) return false;
        TurretEntity sentry = EntityTypeRegistry.SENTRY.get().create(level.getLevel());
        if (sentry == null) return false;
        sentry.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        sentry.setYHeadRot(sentry.getYRot());
        sentry.setYBodyRot(sentry.getYRot());
        sentry.setCeiling(ceiling);
        sentry.setLoadout(loadout);
        //? if forge {
        sentry.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), spawnType, null, null);
        //?} else {
        /*sentry.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), spawnType, null);
        *///?}
        level.addFreshEntityWithPassengers(sentry);
        return true;
    }

    public static class Listener extends SimpleJsonResourceReloadListener {
        public Listener() {
            super(new Gson(), "sentry_spawns");
        }

        @Override
        protected void apply(@NotNull Map<ResourceLocation, JsonElement> files, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
            Map<ResourceLocation, SentrySpawn> loaded = new HashMap<>();
            files.forEach((id, json) -> {
                DataResult<SentrySpawn> result = CODEC.parse(JsonOps.INSTANCE, json);
                result.result().ifPresent(spawn -> loaded.put(id, spawn));
                result.error().ifPresent(error -> TACZTurrets.LOGGER.error("Couldn't load sentry spawn {}: {}", id, error.message()));
            });
            spawns = loaded;
        }
    }
}
