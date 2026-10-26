package com.entropy.tacz_turrets.registry;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.turret.TurretType;
import com.tacz.guns.api.item.builder.BlockItemBuilder;
import com.tacz.guns.init.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.function.Supplier;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

public class CreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final Supplier<CreativeModeTab> TURRETS = TABS.register(MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tacz_turrets"))
            .icon(() -> new ItemStack(ItemRegistry.turret(TurretType.MODERN)))
            .displayItems((parameters, output) -> {
                output.accept(BlockItemBuilder.create(ModItems.WORKBENCH_211.get()).setId(TACZTurrets.id("turret_workbench")).build());
                ItemRegistry.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
            })
            .build());
}
