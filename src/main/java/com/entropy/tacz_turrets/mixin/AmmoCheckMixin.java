package com.entropy.tacz_turrets.mixin;

import com.entropy.tacz_turrets.turret.TurretEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.entity.shooter.LivingEntityAmmoCheck;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityAmmoCheck.class)
public class AmmoCheckMixin {
    @Shadow(remap = false)
    @Final
    private LivingEntity shooter;

    @ModifyReturnValue(method = "needCheckAmmo", at = @At("RETURN"), remap = false)
    private boolean freeReloadsForTurrets(boolean original) {
        return original && !(shooter instanceof TurretEntity turret && turret.hasInfiniteAmmo());
    }

    @ModifyReturnValue(method = "consumesAmmoOrNot", at = @At("RETURN"), remap = false)
    private boolean magazineUseForTurrets(boolean original) {
        return original && !(shooter instanceof TurretEntity turret && !turret.usesMagazine());
    }
}
