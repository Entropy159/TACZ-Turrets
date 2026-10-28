package com.entropy.tacz_turrets.mixin;

import com.entropy.tacz_turrets.turret.TurretEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GunData.class)
public class FireRateMixin {
    @ModifyReturnValue(method = "getShootInterval", at = @At("RETURN"), remap = false)
    private long turretFireRate(long interval, LivingEntity shooter, FireMode fireMode, ItemStack gun) {
        if (shooter instanceof TurretEntity turret) return Math.max(1L, Math.round(interval / turret.fireRate()));
        return interval;
    }
}
