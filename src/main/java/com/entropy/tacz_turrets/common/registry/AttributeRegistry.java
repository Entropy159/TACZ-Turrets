package com.entropy.tacz_turrets.common.registry;

import com.entropy.tacz_turrets.common.entity.TurretEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AttributeRegistry {
    @SubscribeEvent
    public static void register(EntityAttributeCreationEvent event) {
        event.put(EntityTypeRegistry.TURRET.get(), TurretEntity.createLivingAttributes().build());
    }
}
