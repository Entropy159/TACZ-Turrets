package com.entropy.tacz_turrets.mixin;

import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.tacz.guns.sound.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class ShootSoundMixin {
    @Inject(method = "sendSoundToNearby", at = @At("HEAD"), cancellable = true, remap = false)
    private static void turretShootSound(LivingEntity sourceEntity, int distance, ResourceLocation gunId, ResourceLocation gunDisplayId, String soundName, float volume, float pitch, CallbackInfo ci) {
        if (!TACZTurretsConfig.firstPersonShootSound || !(sourceEntity instanceof TurretEntity)) {
            return;
        }
        String replacement;
        if (SoundManager.SHOOT_3P_SOUND.equals(soundName)) {
            replacement = SoundManager.SHOOT_SOUND;
        } else if (SoundManager.SILENCE_3P_SOUND.equals(soundName)) {
            replacement = SoundManager.SILENCE_SOUND;
        } else {
            return;
        }
        ci.cancel();
        SoundManager.sendSoundToNearby(sourceEntity, distance, gunId, gunDisplayId, replacement, volume, pitch);
    }
}
