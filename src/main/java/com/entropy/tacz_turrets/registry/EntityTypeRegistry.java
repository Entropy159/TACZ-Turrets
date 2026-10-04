package com.entropy.tacz_turrets.registry;

import com.entropy.tacz_turrets.turret.TurretEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.function.Supplier;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

public class EntityTypeRegistry {
    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final Supplier<EntityType<TurretEntity>> TURRET = TYPES.register("turret", () -> EntityType.Builder.<TurretEntity>of(TurretEntity::new, MobCategory.MISC).sized(1f, 1f).build("turret"));
}
