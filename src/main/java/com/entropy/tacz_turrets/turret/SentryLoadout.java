package com.entropy.tacz_turrets.turret;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.registry.ItemRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
//? if forge {
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
//?} else {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
*///?}
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SentryLoadout(List<TurretType> types, boolean hostile, List<GunEntry> guns, Optional<Range> ammo, float laserChance, float paintChance, List<Either<Item, Banner>> paints, int range, float gunDropChance, float laserDropChance) {
    public static final ResourceLocation DEFAULT = TACZTurrets.id("default");
    private static final Codec<TurretType> TYPE_CODEC = Codec.STRING.comapFlatMap(id -> Arrays.stream(TurretType.values()).filter(type -> type.id().equals(id)).findFirst().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown turret type " + id)), TurretType::id);
    private static final Codec<Float> CHANCE = Codec.floatRange(0.0F, 1.0F);
    public static final Codec<SentryLoadout> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.nonEmptyList(TYPE_CODEC.listOf()).optionalFieldOf("types", List.of(TurretType.values())).forGetter(SentryLoadout::types),
            Codec.BOOL.optionalFieldOf("hostile", true).forGetter(SentryLoadout::hostile),
            ExtraCodecs.nonEmptyList(GunEntry.CODEC.listOf()).fieldOf("guns").forGetter(SentryLoadout::guns),
            Range.CODEC.optionalFieldOf("ammo").forGetter(SentryLoadout::ammo),
            CHANCE.optionalFieldOf("laser_chance", 0.0F).forGetter(SentryLoadout::laserChance),
            CHANCE.optionalFieldOf("paint_chance", 0.0F).forGetter(SentryLoadout::paintChance),
            Codec.either(BuiltInRegistries.ITEM.byNameCodec(), Banner.CODEC).listOf().optionalFieldOf("paints", List.of()).forGetter(SentryLoadout::paints),
            Codec.intRange(1, 256).optionalFieldOf("range", 32).forGetter(SentryLoadout::range),
            CHANCE.optionalFieldOf("gun_drop_chance", 0.1F).forGetter(SentryLoadout::gunDropChance),
            CHANCE.optionalFieldOf("laser_drop_chance", 0.0F).forGetter(SentryLoadout::laserDropChance)
    ).apply(instance, SentryLoadout::new));
    private static volatile Map<ResourceLocation, SentryLoadout> loadouts = Map.of();

    public static Optional<SentryLoadout> get(ResourceLocation id) {
        return Optional.ofNullable(loadouts.getOrDefault(id, loadouts.get(DEFAULT)));
    }

    public List<TurretType> enabledTypes() {
        return types.stream().filter(TACZTurretsConfig::enabled).toList();
    }

    public void apply(TurretEntity turret, Holder<Biome> biome) {
        RandomSource random = turret.getRandom();
        List<TurretType> pool = enabledTypes().isEmpty() ? types : enabledTypes();
        turret.setTurretType(pool.get(random.nextInt(pool.size())));
        turret.setPlayerTargeting(hostile ? PlayerTargeting.ALL : PlayerTargeting.RETALIATE);
        turret.setSentryRange(range);
        turret.setInfiniteAmmo(ammo.isEmpty());
        turret.setDropChance(EquipmentSlot.MAINHAND, gunDropChance);
        turret.setDropChance(EquipmentSlot.OFFHAND, laserDropChance);
        turret.setLaserStack(random.nextFloat() < laserChance ? randomLaser(random) : ItemStack.EMPTY);
        turret.setPaint(random.nextFloat() < paintChance ? randomPaint(turret, random, biome) : ItemStack.EMPTY);
        ItemStackHandler inventory = turret.getInventory();
        for (int slot = 0; slot < inventory.getSlots(); slot++) inventory.setStackInSlot(slot, ItemStack.EMPTY);
        turret.setGunStack(ItemStack.EMPTY);
        WeightedRandom.getRandomItem(random, guns.stream().filter(entry -> !entry.candidates().isEmpty()).toList()).ifPresent(entry -> {
            List<ResourceLocation> candidates = entry.candidates();
            ResourceLocation gunId = candidates.get(random.nextInt(candidates.size()));
            GunData data = TimelessAPI.getCommonGunIndex(gunId).orElseThrow().getGunData();
            int rounds = ammo.map(range -> range.roll(random)).orElse(data.getAmmoAmount());
            int loaded = Math.min(rounds, data.getAmmoAmount());
            List<FireMode> modes = data.getFireModeSet();
            GunItemBuilder gun = GunItemBuilder.create().setId(gunId).setAmmoCount(loaded).setFireMode(modes.contains(FireMode.AUTO) ? FireMode.AUTO : modes.isEmpty() ? FireMode.UNKNOWN : modes.get(0));
            //? if forge {
            turret.setGunStack(gun.build());
            //?} else {
            /*turret.setGunStack(gun.build(turret.level().registryAccess()));
            *///?}
            int stackSize = TimelessAPI.getCommonAmmoIndex(data.getAmmoId()).map(CommonAmmoIndex::getStackSize).orElse(64);
            for (int left = rounds - loaded; left > 0; left -= stackSize) {
                if (!ItemHandlerHelper.insertItemStacked(inventory, AmmoItemBuilder.create().setId(data.getAmmoId()).setCount(Math.min(left, stackSize)).build(), false).isEmpty()) break;
            }
        });
    }

    private static ItemStack randomLaser(RandomSource random) {
        List<ResourceLocation> lasers = TimelessAPI.getAllCommonAttachmentIndex().stream().filter(entry -> entry.getValue().getType() == AttachmentType.LASER && !entry.getValue().getPojo().isHidden()).map(Map.Entry::getKey).toList();
        return lasers.isEmpty() ? ItemStack.EMPTY : AttachmentItemBuilder.create().setId(lasers.get(random.nextInt(lasers.size()))).build();
    }

    private ItemStack randomPaint(TurretEntity turret, RandomSource random, Holder<Biome> biome) {
        List<Either<Item, Banner>> pool = !paints.isEmpty() ? paints : ItemRegistry.CAMOS.stream()
                .filter(name -> biome.is(TagKey.create(Registries.BIOME, TACZTurrets.id("camo/" + name))))
                .map(name -> Either.<Item, Banner>left(BuiltInRegistries.ITEM.get(TACZTurrets.id(name + "_camo"))))
                .toList();
        if (pool.isEmpty()) return ItemStack.EMPTY;
        ItemStack paint = pool.get(random.nextInt(pool.size())).map(item -> new ItemStack(item), banner -> banner.build(turret));
        return TurretEntity.isPaint(paint) ? paint : ItemStack.EMPTY;
    }

    public record GunEntry(Optional<ResourceLocation> gun, Optional<String> type, int weight) implements WeightedEntry {
        static final Codec<GunEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("gun").forGetter(GunEntry::gun),
                Codec.STRING.optionalFieldOf("type").forGetter(GunEntry::type),
                Codec.intRange(1, 10000).optionalFieldOf("weight", 1).forGetter(GunEntry::weight)
        ).apply(instance, GunEntry::new));

        List<ResourceLocation> candidates() {
            if (gun.isPresent()) return TimelessAPI.getCommonGunIndex(gun.get()).isPresent() ? List.of(gun.get()) : List.of();
            return TimelessAPI.getAllCommonGunIndex().stream().filter(entry -> type.isEmpty() || type.get().equals(entry.getValue().getType())).map(Map.Entry::getKey).toList();
        }

        @Override
        public @NotNull Weight getWeight() {
            return Weight.of(weight);
        }
    }

    public record Banner(DyeColor base, List<Layer> patterns) {
        static final Codec<Banner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DyeColor.CODEC.fieldOf("banner").forGetter(Banner::base),
                Layer.CODEC.listOf().optionalFieldOf("patterns", List.of()).forGetter(Banner::patterns)
        ).apply(instance, Banner::new));

        ItemStack build(TurretEntity turret) {
            ItemStack stack = new ItemStack(BannerBlock.byColor(base));
            //? if forge {
            BannerPattern.Builder builder = new BannerPattern.Builder();
            for (Layer layer : patterns) BuiltInRegistries.BANNER_PATTERN.getHolder(layer.key()).ifPresent(pattern -> builder.addPattern(pattern, layer.color()));
            stack.getOrCreateTagElement("BlockEntityTag").put("Patterns", builder.toListTag());
            //?} else {
            /*BannerPatternLayers.Builder builder = new BannerPatternLayers.Builder();
            for (Layer layer : patterns) builder.addIfRegistered(turret.level().registryAccess().lookupOrThrow(Registries.BANNER_PATTERN), layer.key(), layer.color());
            stack.set(DataComponents.BANNER_PATTERNS, builder.build());
            *///?}
            return stack;
        }

        public record Layer(ResourceLocation pattern, DyeColor color) {
            static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("pattern").forGetter(Layer::pattern),
                    DyeColor.CODEC.fieldOf("color").forGetter(Layer::color)
            ).apply(instance, Layer::new));

            ResourceKey<BannerPattern> key() {
                return ResourceKey.create(Registries.BANNER_PATTERN, pattern);
            }
        }
    }

    public record Range(int min, int max) {
        public static final Codec<Range> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 10000).fieldOf("min").forGetter(Range::min),
                Codec.intRange(0, 10000).fieldOf("max").forGetter(Range::max)
        ).apply(instance, Range::new));

        public int roll(RandomSource random) {
            return Mth.randomBetweenInclusive(random, min, Math.max(min, max));
        }
    }

    public static class Listener extends SimpleJsonResourceReloadListener {
        public Listener() {
            super(new Gson(), "sentry_loadouts");
        }

        @Override
        protected void apply(@NotNull Map<ResourceLocation, JsonElement> files, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
            Map<ResourceLocation, SentryLoadout> loaded = new HashMap<>();
            files.forEach((id, json) -> {
                DataResult<SentryLoadout> result = CODEC.parse(JsonOps.INSTANCE, json);
                result.result().ifPresent(loadout -> loaded.put(id, loadout));
                result.error().ifPresent(error -> TACZTurrets.LOGGER.error("Couldn't load sentry loadout {}: {}", id, error.message()));
            });
            loadouts = loaded;
        }
    }
}
