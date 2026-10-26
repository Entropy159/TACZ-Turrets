package com.entropy.tacz_turrets.mixin;

import com.entropy.tacz_turrets.turret.TurretEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModernKineticGunScriptAPI.class)
public class ReloadSpeedMixin {
    @Shadow(remap = false)
    private LivingEntity shooter;

    @ModifyReturnValue(method = "getReloadTime", at = @At("RETURN"), remap = false)
    private long turretReloadSpeed(long time) {
        if (time > 0 && shooter instanceof TurretEntity turret) return (long) (time * turret.reloadSpeed());
        return time;
    }
}
