package com.entropy.tacz_turrets.common.registry;

import com.entropy.tacz_turrets.TurretItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<TurretItem> TURRET = ITEMS.register("turret", TurretItem::new);
}
