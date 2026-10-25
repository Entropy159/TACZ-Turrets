package com.entropy.tacz_turrets.registry;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.item.TurretItem;
import com.entropy.tacz_turrets.turret.TurretType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final Map<TurretType, Supplier<TurretItem>> TURRETS = new EnumMap<>(TurretType.class);

    static {
        for (TurretType type : TurretType.values()) TURRETS.put(type, ITEMS.register(type.id() + "_turret", () -> new TurretItem(type)));
        //? if neoforge
        /*ITEMS.addAlias(TACZTurrets.id("turret"), TACZTurrets.id("modern_turret"));*/
    }

    public static TurretItem turret(TurretType type) {
        return TURRETS.get(type).get();
    }
}
