package com.entropy.tacz_turrets.config;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.turret.HealthBarStyle;
import com.entropy.tacz_turrets.turret.InaccuracyMode;
import com.entropy.tacz_turrets.turret.RecoilType;
import com.entropy.tacz_turrets.turret.RetaliateTargeting;
import com.entropy.tacz_turrets.util.ItemFilter;
import com.entropy.tacz_turrets.util.TargetFilter;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = TACZTurrets.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TACZTurretsConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue CONSUME_AMMO = BUILDER
            .comment("Whether turrets need ammo.")
            .define("consumeAmmo", true);

    private static final ForgeConfigSpec.IntValue TURRET_RANGE = BUILDER
            .comment("Turret detection and engagement range in blocks.")
            .defineInRange("turretRange", 64, 8, 1000);

    private static final ForgeConfigSpec.IntValue SNIPER_TURRET_RANGE = BUILDER
            .comment("Turret range for sniper guns.")
            .defineInRange("sniperTurretRange", 128, 8, 1000);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SNIPER_GUN_TYPES = BUILDER
            .comment("Gun types that use the sniper range.")
            .defineList("sniperGunTypes", List.of("sniper"), entry -> entry instanceof String type && !type.isBlank());

    private static final ForgeConfigSpec.IntValue TURRET_HEALTH = BUILDER
            .comment("Health of turret.")
            .defineInRange("turretHealth", 200, 10, 1000);

    private static final ForgeConfigSpec.IntValue TURRET_SLOT_ROWS = BUILDER
            .comment("Rows of ammo slots in the turret screen.")
            .defineInRange("turretSlotRows", 2, 1, 6);

    private static final ForgeConfigSpec.IntValue TURRET_SLOT_LENGTH = BUILDER
            .comment("Ammo slots per row in the turret screen.")
            .defineInRange("turretSlotLength", 5, 1, 9);

    private static final ForgeConfigSpec.EnumValue<HealthBarStyle> HEALTH_BAR_STYLE = BUILDER
            .comment("Turret health bar style. GREEN_TO_RED fades green to orange to red as health drops, COLOR uses healthBarColor.")
            .defineEnum("healthBarStyle", HealthBarStyle.GREEN_TO_RED);

    private static final ForgeConfigSpec.ConfigValue<String> HEALTH_BAR_COLOR = BUILDER
            .comment("Turret health bar colour as hex.")
            .define("healthBarColor", "#FF3030");

    private static final ForgeConfigSpec.BooleanValue TURRETS_TAKE_DAMAGE = BUILDER
            .comment("If false, turrets will be immune to damage.")
            .define("turretsTakeDamage", true);

    private static final ForgeConfigSpec.BooleanValue TARGET_ALL_MOBS = BUILDER
            .comment("If true, turrets target all living entities (except players, turrets, and the owner). If false, turrets only target vanilla monsters and entities in the tacz_turrets:turret_targets entity type tag.")
            .define("targetAllMobs", false);

    private static final ForgeConfigSpec.BooleanValue LOG_TURRET_SHOOT_RESULTS = BUILDER
            .comment("Logs turret shoot results when enabled. Only use for debugging purposes.")
            .define("logTurretShootResults", false);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> TARGET_BLACKLIST = BUILDER
            .comment("Entities turrets never target. Accepts entity ids and #entity tags.")
            .defineList("targetBlacklist", List.of(), TargetFilter::isValidEntry);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> TARGET_WHITELIST = BUILDER
            .comment("Entities turrets always target. Accepts entity ids and #entity tags.")
            .defineList("targetWhitelist", List.of(), TargetFilter::isValidEntry);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DAMAGEABLE_ENTITIES = BUILDER
            .comment("Entities turret fire can damage, so bullets pass through bystanders. Leave empty to match what turrets target. Accepts entity ids and #entity tags.")
            .defineList("damageableEntities", List.of(), TargetFilter::isValidEntry);

    private static final ForgeConfigSpec.EnumValue<InaccuracyMode> INACCURACY_MODE = BUILDER
            .comment("How turret inaccuracy is calculated. DISTANCE gets less accurate the further away the target is, RANDOM is the same at any range.")
            .defineEnum("inaccuracyMode", InaccuracyMode.DISTANCE);

    private static final ForgeConfigSpec.DoubleValue DISTANCE_INACCURACY = BUILDER
            .comment("Distance inaccuracy at maximum range. 0 always hits.")
            .defineInRange("distanceInaccuracy", 1.0D, 0.0D, 45.0D);

    private static final ForgeConfigSpec.DoubleValue RANDOM_INACCURACY = BUILDER
            .comment("Random inaccuracy. 0 always hits.")
            .defineInRange("randomInaccuracy", 1.0D, 0.0D, 45.0D);

    private static final ForgeConfigSpec.IntValue ADAPTIVE_RANGE = BUILDER
            .comment("Distance at which adaptive mode switches from conserving ammo to firing freely.")
            .defineInRange("adaptiveRange", 25, 1, 1000);

    private static final ForgeConfigSpec.BooleanValue REPAIR_PARTICLES = BUILDER
            .comment("Repair particles.")
            .define("repairParticles", true);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> REPAIR_SOUNDS = BUILDER
            .comment("Sounds played when a turret is repaired. One is picked at random.")
            .defineList("repairSounds", List.of("minecraft:entity.iron_golem.repair"), ItemFilter::isValidEntry);

    private static final ForgeConfigSpec.EnumValue<RecoilType> RECOIL_TYPE = BUILDER
            .comment("Turret recoil type. BOUNCE kicks the barrel up, PUSH slides the gun backwards.")
            .defineEnum("recoilType", RecoilType.PUSH);

    private static final ForgeConfigSpec.BooleanValue ALLIES_CANNOT_BE_DAMAGED = BUILDER
            .comment("Allies cannot be damaged by turret fire.")
            .define("alliesCannotBeDamaged", true);

    private static final ForgeConfigSpec.EnumValue<RetaliateTargeting> RETALIATE_TARGETING = BUILDER
            .comment("How turrets treat a player they are retaliating against. CONTINUE_TARGETING keeps shooting them, CLEAR_ON_DEATH forgets them once they die.")
            .defineEnum("retaliateTargeting", RetaliateTargeting.CLEAR_ON_DEATH);

    private static final ForgeConfigSpec.IntValue RETALIATION_TIMER = BUILDER
            .comment("Grudge time in seconds when Retaliate Targeting type is Continue Targeting.")
            .defineInRange("retaliationTimer", 30, 1, 3600);

    private static final ForgeConfigSpec.BooleanValue ALLIES_HAVE_PERMS = BUILDER
            .comment("Allies can interact with turrets even if they are not owner.")
            .define("alliesHavePerms", true);

    private static final ForgeConfigSpec.BooleanValue OP_BYPASS = BUILDER
            .comment("Operators can manage any turret.")
            .define("opBypass", true);

    private static final ForgeConfigSpec.BooleanValue OWNER_TAKES_NO_DAMAGE = BUILDER
            .comment("Owners cannot be damaged by their own turrets.")
            .define("ownerTakesNoDamage", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_SOUNDS = BUILDER
            .comment("Enable sounds.")
            .define("enableSounds", true);

    private static final ForgeConfigSpec.BooleanValue RELOAD_SOUND = BUILDER
            .comment("Turret reload sound.")
            .define("reloadSound", true);

    private static final ForgeConfigSpec.BooleanValue TURRET_RECOIL = BUILDER
            .comment("Turret recoil.")
            .define("turretRecoil", true);

    private static final ForgeConfigSpec.BooleanValue FIRST_PERSON_SHOOT_SOUND = BUILDER
            .comment("Turrets use the first person gunshot sound. Needed for sound packs that only replace first person sounds.")
            .define("firstPersonShootSound", false);

    private static final ForgeConfigSpec.BooleanValue BETTER_TARGETING = BUILDER
            .comment("Better turret targeting. Turrets pick separate targets instead of focusing one.")
            .define("betterTargeting", true);

    private static final ForgeConfigSpec.BooleanValue DAMAGE_PLAYERS = BUILDER
            .comment("Enable player damage.")
            .define("damagePlayers", true);

    private static final ForgeConfigSpec.BooleanValue PROTECT_OWNER = BUILDER
            .comment("Turrets defend their owner.")
            .define("protectOwner", true);

    private static final ForgeConfigSpec.BooleanValue RESPECT_TEAMS = BUILDER
            .comment("Turrets spare teammates of their owner.")
            .define("respectTeams", true);

    private static final ForgeConfigSpec.BooleanValue CREDIT_KILLS_TO_OWNER = BUILDER
            .comment("Turret kills count as owner kills.")
            .define("creditKillsToOwner", true);

    private static final ForgeConfigSpec.BooleanValue PASSIVE_HEALING = BUILDER
            .comment("Passive healing.")
            .define("passiveHealing", false);

    private static final ForgeConfigSpec.DoubleValue PASSIVE_HEAL_AMOUNT = BUILDER
            .comment("Passive healing amount.")
            .defineInRange("passiveHealAmount", 1.0D, 0.0D, 1000.0D);

    private static final ForgeConfigSpec.IntValue PASSIVE_HEAL_INTERVAL = BUILDER
            .comment("Passive healing frequency in ticks.")
            .defineInRange("passiveHealInterval", 100, 1, 72000);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> REPAIR_ITEMS = BUILDER
            .comment("Items that can repair turrets. Accepts item ids and #item tags.")
            .defineList("repairItems", List.of("minecraft:iron_bars"), ItemFilter::isValidEntry);

    private static final ForgeConfigSpec.DoubleValue REPAIR_AMOUNT = BUILDER
            .comment("Health restored per repair item.")
            .defineInRange("repairAmount", 25.0D, 0.0D, 1000.0D);

    private static final ForgeConfigSpec.BooleanValue REQUIRE_ENERGY = BUILDER
            .comment("Turrets need FE to run.")
            .define("requireEnergy", false);

    private static final ForgeConfigSpec.IntValue ENERGY_CAPACITY = BUILDER
            .comment("Energy buffer size.")
            .defineInRange("energyCapacity", 10000, 1, 1000000000);

    private static final ForgeConfigSpec.IntValue ENERGY_TRANSFER_RATE = BUILDER
            .comment("Energy accepted per tick.")
            .defineInRange("energyTransferRate", 200, 1, 1000000000);

    private static final ForgeConfigSpec.IntValue ENERGY_PER_SHOT = BUILDER
            .comment("Energy used per shot.")
            .defineInRange("energyPerShot", 10, 0, 1000000);

    private static final ForgeConfigSpec.IntValue ENERGY_IDLE_DRAIN = BUILDER
            .comment("Energy used per tick.")
            .defineInRange("energyIdleDrain", 1, 0, 1000000);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean consumeAmmo = CONSUME_AMMO.getDefault();
    public static int turretRange = TURRET_RANGE.getDefault();
    public static int sniperTurretRange = SNIPER_TURRET_RANGE.getDefault();
    public static Set<String> sniperGunTypes = Set.copyOf(SNIPER_GUN_TYPES.getDefault());
    public static int turretHealth = TURRET_HEALTH.getDefault();
    public static int turretSlotRows = TURRET_SLOT_ROWS.getDefault();
    public static int turretSlotLength = TURRET_SLOT_LENGTH.getDefault();
    public static HealthBarStyle healthBarStyle = HEALTH_BAR_STYLE.getDefault();
    public static int healthBarColor = parseColor(HEALTH_BAR_COLOR.getDefault());
    public static boolean turretsTakeDamage = TURRETS_TAKE_DAMAGE.getDefault();
    public static boolean targetAllMobs = TARGET_ALL_MOBS.getDefault();
    public static boolean logTurretShootResults = LOG_TURRET_SHOOT_RESULTS.getDefault();
    public static TargetFilter targetBlacklist = TargetFilter.of(TARGET_BLACKLIST.getDefault());
    public static TargetFilter targetWhitelist = TargetFilter.of(TARGET_WHITELIST.getDefault());
    public static TargetFilter damageableEntities = TargetFilter.of(DAMAGEABLE_ENTITIES.getDefault());
    public static InaccuracyMode inaccuracyMode = INACCURACY_MODE.getDefault();
    public static double distanceInaccuracy = DISTANCE_INACCURACY.getDefault();
    public static double randomInaccuracy = RANDOM_INACCURACY.getDefault();
    public static int adaptiveRange = ADAPTIVE_RANGE.getDefault();
    public static boolean repairParticles = REPAIR_PARTICLES.getDefault();
    public static List<? extends String> repairSounds = REPAIR_SOUNDS.getDefault();
    public static RecoilType recoilType = RECOIL_TYPE.getDefault();
    public static boolean alliesCannotBeDamaged = ALLIES_CANNOT_BE_DAMAGED.getDefault();
    public static boolean ownerTakesNoDamage = OWNER_TAKES_NO_DAMAGE.getDefault();
    public static boolean opBypass = OP_BYPASS.getDefault();
    public static boolean alliesHavePerms = ALLIES_HAVE_PERMS.getDefault();
    public static RetaliateTargeting retaliateTargeting = RETALIATE_TARGETING.getDefault();
    public static int retaliationTimer = RETALIATION_TIMER.getDefault();
    public static boolean enableSounds = ENABLE_SOUNDS.getDefault();
    public static boolean reloadSound = RELOAD_SOUND.getDefault();
    public static boolean turretRecoil = TURRET_RECOIL.getDefault();
    public static boolean firstPersonShootSound = FIRST_PERSON_SHOOT_SOUND.getDefault();
    public static boolean betterTargeting = BETTER_TARGETING.getDefault();
    public static boolean damagePlayers = DAMAGE_PLAYERS.getDefault();
    public static boolean protectOwner = PROTECT_OWNER.getDefault();
    public static boolean respectTeams = RESPECT_TEAMS.getDefault();
    public static boolean creditKillsToOwner = CREDIT_KILLS_TO_OWNER.getDefault();
    public static boolean passiveHealing = PASSIVE_HEALING.getDefault();
    public static double passiveHealAmount = PASSIVE_HEAL_AMOUNT.getDefault();
    public static int passiveHealInterval = PASSIVE_HEAL_INTERVAL.getDefault();
    public static ItemFilter repairItems = ItemFilter.of(REPAIR_ITEMS.getDefault());
    public static double repairAmount = REPAIR_AMOUNT.getDefault();
    public static boolean requireEnergy = REQUIRE_ENERGY.getDefault();
    public static int energyCapacity = ENERGY_CAPACITY.getDefault();
    public static int energyTransferRate = ENERGY_TRANSFER_RATE.getDefault();
    public static int energyPerShot = ENERGY_PER_SHOT.getDefault();
    public static int energyIdleDrain = ENERGY_IDLE_DRAIN.getDefault();

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
        consumeAmmo = CONSUME_AMMO.get();
        turretRange = TURRET_RANGE.get();
        sniperTurretRange = SNIPER_TURRET_RANGE.get();
        sniperGunTypes = Set.copyOf(SNIPER_GUN_TYPES.get());
        turretHealth = TURRET_HEALTH.get();
        turretSlotRows = TURRET_SLOT_ROWS.get();
        turretSlotLength = TURRET_SLOT_LENGTH.get();
        healthBarStyle = HEALTH_BAR_STYLE.get();
        healthBarColor = parseColor(HEALTH_BAR_COLOR.get());
        turretsTakeDamage = TURRETS_TAKE_DAMAGE.get();
        targetAllMobs = TARGET_ALL_MOBS.get();
        logTurretShootResults = LOG_TURRET_SHOOT_RESULTS.get();
        targetBlacklist = TargetFilter.of(TARGET_BLACKLIST.get());
        targetWhitelist = TargetFilter.of(TARGET_WHITELIST.get());
        damageableEntities = TargetFilter.of(DAMAGEABLE_ENTITIES.get());
        inaccuracyMode = INACCURACY_MODE.get();
        distanceInaccuracy = DISTANCE_INACCURACY.get();
        randomInaccuracy = RANDOM_INACCURACY.get();
        adaptiveRange = ADAPTIVE_RANGE.get();
        repairParticles = REPAIR_PARTICLES.get();
        repairSounds = REPAIR_SOUNDS.get();
        recoilType = RECOIL_TYPE.get();
        alliesCannotBeDamaged = ALLIES_CANNOT_BE_DAMAGED.get();
        ownerTakesNoDamage = OWNER_TAKES_NO_DAMAGE.get();
        opBypass = OP_BYPASS.get();
        alliesHavePerms = ALLIES_HAVE_PERMS.get();
        retaliateTargeting = RETALIATE_TARGETING.get();
        retaliationTimer = RETALIATION_TIMER.get();
        enableSounds = ENABLE_SOUNDS.get();
        reloadSound = RELOAD_SOUND.get();
        turretRecoil = TURRET_RECOIL.get();
        firstPersonShootSound = FIRST_PERSON_SHOOT_SOUND.get();
        betterTargeting = BETTER_TARGETING.get();
        damagePlayers = DAMAGE_PLAYERS.get();
        protectOwner = PROTECT_OWNER.get();
        respectTeams = RESPECT_TEAMS.get();
        creditKillsToOwner = CREDIT_KILLS_TO_OWNER.get();
        passiveHealing = PASSIVE_HEALING.get();
        passiveHealAmount = PASSIVE_HEAL_AMOUNT.get();
        passiveHealInterval = PASSIVE_HEAL_INTERVAL.get();
        repairItems = ItemFilter.of(REPAIR_ITEMS.get());
        repairAmount = REPAIR_AMOUNT.get();
        requireEnergy = REQUIRE_ENERGY.get();
        energyCapacity = ENERGY_CAPACITY.get();
        energyTransferRate = ENERGY_TRANSFER_RATE.get();
        energyPerShot = ENERGY_PER_SHOT.get();
        energyIdleDrain = ENERGY_IDLE_DRAIN.get();
    }
}
