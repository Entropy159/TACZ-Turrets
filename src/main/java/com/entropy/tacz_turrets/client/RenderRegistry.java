package com.entropy.tacz_turrets.client;

import com.entropy.tacz_turrets.client.renderer.TurretRenderer;
import com.entropy.tacz_turrets.common.registry.EntityTypeRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RenderRegistry {
    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        EntityRenderers.register(EntityTypeRegistry.TURRET.get(), TurretRenderer::new);
    }
}
