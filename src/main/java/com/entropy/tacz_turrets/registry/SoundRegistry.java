package com.entropy.tacz_turrets.registry;

import com.entropy.tacz_turrets.TACZTurrets;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<SoundEvent> TURRET_PLACE = SOUNDS.register("turret_place", () -> SoundEvent.createVariableRangeEvent(TACZTurrets.id("turret_place")));
    public static final RegistryObject<SoundEvent> TURRET_PICKUP = SOUNDS.register("turret_pickup", () -> SoundEvent.createVariableRangeEvent(TACZTurrets.id("turret_pickup")));
}
