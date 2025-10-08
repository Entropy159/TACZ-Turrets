package com.entropy.tacz_turrets.common.registry;

import com.entropy.tacz_turrets.common.entity.TurretEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

@Mod.EventBusSubscriber
public class EntityTypeRegistry {
    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<TurretEntity>> TURRET = TYPES.register("turret", () -> TurretEntity.TYPE);

    @SubscribeEvent
    public static void register(RegisterEvent event) {

    }
}
