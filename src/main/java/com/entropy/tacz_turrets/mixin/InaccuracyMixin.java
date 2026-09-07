package com.entropy.tacz_turrets.mixin;

import com.entropy.tacz_turrets.turret.TurretEntity;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InaccuracyType.class)
public class InaccuracyMixin {
    @Inject(method = "getInaccuracyType", at = @At("HEAD"), cancellable = true, remap = false)
    private static void turretsAlwaysAim(LivingEntity livingEntity, CallbackInfoReturnable<InaccuracyType> cir) {
        if (livingEntity instanceof TurretEntity) {
            cir.setReturnValue(InaccuracyType.AIM);
        }
    }
}
