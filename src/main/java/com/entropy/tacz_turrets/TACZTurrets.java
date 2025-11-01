package com.entropy.tacz_turrets;

import com.entropy.tacz_turrets.common.registry.AttributeRegistry;
import com.entropy.tacz_turrets.common.registry.EntityTypeRegistry;
import com.entropy.tacz_turrets.common.registry.ItemRegistry;
import com.mojang.logging.LogUtils;
import com.tacz.guns.init.ModCreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TACZTurrets.MODID)
public class TACZTurrets {
    public static final String MODID = "tacz_turrets";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TACZTurrets(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, TACZTurretsConfig.SPEC);
        IEventBus modEventBus = context.getModEventBus();
        EntityTypeRegistry.TYPES.register(modEventBus);

        ItemRegistry.ITEMS.register(modEventBus);
        modEventBus.addListener(AttributeRegistry::register);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    public void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.OTHER_TAB.getKey()) event.accept(ItemRegistry.TURRET.get());
    }
}
