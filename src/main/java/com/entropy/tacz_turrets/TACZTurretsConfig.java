package com.entropy.tacz_turrets;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Config(name = TACZTurrets.MODID)
public class TACZTurretsConfig implements ConfigData {
    public boolean consumeAmmo = true;

    public static TACZTurretsConfig get() {
        return AutoConfig.getConfigHolder(TACZTurretsConfig.class).getConfig();
    }

    public static Screen createScreen(Screen parent) {
        TACZTurretsConfig config = get();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Component.literal("TACZ Turrets")).setSavingRunnable(() -> {
            AutoConfig.getConfigHolder(TACZTurretsConfig.class).save();
        });
        ConfigCategory mainCategory = builder.getOrCreateCategory(Component.literal("Main"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        mainCategory.addEntry(entryBuilder.startBooleanToggle(Component.literal("Consume Ammo"), config.consumeAmmo).setDefaultValue(false).setSaveConsumer(newVal -> config.consumeAmmo = newVal).build());
        return builder.build();
    }
}
