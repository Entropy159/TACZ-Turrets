package com.entropy.tacz_turrets.client;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.client.renderer.TurretRenderer;
import com.entropy.tacz_turrets.client.screen.TurretScreen;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.registry.EntityTypeRegistry;
import com.entropy.tacz_turrets.registry.ItemRegistry;
import com.entropy.tacz_turrets.registry.MenuRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
//? if forge {
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
//?} else {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
*///?}

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

//? if forge {
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//?} else {
/*@EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = EventBusSubscriber.Bus.MOD)
*///?}
public class RenderRegistry {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityTypeRegistry.TURRET.get(), TurretRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            //? if forge
            MenuScreens.register(MenuRegistry.TURRET.get(), TurretScreen::new);
            ItemProperties.register(ItemRegistry.TURRET.get(), TACZTurrets.id("model"), (stack, level, entity, seed) -> TACZTurretsConfig.MODEL_TYPE.get().ordinal());
        });
        //? if neoforge
        /*ModList.get().getModContainerById(MODID).ifPresent(container -> container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new));*/
    }

    //? if neoforge {
    /*@SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.TURRET.get(), TurretScreen::new);
    }
    *///?}
}
