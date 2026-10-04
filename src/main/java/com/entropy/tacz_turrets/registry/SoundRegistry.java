package com.entropy.tacz_turrets.registry;

import com.entropy.tacz_turrets.TACZTurrets;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.function.Supplier;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final Supplier<SoundEvent> TURRET_PLACE = SOUNDS.register("turret_place", () -> SoundEvent.createVariableRangeEvent(TACZTurrets.id("turret_place")));
    public static final Supplier<SoundEvent> TURRET_PICKUP = SOUNDS.register("turret_pickup", () -> SoundEvent.createVariableRangeEvent(TACZTurrets.id("turret_pickup")));
    public static final Supplier<SoundEvent> TURRET_HURT = SOUNDS.register("turret_hurt", () -> SoundEvent.createVariableRangeEvent(TACZTurrets.id("turret_hurt")));
}
