package com.entropy.tacz_turrets.registry;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.item.CamoItem;
import com.entropy.tacz_turrets.item.TurretItem;
import com.entropy.tacz_turrets.turret.TurretType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
//? if forge {
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final Map<TurretType, Supplier<TurretItem>> TURRETS = new EnumMap<>(TurretType.class);
    public static final List<String> CAMOS = List.of("desert", "badlands", "forest", "snow", "jungle", "swamp", "urban", "night", "ocean", "autumn", "nether", "end", "oak", "spruce", "birch", "acacia", "dark_oak", "mangrove", "cherry");

    static {
        for (TurretType type : TurretType.values()) TURRETS.put(type, ITEMS.register(type.id() + "_turret", () -> new TurretItem(type)));
        for (String camo : CAMOS) ITEMS.register(camo + "_camo", () -> new CamoItem(camo));
        //? if neoforge
        /*ITEMS.addAlias(TACZTurrets.id("turret"), TACZTurrets.id("modern_turret"));*/
    }

    //? if forge {
    public static final Supplier<Item> SENTRY_SPAWN_EGG = ITEMS.register("sentry_turret_spawn_egg", () -> new ForgeSpawnEggItem(EntityTypeRegistry.SENTRY, 0x3B4046, 0xD8492F, new Item.Properties()));
    //?} else {
    /*public static final Supplier<Item> SENTRY_SPAWN_EGG = ITEMS.register("sentry_turret_spawn_egg", () -> new DeferredSpawnEggItem(EntityTypeRegistry.SENTRY, 0x3B4046, 0xD8492F, new Item.Properties()));
    *///?}

    public static TurretItem turret(TurretType type) {
        return TURRETS.get(type).get();
    }
}
