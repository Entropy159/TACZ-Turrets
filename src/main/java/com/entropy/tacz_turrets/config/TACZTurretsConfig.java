package com.entropy.tacz_turrets.config;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.turret.HealthBarStyle;
import com.entropy.tacz_turrets.turret.InaccuracyMode;
import com.entropy.tacz_turrets.turret.RecoilType;
import com.entropy.tacz_turrets.turret.RetaliateTargeting;
import com.entropy.tacz_turrets.turret.TurretType;
import com.entropy.tacz_turrets.util.RegistryFilter;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
//? if forge {
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
//?} else {
/*import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
*///?}

import java.util.Arrays;
import java.util.List;
import java.util.Set;

//? if forge {
@Mod.EventBusSubscriber(modid = TACZTurrets.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//?} else {
/*@EventBusSubscriber(modid = TACZTurrets.MODID, bus = EventBusSubscriber.Bus.MOD)
*///?}
public class TACZTurretsConfig {
    private static final Builder BUILDER = new Builder();

    public static final BooleanValue CONSUME_AMMO = BUILDER
            .comment("Whether turrets need ammo.")
            .define("consumeAmmo", true);

    public static final ConfigValue<List<? extends String>> SNIPER_GUN_TYPES = BUILDER
            .comment("Gun types that use the sniper range.")
            .defineList("sniperGunTypes", List.of("sniper"), entry -> entry instanceof String type && !type.isBlank());

    public static final BooleanValue TURRETS_TAKE_DAMAGE = BUILDER
            .comment("If false, turrets will be immune to damage.")
            .define("turretsTakeDamage", true);

    public static final BooleanValue DROP_ITEMS_ON_DEATH = BUILDER
            .comment("Destroyed turrets drop their gun and ammo.")
            .define("dropItemsOnDeath", true);

    public static final BooleanValue RETURN_CAMOS = BUILDER
            .comment("Get a turret's camo back when it is scraped off, replaced or destroyed.")
            .define("returnCamos", false);

    public static final BooleanValue PICKUP_ITEMS_TO_INVENTORY = BUILDER
            .comment("Picking up a turret puts its gun and ammo in your inventory. If false, they drop on the ground.")
            .define("pickupItemsToInventory", true);

    public static final BooleanValue PICKUP_TURRET_TO_INVENTORY = BUILDER
            .comment("Picking up a turret puts the turret in your inventory. If false, it drops on the ground.")
            .define("pickupTurretToInventory", true);

    public static final ConfigValue<List<? extends String>> EFFECT_BLACKLIST = BUILDER
            .comment("Effects turrets are immune to. Accepts effect ids and #effect tags.")
            .defineList("effectBlacklist", List.of("minecraft:poison", "minecraft:wither", "minecraft:weakness", "minecraft:blindness", "minecraft:darkness", "minecraft:hunger", "minecraft:mining_fatigue", "minecraft:nausea", "minecraft:slowness"), RegistryFilter::isValidEntry);

    public static final BooleanValue TARGET_ALL_MOBS = BUILDER
            .comment("If true, turrets target all living entities (except players, turrets, and the owner). If false, turrets only target vanilla monsters and entities in the tacz_turrets:turret_targets entity type tag.")
            .define("targetAllMobs", false);

    public static final BooleanValue LOG_TURRET_SHOOT_RESULTS = BUILDER
            .comment("Logs turret shoot results when enabled. Only use for debugging purposes.")
            .define("logTurretShootResults", false);

    public static final ConfigValue<List<? extends String>> TARGET_BLACKLIST = BUILDER
            .comment("Entities turrets never target. Accepts entity ids and #entity tags.")
            .defineList("targetBlacklist", List.of(), RegistryFilter::isValidEntry);

    public static final ConfigValue<List<? extends String>> TARGET_WHITELIST = BUILDER
            .comment("Entities turrets always target. Accepts entity ids and #entity tags.")
            .defineList("targetWhitelist", List.of(), RegistryFilter::isValidEntry);

    public static final ConfigValue<List<? extends String>> DAMAGEABLE_ENTITIES = BUILDER
            .comment("Entities turret fire can damage, so bullets pass through bystanders. Leave empty to match what turrets target. Accepts entity ids and #entity tags.")
            .defineList("damageableEntities", List.of(), RegistryFilter::isValidEntry);

    public static final EnumValue<InaccuracyMode> INACCURACY_MODE = BUILDER
            .comment("How turret inaccuracy is calculated. DISTANCE gets less accurate the further away the target is, RANDOM is the same at any range.")
            .defineEnum("inaccuracyMode", InaccuracyMode.DISTANCE);

    public static final IntValue ADAPTIVE_RANGE = BUILDER
            .comment("Distance at which adaptive mode switches from conserving ammo to firing freely.")
            .defineInRange("adaptiveRange", 25, 1, 1000);

    public static final BooleanValue REPAIR_PARTICLES = BUILDER
            .comment("Repair particles.")
            .define("repairParticles", true);

    public static final ConfigValue<List<? extends String>> REPAIR_SOUNDS = BUILDER
            .comment("Sounds played when a turret is repaired. One is picked at random.")
            .defineList("repairSounds", List.of("minecraft:entity.iron_golem.repair"), RegistryFilter::isValidEntry);

    public static final BooleanValue ALLIES_CANNOT_BE_DAMAGED = BUILDER
            .comment("Allies cannot be damaged by turret fire.")
            .define("alliesCannotBeDamaged", true);

    public static final EnumValue<RetaliateTargeting> RETALIATE_TARGETING = BUILDER
            .comment("How turrets treat a player they are retaliating against. CONTINUE_TARGETING keeps shooting them, CLEAR_ON_DEATH forgets them once they die.")
            .defineEnum("retaliateTargeting", RetaliateTargeting.CLEAR_ON_DEATH);

    public static final IntValue RETALIATION_TIMER = BUILDER
            .comment("Grudge time in seconds when Retaliate Targeting type is Continue Targeting.")
            .defineInRange("retaliationTimer", 30, 1, 3600);

    public static final BooleanValue ALLIES_HAVE_PERMS = BUILDER
            .comment("Allies can interact with turrets even if they are not owner.")
            .define("alliesHavePerms", true);

    public static final BooleanValue OP_BYPASS = BUILDER
            .comment("Operators can manage any turret.")
            .define("opBypass", true);

    public static final BooleanValue OWNER_TAKES_NO_DAMAGE = BUILDER
            .comment("Owners cannot be damaged by their own turrets.")
            .define("ownerTakesNoDamage", true);

    public static final BooleanValue ENABLE_SOUNDS = BUILDER
            .comment("Enable sounds.")
            .define("enableSounds", true);

    public static final BooleanValue RELOAD_SOUND = BUILDER
            .comment("Turret reload sound.")
            .define("reloadSound", true);

    public static final BooleanValue TURRET_RECOIL = BUILDER
            .comment("Turret recoil.")
            .define("turretRecoil", true);

    public static final BooleanValue FIRST_PERSON_SHOOT_SOUND = BUILDER
            .comment("Turrets use the first person gunshot sound. Needed for sound packs that only replace first person sounds.")
            .define("firstPersonShootSound", false);

    public static final BooleanValue BETTER_TARGETING = BUILDER
            .comment("Better turret targeting. Turrets pick separate targets instead of focusing one.")
            .define("betterTargeting", true);

    public static final BooleanValue IDLE_LOOK_AROUND = BUILDER
            .comment("Turrets with a gun look around when they have no target.")
            .define("idleLookAround", true);

    public static final BooleanValue DAMAGE_PLAYERS = BUILDER
            .comment("Enable player damage.")
            .define("damagePlayers", true);

    public static final BooleanValue PROTECT_OWNER = BUILDER
            .comment("Turrets defend their owner.")
            .define("protectOwner", true);

    public static final BooleanValue RESPECT_TEAMS = BUILDER
            .comment("Turrets spare their owner's teammates on vanilla teams, FTB Teams and Open Parties and Claims parties. Allies of the owner's FTB team or party count as teammates.")
            .define("respectTeams", true);

    public static final BooleanValue CREDIT_KILLS_TO_OWNER = BUILDER
            .comment("Turret kills count as owner kills.")
            .define("creditKillsToOwner", true);

    public static final BooleanValue PASSIVE_HEALING = BUILDER
            .comment("Passive healing.")
            .define("passiveHealing", false);

    public static final IntValue PASSIVE_HEAL_INTERVAL = BUILDER
            .comment("Passive healing frequency in ticks.")
            .defineInRange("passiveHealInterval", 100, 1, 72000);

    public static final ConfigValue<List<? extends String>> REPAIR_ITEMS = BUILDER
            .comment("Items that can repair turrets. Accepts item ids and #item tags.")
            .defineList("repairItems", List.of("minecraft:iron_bars"), RegistryFilter::isValidEntry);

    public static final BooleanValue REQUIRE_ENERGY = BUILDER
            .comment("Turrets need FE to run.")
            .define("requireEnergy", false);

    //? if forge {
    public static final BooleanValue IDENTICAL_TURRETS = BUILDER
    //?} else {
    /*private static final Builder COMMON_BUILDER = new Builder();

    public static final BooleanValue IDENTICAL_TURRETS = COMMON_BUILDER
    *///?}
            .comment("All turrets use the modern turret's stats and recipe, so turret types only change the look.")
            .define("identicalTurrets", true);

    private static final TurretStats[] STATS = Arrays.stream(TurretType.values()).map(TurretStats::new).toArray(TurretStats[]::new);

    public static TurretStats stats(TurretType type) {
        return STATS[IDENTICAL_TURRETS.get() ? TurretType.MODERN.ordinal() : type.ordinal()];
    }

    public static boolean enabled(TurretType type) {
        return STATS[type.ordinal()].enabled.get();
    }

    public static int maxRange() {
        int range = 0;
        for (TurretType type : TurretType.values()) range = Math.max(range, Math.max(stats(type).range.get(), stats(type).sniperRange.get()));
        return range;
    }

    public static final class TurretStats {
        public final BooleanValue enabled;
        public final IntValue health;
        public final DoubleValue armor;
        public final IntValue range;
        public final IntValue sniperRange;
        public final DoubleValue distanceInaccuracy;
        public final DoubleValue randomInaccuracy;
        public final DoubleValue reloadSpeed;
        public final IntValue slotRows;
        public final IntValue slotLength;
        public final IntValue energyCapacity;
        public final IntValue energyTransferRate;
        public final IntValue energyPerShot;
        public final IntValue energyIdleDrain;
        public final DoubleValue passiveHealAmount;
        public final DoubleValue repairAmount;

        private TurretStats(TurretType type) {
            //? if forge {
            BUILDER.push(type.id());
            enabled = BUILDER.comment("Whether this turret can be crafted and placed.").define("enabled", true);
            //?} else {
            /*COMMON_BUILDER.push(type.id());
            enabled = COMMON_BUILDER.comment("Whether this turret can be crafted and placed.").define("enabled", true);
            COMMON_BUILDER.pop();
            BUILDER.push(type.id());
            *///?}
            health = BUILDER.comment("Health of the turret.").defineInRange("health", type.defaultHealth, 10, 1000);
            armor = BUILDER.comment("Armor points of the turret.").defineInRange("armor", type.defaultArmor, 0.0D, 1000.0D);
            range = BUILDER.comment("Detection and engagement range in blocks.").defineInRange("range", type.defaultRange, 8, 1000);
            sniperRange = BUILDER.comment("Range for sniper guns.").defineInRange("sniperRange", type.defaultRange * 2, 8, 1000);
            distanceInaccuracy = BUILDER.comment("Distance inaccuracy at maximum range. 0 always hits.").defineInRange("distanceInaccuracy", type.defaultInaccuracy, 0.0D, 45.0D);
            randomInaccuracy = BUILDER.comment("Random inaccuracy. 0 always hits.").defineInRange("randomInaccuracy", type.defaultInaccuracy, 0.0D, 45.0D);
            reloadSpeed = BUILDER.comment("Reload speed multiplier. 1.0 is the gun's normal speed.").defineInRange("reloadSpeed", type.defaultReloadSpeed, 1.0D, 10.0D);
            slotRows = BUILDER.comment("Rows of ammo slots in the turret screen.").defineInRange("slotRows", 2, 1, 6);
            slotLength = BUILDER.comment("Ammo slots per row in the turret screen.").defineInRange("slotLength", 5, 1, 9);
            energyCapacity = BUILDER.comment("Energy buffer size.").defineInRange("energyCapacity", 10000, 1, 1000000000);
            energyTransferRate = BUILDER.comment("Energy accepted per tick.").defineInRange("energyTransferRate", 200, 1, 1000000000);
            energyPerShot = BUILDER.comment("Energy used per shot.").defineInRange("energyPerShot", 10, 0, 1000000);
            energyIdleDrain = BUILDER.comment("Energy used per tick.").defineInRange("energyIdleDrain", 1, 0, 1000000);
            passiveHealAmount = BUILDER.comment("Passive healing amount.").defineInRange("passiveHealAmount", 1.0D, 0.0D, 1000.0D);
            repairAmount = BUILDER.comment("Health restored per repair item.").defineInRange("repairAmount", 25.0D, 0.0D, 1000.0D);
            BUILDER.pop();
        }
    }

    //? if forge {
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    //?} else {
    /*public static final ModConfigSpec SPEC = BUILDER.build();
    public static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();
    *///?}

    private static final Builder CLIENT_BUILDER = new Builder();

    public static final EnumValue<HealthBarStyle> HEALTH_BAR_STYLE = CLIENT_BUILDER
            .comment("Turret health bar style. GREEN_TO_RED fades green to orange to red as health drops, COLOR uses healthBarColor.")
            .defineEnum("healthBarStyle", HealthBarStyle.GREEN_TO_RED);

    public static final ConfigValue<String> HEALTH_BAR_COLOR = CLIENT_BUILDER
            .comment("Turret health bar colour as hex.")
            .define("healthBarColor", "#FF3030");

    public static final EnumValue<RecoilType> RECOIL_TYPE = CLIENT_BUILDER
            .comment("Turret recoil type. BOUNCE kicks the barrel up, PUSH slides the gun backwards.")
            .defineEnum("recoilType", RecoilType.PUSH);

    public static final BooleanValue LASER_WITHOUT_GUN = CLIENT_BUILDER
            .comment("Show a turret's laser even when it has no gun.")
            .define("laserWithoutGun", true);

    //? if forge {
    public static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();
    //?} else {
    /*public static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();
    *///?}

    public static Set<String> sniperGunTypes = Set.copyOf(SNIPER_GUN_TYPES.getDefault());
    public static int healthBarColor = parseColor(HEALTH_BAR_COLOR.getDefault());
    public static RegistryFilter<EntityType<?>> targetBlacklist = RegistryFilter.of(BuiltInRegistries.ENTITY_TYPE, TARGET_BLACKLIST.getDefault());
    public static RegistryFilter<EntityType<?>> targetWhitelist = RegistryFilter.of(BuiltInRegistries.ENTITY_TYPE, TARGET_WHITELIST.getDefault());
    public static RegistryFilter<EntityType<?>> damageableEntities = RegistryFilter.of(BuiltInRegistries.ENTITY_TYPE, DAMAGEABLE_ENTITIES.getDefault());
    public static RegistryFilter<Item> repairItems = RegistryFilter.of(BuiltInRegistries.ITEM, REPAIR_ITEMS.getDefault());
    public static RegistryFilter<MobEffect> effectBlacklist = RegistryFilter.of(BuiltInRegistries.MOB_EFFECT, EFFECT_BLACKLIST.getDefault());

    public static int parseColor(String hex) {
        String value = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            return (int) (Long.parseLong(value, 16) & 0xFFFFFF);
        } catch (NumberFormatException e) {
            return 0xFF3030;
        }
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Unloading) return;
        if (event.getConfig().getSpec() == CLIENT_SPEC) healthBarColor = parseColor(HEALTH_BAR_COLOR.get());
        if (event.getConfig().getSpec() != SPEC) return;
        sniperGunTypes = Set.copyOf(SNIPER_GUN_TYPES.get());
        targetBlacklist = RegistryFilter.of(BuiltInRegistries.ENTITY_TYPE, TARGET_BLACKLIST.get());
        targetWhitelist = RegistryFilter.of(BuiltInRegistries.ENTITY_TYPE, TARGET_WHITELIST.get());
        damageableEntities = RegistryFilter.of(BuiltInRegistries.ENTITY_TYPE, DAMAGEABLE_ENTITIES.get());
        repairItems = RegistryFilter.of(BuiltInRegistries.ITEM, REPAIR_ITEMS.get());
        effectBlacklist = RegistryFilter.of(BuiltInRegistries.MOB_EFFECT, EFFECT_BLACKLIST.get());
    }
}
