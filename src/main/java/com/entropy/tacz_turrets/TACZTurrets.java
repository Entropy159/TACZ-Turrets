package com.entropy.tacz_turrets;

import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.network.TACZTurretsNetwork;
import com.entropy.tacz_turrets.registry.EntityTypeRegistry;
import com.entropy.tacz_turrets.registry.ItemRegistry;
import com.entropy.tacz_turrets.registry.MenuRegistry;
import com.entropy.tacz_turrets.registry.SoundRegistry;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.mojang.logging.LogUtils;
import com.tacz.guns.init.ModCreativeTabs;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
//? if forge {
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
*///?}

@Mod(TACZTurrets.MODID)
public class TACZTurrets {
    public static final String MODID = "tacz_turrets";
    public static final Logger LOGGER = LogUtils.getLogger();

    //? if forge {
    public TACZTurrets(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        context.registerConfig(ModConfig.Type.COMMON, TACZTurretsConfig.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, TACZTurretsConfig.CLIENT_SPEC);
        TACZTurretsNetwork.register();
    //?} else {
    /*public TACZTurrets(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, TACZTurretsConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, TACZTurretsConfig.CLIENT_SPEC);
        modEventBus.addListener(TACZTurretsNetwork::register);
        modEventBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerEntity(Capabilities.ItemHandler.ENTITY, EntityTypeRegistry.TURRET.get(), (turret, side) -> turret.getInventory());
            event.registerEntity(Capabilities.EnergyStorage.ENTITY, EntityTypeRegistry.TURRET.get(), (turret, side) -> turret.getEnergyStorage());
        });
    *///?}
        EntityTypeRegistry.TYPES.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        MenuRegistry.MENUS.register(modEventBus);
        SoundRegistry.SOUNDS.register(modEventBus);
        modEventBus.addListener((EntityAttributeCreationEvent event) -> event.put(EntityTypeRegistry.TURRET.get(), TurretEntity.createLivingAttributes().build()));
        modEventBus.addListener(this::addCreative);
    }

    public void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.OTHER_TAB.getKey()) event.accept(ItemRegistry.TURRET.get());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
