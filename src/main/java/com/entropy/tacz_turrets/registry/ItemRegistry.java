package com.entropy.tacz_turrets.registry;

import com.entropy.tacz_turrets.item.TurretItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.function.Supplier;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final Supplier<TurretItem> TURRET = ITEMS.register("turret", TurretItem::new);
}
